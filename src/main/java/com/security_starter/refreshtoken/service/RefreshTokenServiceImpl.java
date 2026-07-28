package com.security_starter.refreshtoken.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.security_starter.common.exception.UnauthorizedException;
import com.security_starter.refreshtoken.entity.RefreshTokenEntity;
import com.security_starter.refreshtoken.repository.RefreshTokenRepository;
import com.security_starter.user.entity.UserEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final long REFRESH_TOKEN_DURATION_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshTokenEntity create(UserEntity user) {

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(
                Instant.now().plus(REFRESH_TOKEN_DURATION_DAYS, ChronoUnit.DAYS)
        );
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshTokenEntity verify(String token) {

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new EntityNotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {

            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);

            throw new UnauthorizedException("Refresh token has expired.");
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
