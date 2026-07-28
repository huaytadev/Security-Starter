package com.security_starter.refreshtoken.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.security_starter.refreshtoken.entity.RefreshTokenEntity;
import com.security_starter.user.entity.UserEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    List<RefreshTokenEntity> findAllByUser(UserEntity user);

    List<RefreshTokenEntity> findAllByUserAndRevokedFalse(UserEntity user);

    void deleteAllByUser(UserEntity user);
}
