package com.security_starter.user.service;

import com.security_starter.common.exception.BadRequestException;
import com.security_starter.common.exception.ResourceNotFoundException;
import com.security_starter.common.util.SecurityUtils;
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

    @Override
    public UserResponse getCurrentUser() {

    	String email = SecurityUtils.getCurrentUserEmail();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        return userMapper.toResponse(user);
    }

    @Override
    public Page<UserSummaryResponse> getUsers(PageRequestDTO request) {
        Sort sort = request.direction().equalsIgnoreCase("DESC")
                ? Sort.by(request.sortBy()).descending()
                : Sort.by(request.sortBy()).ascending();


        PageRequest pageable = PageRequest.of(
                request.page(),
                request.size(),
                sort
        );
        return userRepository.findAll(pageable)
                .map(userMapper::toSummaryResponse);
    }
    
    @Override
    public UserResponse getUserById(Long id) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        return userMapper.toResponse(user);
    }
    
    @Override
    @Transactional
    public UserResponse updateUserRoles(Long id, UpdateUserRolesRequest request) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id));
        
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();

        if (user.getEmail().equals(currentUserEmail)) {
            throw new BadRequestException(
                    "You cannot modify your own roles."
            );
        }

        Set<RoleName> requestedRoles;

        try {
            requestedRoles = request.roles()
                    .stream()
                    .map(String::toUpperCase)
                    .map(RoleName::valueOf)
                    .collect(Collectors.toSet());

        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("One or more roles are invalid.");
        }

        List<RoleEntity> roles = roleRepository.findByNameIn(requestedRoles);

        if (roles.size() != requestedRoles.size()) {
            throw new BadRequestException(
                    "One or more roles do not exist."
            );
        }

        user.getRoles().clear();
        user.getRoles().addAll(roles);

        UserEntity updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }
    
    @Override
    @Transactional
    public UserResponse updateUserEnabled(Long id, UpdateUserStatusRequest request) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        ));
        
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();

        if (user.getEmail().equals(currentUserEmail)) {
            throw new BadRequestException(
                    "You cannot disable your own account."
            );
        }

        user.setEnabled(request.value());

        UserEntity updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserLock(Long id, UpdateUserStatusRequest request) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id));
        
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();

        if (user.getEmail().equals(currentUserEmail)) {
            throw new BadRequestException(
                    "You cannot lock your own account."
            );
        }

        user.setAccountNonLocked(request.value());

        return userMapper.toResponse(userRepository.save(user));
    }
}