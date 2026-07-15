package com.ashish.rentflatmatefinder.service.impl;

import com.ashish.rentflatmatefinder.config.JwtProperties;
import com.ashish.rentflatmatefinder.dto.request.LoginRequest;
import com.ashish.rentflatmatefinder.dto.request.RegisterRequest;
import com.ashish.rentflatmatefinder.dto.response.AuthenticationResponse;
import com.ashish.rentflatmatefinder.entity.RefreshToken;
import com.ashish.rentflatmatefinder.entity.Role;
import com.ashish.rentflatmatefinder.entity.RoleName;
import com.ashish.rentflatmatefinder.entity.User;
import com.ashish.rentflatmatefinder.exception.BadRequestException;
import com.ashish.rentflatmatefinder.repository.RefreshTokenRepository;
import com.ashish.rentflatmatefinder.repository.RoleRepository;
import com.ashish.rentflatmatefinder.repository.UserRepository;
import com.ashish.rentflatmatefinder.security.jwt.JwtService;
import com.ashish.rentflatmatefinder.service.AuthenticationService;
import com.ashish.rentflatmatefinder.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    @Override
    public AuthenticationResponse register(RegisterRequest request) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email already exists");
        }

        RoleName requestedRole = request.getRole() != null ? request.getRole() : RoleName.TENANT;

        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() ->
                        new BadRequestException("Invalid role"));

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(normalizedEmail);
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEnabled(true);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return generateAuthenticationResponse(user);
    }

    @Transactional
    @Override
    public AuthenticationResponse login(LoginRequest request) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmailWithRole(normalizedEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return generateAuthenticationResponse(user);
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() ->
                        new BadRequestException("Invalid refresh token"));

        if (token.getRevoked()) {
            throw new BadRequestException("Refresh token has been revoked");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token has expired");
        }

        User user = userRepository.findByIdWithRole(token.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() ->
                        new BadRequestException("Invalid refresh token"));

        token.setRevoked(true);

        refreshTokenRepository.save(token);
    }

    private AuthenticationResponse generateAuthenticationResponse(User user) {

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);

        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken token = new RefreshToken();

        token.setToken(refreshToken);

        token.setUser(user);

        token.setExpiryDate(
                LocalDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpiration() / 1000
                )
        );

        token.setRevoked(false);

        refreshTokenRepository.save(token);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthenticationResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().getName().name())
                .build();
    }
}