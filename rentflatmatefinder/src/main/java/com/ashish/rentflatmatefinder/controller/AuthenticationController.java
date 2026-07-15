package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.request.LoginRequest;
import com.ashish.rentflatmatefinder.dto.request.RegisterRequest;
import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import com.ashish.rentflatmatefinder.dto.response.AuthenticationResponse;
import com.ashish.rentflatmatefinder.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthenticationResponse response =
                authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AuthenticationResponse>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthenticationResponse response =
                authenticationService.login(request);

        return ResponseEntity.ok(
                ApiResponse.<AuthenticationResponse>builder()
                        .success(true)
                        .message("Login successful")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestParam String refreshToken) {

        authenticationService.logout(refreshToken);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Logout successful")
                        .data("Refresh token revoked")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @RequestParam String refreshToken) {

        AuthenticationResponse response =
                authenticationService.refreshToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponse.<AuthenticationResponse>builder()
                        .success(true)
                        .message("Access token refreshed")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}