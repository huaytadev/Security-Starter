package com.security_starter.auth.mapper;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.security_starter.auth.dto.AuthResponse;
import com.security_starter.refreshtoken.entity.RefreshTokenEntity;
import com.security_starter.security.jwt.JwtService;
import com.security_starter.user.entity.UserEntity;

@Component
public class AuthMapper {

    private final JwtService jwtService;

    public AuthMapper(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthResponse toRegisterResponse(UserEntity user) {

        return new AuthResponse(
                null,
                null,
                "Bearer",
                null,
                user.getUsername(),
                user.getEmail(),
                Instant.now()
        );
    }

    public AuthResponse toLoginResponse(
            UserEntity user,
            String accessToken,
            RefreshTokenEntity refreshToken
    ) {

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtService.getAccessTokenExpiration(),
                user.getUsername(),
                user.getEmail(),
                Instant.now()
        );
    }

    public AuthResponse toRefreshResponse(
            UserEntity user,
            String accessToken,
            RefreshTokenEntity refreshToken
    ) {

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtService.getAccessTokenExpiration(),
                user.getUsername(),
                user.getEmail(),
                Instant.now()
        );
    }

}