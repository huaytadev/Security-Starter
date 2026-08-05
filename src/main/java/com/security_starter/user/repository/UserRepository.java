package com.security_starter.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.security_starter.role.entity.RoleName;
import com.security_starter.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    
    long countByRoles_Name(RoleName roleName);
    
    long countByRoles_NameAndEnabledTrueAndAccountNonLockedTrue(RoleName roleName);
}
