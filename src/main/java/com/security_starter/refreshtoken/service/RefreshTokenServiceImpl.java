package com.security_starter.refreshtoken.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.security_starter.common.exception.UnauthorizedException;
import com.security_starter.refreshtoken.entity.RefreshTokenEntity;
import com.security_starter.refreshtoken.repository.RefreshTokenRepository;
import com.security_starter.user.entity.UserEntity;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

	private static final int TOKEN_BYTES = 32;
	
	@Value("${spring.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;
    
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public RefreshTokenEntity create(UserEntity user) {
        String familyId = UUID.randomUUID().toString();

        return createToken(user, familyId);
    }
    
    private RefreshTokenEntity createToken(UserEntity user, String familyId) {
    	String rawToken = generateSecureToken();
    	RefreshTokenEntity refreshToken = new RefreshTokenEntity();
    	refreshToken.setTokenHash(refreshTokenHasher.hash(rawToken));
    	
        refreshToken.setFamilyId(familyId);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
        refreshToken.setRevoked(false);
        
        refreshToken.setToken(rawToken);

        return refreshTokenRepository.save(refreshToken);
    }
    
    private String generateSecureToken() {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public RefreshTokenEntity verify(String token) {

    	String tokenHash = refreshTokenHasher.hash(token);
    	
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token."));
        
        if (refreshToken.isRevoked()) {
            refreshTokenRepository.revokeActiveByFamilyId(refreshToken.getFamilyId());

            throw new UnauthorizedException("Refresh token reuse detected.");
        }

        Instant now = Instant.now();
        
        if (refreshToken.getExpiresAt().isBefore(Instant.now())|| refreshToken.getExpiresAt().equals(now)) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.saveAndFlush(refreshToken);

            throw new UnauthorizedException("Invalid or expired refresh token.");
        }

        UserEntity user = refreshToken.getUser();


        if (!user.getEnabled() || !user.getAccountNonLocked()) {
        	refreshTokenRepository.revokeActiveByFamilyId(refreshToken.getFamilyId());

            throw new UnauthorizedException("User account is disabled or locked.");
        }

        return refreshToken;
    }
    
    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public RefreshTokenEntity rotate(String token) {
        RefreshTokenEntity currentToken = verify(token);
        currentToken.setRevoked(true);

        refreshTokenRepository.save(currentToken);

        return create(currentToken.getUser(), currentToken.getFamilyId());
    }

    @Override
    public void revoke(String token) {
        RefreshTokenEntity refreshToken = verify(token);

        refreshToken.setRevoked(true);
    }
    
    @Override
    @Transactional
    public void revoke(RefreshTokenEntity refreshToken) {
        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAll(UserEntity user) {
        List<RefreshTokenEntity> refreshTokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        refreshTokens.forEach(token -> token.setRevoked(true));
        
        refreshTokenRepository.saveAll(refreshTokens);
    }
    
    private RefreshTokenEntity create(UserEntity user, String familyId) {

        String rawToken = generateToken();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();

        /*
         * Raw token is kept only in memory.
         */
        refreshToken.setToken(rawToken);

        /*
         * Only the SHA-256 hash is persisted.
         */
        refreshToken.setTokenHash(refreshTokenHasher.hash(rawToken));

        refreshToken.setFamilyId(familyId);
        refreshToken.setUser(user);

        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));

        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}

    private UnauthorizedException invalidRefreshToken() {
        return new UnauthorizedException("Invalid or expired refresh token.");
    }
}
