package com.security_starter.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.security_starter.role.entity.RoleEntity;
import com.security_starter.role.entity.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(RoleName name);

    boolean existsByName(RoleName name);
}