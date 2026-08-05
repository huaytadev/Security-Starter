package com.security_starter.refreshtoken.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.security_starter.common.exception.UnauthorizedException;
import com.security_starter.refreshtoken.entity.RefreshTokenEntity;
import com.security_starter.refreshtoken.repository.RefreshTokenRepository;
import com.security_starter.user.entity.UserEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

	@Value("${spring.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshTokenEntity create(UserEntity user) {

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(
        		Instant.now().plusMillis(refreshTokenExpiration)
        );
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshTokenEntity verify(String token) {

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid or expired refresh token."));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Invalid or expired refresh token.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {

            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);

            throw new UnauthorizedException("Invalid or expired refresh token.");
        }

        return refreshToken;
    }
    
    @Transactional
    @Override
    public RefreshTokenEntity rotate(String token) {

        RefreshTokenEntity currentToken = verify(token);

        currentToken.setRevoked(true);

        refreshTokenRepository.save(currentToken);

        return create(currentToken.getUser());
    }

    @Override
    public void revoke(String token) {

        RefreshTokenEntity refreshToken = verify(token);

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAll(UserEntity user) {

    	List<RefreshTokenEntity> refreshTokens =
    	        refreshTokenRepository.findAllByUserAndRevokedFalse(user);

    	refreshTokens.forEach(token -> token.setRevoked(true));

    	refreshTokenRepository.saveAll(refreshTokens);
    }
}
