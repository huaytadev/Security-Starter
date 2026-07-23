package com.security_starter.auth.dto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        String username,
        String email,
        Instant issuedAt
) {}
