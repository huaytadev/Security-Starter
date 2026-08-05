package com.security_starter.user.service;

import com.security_starter.common.exception.BadRequestException;
import com.security_starter.common.exception.ResourceNotFoundException;
import com.security_starter.common.util.SecurityUtils;
import com.security_starter.refreshtoken.service.RefreshTokenService;
import com.security_starter.role.entity.RoleEntity;
import com.security_starter.role.entity.RoleName;
import com.security_starter.role.repository.RoleRepository;
import com.security_starter.user.dto.PageRequestDTO;
import com.security_starter.user.dto.UpdateUserRolesRequest;
import com.security_starter.user.dto.UpdateUserStatusRequest;
import com.security_starter.user.dto.UserResponse;
import com.security_starter.user.dto.UserSummaryResponse;
import com.security_starter.user.entity.UserEntity;
import com.security_starter.user.mapper.UserMapper;
import com.security_starter.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "username",
            "email",
            "createdAt",
            "updatedAt",
            "enabled"
    );

    @Override
    public UserResponse getCurrentUser() {

        return userMapper.toResponse(
                SecurityUtils.getCurrentUser()
        );
    }

    @Override
    public Page<UserSummaryResponse> getUsers(PageRequestDTO request) {
    	
        return userRepository.findAll(buildPageRequest(request))
                .map(userMapper::toSummaryResponse);
    }
    
    @Override
    public UserResponse getUserById(Long id) {

    	return userMapper.toResponse(findUserOrThrow(id));
    }
    
    @Override
    @Transactional
    public UserResponse updateUserRoles(Long id, UpdateUserRolesRequest request) {

        UserEntity user = findUserOrThrow(id);

        validateNotSelf(user, "You cannot modify your own roles.");

        Set<RoleEntity> roles = resolveRoles(request.roles());

        validateLastAdmin(
                user,
                hasAdminRole(roles),
                user.getEnabled(),
                user.getAccountNonLocked()
        );
        
        user.getRoles().clear();
        user.getRoles().addAll(roles);

        return userMapper.toResponse(userRepository.save(user));
    }
    
    @Override
    @Transactional
    public UserResponse updateUserEnabled(Long id, UpdateUserStatusRequest request) {

    	UserEntity user = findUserOrThrow(id);

        validateNotSelf(user, "You cannot disable your own account.");

        validateLastAdmin(
                user,
                hasAdminRole(user.getRoles()),
                request.value(),
                user.getAccountNonLocked()
        );
        
        user.setEnabled(request.value());
        
        if (!request.value()) {
            refreshTokenService.revokeAll(user);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUserLock(Long id, UpdateUserStatusRequest request) {

    	UserEntity user = findUserOrThrow(id);

        validateNotSelf(user,"You cannot lock your own account.");
        
        validateLastAdmin(
                user,
                hasAdminRole(user.getRoles()),
                user.getEnabled(),
                request.value()
        );
        
        user.setAccountNonLocked(request.value());
        
        if (!request.value()) {
            refreshTokenService.revokeAll(user);
        }

        return userMapper.toResponse(userRepository.save(user));
    }
    
    
    /*Private Methods*/
    
    private UserEntity findUserOrThrow(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        ));
    }

    private void validateNotSelf(UserEntity user, String message) {

        if (user.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BadRequestException(message);
        }
    }

    private Set<RoleEntity> resolveRoles(Set<String> roleNames) {

        Set<RoleName> requestedRoles;

        try {

            requestedRoles = roleNames.stream()
                    .map(String::toUpperCase)
                    .map(RoleName::valueOf)
                    .collect(Collectors.toSet());

        } catch (IllegalArgumentException ex) {

            throw new BadRequestException("One or more roles are invalid.");
        }

        List<RoleEntity> roles =roleRepository.findByNameIn(requestedRoles);

        if (roles.size() != requestedRoles.size()) {
            throw new BadRequestException("One or more roles do not exist.");
        }

        return Set.copyOf(roles);
    }

    private PageRequest buildPageRequest(PageRequestDTO request) {

        if (!ALLOWED_SORT_FIELDS.contains(request.sortBy())) {
            throw new BadRequestException(
                    "Invalid sort field: " + request.sortBy()
            );
        }

        Sort.Direction direction =
                Sort.Direction.fromOptionalString(request.direction())
                        .orElse(Sort.Direction.ASC);

        return PageRequest.of(
                request.page(),
                request.size(),
                Sort.by(direction, request.sortBy())
        );
    }
    
    private void validateLastAdmin(
    		UserEntity user,
            Boolean willRemainAdmin,
            Boolean willRemainEnabled,
            Boolean willRemainUnlocked
        ) {

        boolean isCurrentlyActiveAdmin =
                hasAdminRole(user.getRoles())
                && user.getEnabled()
                && user.getAccountNonLocked();

        if (!isCurrentlyActiveAdmin) {
            return;
        }

        boolean willStillBeActiveAdmin = 
        		willRemainAdmin 
        		&& willRemainEnabled 
        		&& willRemainUnlocked;

        if (willStillBeActiveAdmin) {
            return;
        }

        long activeAdmins = userRepository
                .countByRoles_NameAndEnabledTrueAndAccountNonLockedTrue(RoleName.ADMIN);

        if (activeAdmins <= 1) {
            throw new BadRequestException(
                    "The last active administrator cannot lose administrative access."
            );
        }
    }

    private boolean hasAdminRole(Set<RoleEntity> roles) {

        return roles.stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN);
    }

}