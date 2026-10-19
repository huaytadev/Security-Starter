package com.security_starter.refreshtoken.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import com.security_starter.user.entity.BaseAuditableEntity;
import com.security_starter.user.entity.UserEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends BaseAuditableEntity {

    @Column(name="token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    
    @Column(name="family_id", nullable = false, length = 36)
    private String familyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;
    
    /*
     * The raw refresh token is never persisted.
     * It only exists in memory so it can be returned
     * to the client after creation/rotation.
     */
    @Transient
    private String token;
}
