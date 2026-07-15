package com.ashish.rentflatmatefinder.security.jwt;

import com.ashish.rentflatmatefinder.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes()
        );

    }
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList());

        if (userDetails instanceof com.ashish.rentflatmatefinder.service.CustomUserDetails customUser) {
            extraClaims.put("userId", customUser.getUser().getId());
            extraClaims.put("role", customUser.getUser().getRole().getName().name());
        }

        return generateToken(
                extraClaims,
                userDetails,
                jwtProperties.getAccessTokenExpiration()
        );
    }
    private String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();

    }
    public String generateRefreshToken(UserDetails userDetails) {

        return generateToken(
                new HashMap<>(),
                userDetails,
                jwtProperties.getRefreshTokenExpiration()
        );

    }
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);

    }
    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );

    }
    public Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );

    }
    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());

    }
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && userDetails.isEnabled();

    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

}