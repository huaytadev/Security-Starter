package com.security_starter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.security_starter.permission.entity.PermissionEntity;
import com.security_starter.permission.repository.PermissionRepository;
import com.security_starter.role.entity.RoleEntity;
import com.security_starter.role.entity.RoleName;
import com.security_starter.role.repository.RoleRepository;
import com.security_starter.user.entity.UserEntity;
import com.security_starter.user.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createPermissions();
        createRoles();
        createAdminUser();
    }

    private void createPermissions() {
        List<String> permissions = List.of(
                "user:read",
                "user:create",
                "user:update",
                "user:delete",
                "role:read",
                "role:create",
                "role:update",
                "role:delete",
                "permission:read",
                "permission:create",
                "permission:update",
                "permission:delete"
        );

        for (String permissionName : permissions) {
            if (!permissionRepository.existsByName(permissionName)) {
                PermissionEntity permission = new PermissionEntity();
                permission.setName(permissionName);
                permission.setDescription("Permission for " + permissionName);
                permissionRepository.save(permission);
            }
        }
    }

    private void createRoles() {
        createOrUpdateRole(
                RoleName.USER,
                "Basic user role",
                Set.of(
                        getPermission("user:read")
                )
        );

        createOrUpdateRole(
                RoleName.MODERATOR,
                "Moderator role",
                Set.of(
                        getPermission("user:read"),
                        getPermission("user:update")
                )
        );

        createOrUpdateRole(
                RoleName.ADMIN,
                "Administrator role",
                Set.copyOf(permissionRepository.findAll())
        );
    }
    
    private RoleEntity createOrUpdateRole(
            RoleName roleName,
            String description,
            Set<PermissionEntity> permissions
    ) {

        RoleEntity role = roleRepository.findByName(roleName)
                .orElseGet(RoleEntity::new);

        role.setName(roleName);
        role.setDescription(description);
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }


    private PermissionEntity getPermission(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Permission not found: " + name
                ));
    }

    private void createAdminUser() {
        String adminEmail = "admin@local.com";

        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        RoleEntity adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "ADMIN role not found"
                ));

        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("Admin123!"));
        admin.getRoles().add(adminRole);

        userRepository.save(admin);
    }
}
