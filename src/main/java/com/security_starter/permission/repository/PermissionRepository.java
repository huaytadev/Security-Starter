package com.security_starter.permission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.security_starter.permission.entity.PermissionEntity;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByName(String name);

    boolean existsByName(String name);
}
