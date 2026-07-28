package com.security_starter.refreshtoken.service;

import com.security_starter.refreshtoken.entity.RefreshTokenEntity;
import com.security_starter.user.entity.UserEntity;

public interface RefreshTokenService {

    RefreshTokenEntity create(UserEntity user);

    RefreshTokenEntity verify(String token);

    RefreshTokenEntity rotate(String token);
    
    void revoke(String token);

    void revokeAll(UserEntity user);
}