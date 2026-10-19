package com.security_starter.refreshtoken.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.security_starter.refreshtoken.entity.RefreshTokenEntity;
import com.security_starter.user.entity.UserEntity;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    		SELECT token FROM RefreshTokenEntity token
    		WHERE token.tokenHash = :tokenHash
    		""")
	Optional<RefreshTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<RefreshTokenEntity> findAllByUserAndRevokedFalse(UserEntity user);

    @Modifying
    @Query("""
    		UPDATE RefreshTokenEntity token
    		SET token.revoked = true
    		WHERE token.familyId = :familyId AND token.revoked = false
    		""")
    int revokeActiveByFamilyId(@Param("familyId") String familyId);
    
    void deleteAllByUser(UserEntity user);
}
