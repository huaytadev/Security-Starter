package com.security_starter.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security_starter.auth.dto.AuthResponse;
import com.security_starter.auth.dto.LoginRequest;
import com.security_starter.auth.dto.LogoutRequest;
import com.security_starter.auth.dto.RefreshTokenRequest;
import com.security_starter.auth.dto.RegisterRequest;
import com.security_starter.auth.service.AuthService;
import com.security_starter.common.response.ApiResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User registered successfully",
                        response
                ));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        response
                )
        );
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Generate a new access token using a refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        AuthResponse response = authService.refresh(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Token refreshed successfully",
                        response
                )
        );
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {

        authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success("Logout successful")
        );
    }
}
