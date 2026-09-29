package com.security_starter.config;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.security_starter.permission.entity.PermissionEntity;
import com.security_starter.permission.repository.PermissionRepository;
import com.security_starter.role.entity.RoleEntity;
import com.security_starter.role.entity.RoleName;
import com.security_starter.role.repository.RoleRepository;
import com.security_starter.user.entity.AuthProvider;
import com.security_starter.user.entity.UserEntity;
import com.security_starter.user.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${ADMIN_USERNAME}")
    private String adminUsername;
    @Value("${ADMIN_EMAIL}")
    private String adminEmail;
    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        initializePermissions();
        initializeRoles();
        initializeAdminUser();
    }

    private void initializePermissions() {
        createPermissionIfNotExists("USER_READ", "Allows reading user information");
        createPermissionIfNotExists("USER_WRITE", "Allows creating and updating users");
        createPermissionIfNotExists("USER_DELETE", "Allows deleting users");
        
        createPermissionIfNotExists("ROLE_READ", "Allows reading roles");
        createPermissionIfNotExists("ROLE_WRITE", "Allows creating and updating roles");
        createPermissionIfNotExists("ROLE_DELETE", "Allows deleting roles");
    }

    private void initializeRoles() {
        PermissionEntity userRead = getPermission("USER_READ");
        PermissionEntity userWrite = getPermission("USER_WRITE");
        PermissionEntity userDelete = getPermission("USER_DELETE");

        PermissionEntity roleRead = getPermission("ROLE_READ");
        PermissionEntity roleWrite = getPermission("ROLE_WRITE");
        PermissionEntity roleDelete = getPermission("ROLE_DELETE");

        Set<PermissionEntity> userPermissions = new HashSet<>();
        userPermissions.add(userRead);

        createRoleIfNotExists(
                RoleName.USER,
                "Standard application user",
                userPermissions
        );

        Set<PermissionEntity> moderatorPermissions = new HashSet<>();
        moderatorPermissions.add(userRead);
        moderatorPermissions.add(userWrite);
        moderatorPermissions.add(roleRead);

        createRoleIfNotExists(
                RoleName.MODERATOR,
                "Application moderator",
                moderatorPermissions
        );

        Set<PermissionEntity> adminPermissions = new HashSet<>();
        adminPermissions.add(userRead);
        adminPermissions.add(userWrite);
        adminPermissions.add(userDelete);
        adminPermissions.add(roleRead);
        adminPermissions.add(roleWrite);
        adminPermissions.add(roleDelete);

        createRoleIfNotExists(
        		RoleName.ADMIN,
                "Application administrator",
                adminPermissions
        );
    }
    
    private void  initializeAdminUser() {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        RoleEntity adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Required ADMIN role was not initialized"
                		));

        UserEntity admin = new UserEntity();

        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEnabled(true);
        admin.setAccountNonLocked(true);
        admin.setProvider(AuthProvider.LOCAL);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
    }
    
    private PermissionEntity getPermission(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Required permission was not initialized: " + name
                        ));
    }


    private void createPermissionIfNotExists(String name, String description) {
        if (permissionRepository.findByName(name).isEmpty()) {
            PermissionEntity permission = new PermissionEntity();

            permission.setName(name);
            permission.setDescription(description);

            permissionRepository.save(permission);
        }
    }

    private void createRoleIfNotExists(RoleName name, String description, Set<PermissionEntity> permissions) {
        RoleEntity role = roleRepository.findByName(name)
                .orElseGet(() -> {
                	RoleEntity newRole = new RoleEntity();

                    newRole.setName(name);
                    newRole.setDescription(description);
                    newRole.setPermissions(new HashSet<>());

                    return newRole;
                    });

        role.getPermissions().addAll(permissions);

        roleRepository.save(role);
    }
}
