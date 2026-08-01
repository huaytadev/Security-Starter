package com.security_starter.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.security_starter.user.dto.PageRequestDTO;
import com.security_starter.user.dto.UpdateUserRolesRequest;
import com.security_starter.user.dto.UpdateUserStatusRequest;
import com.security_starter.user.dto.UserResponse;
import com.security_starter.user.dto.UserSummaryResponse;

public interface UserService {

    UserResponse getCurrentUser();

    Page<UserSummaryResponse> getUsers(PageRequestDTO request);
    
    UserResponse getUserById(Long id);
    
    UserResponse updateUserRoles(Long id, UpdateUserRolesRequest request);
    
    UserResponse updateUserEnabled(Long id, UpdateUserStatusRequest request);
    
    UserResponse updateUserLock(Long id, UpdateUserStatusRequest request);
}
