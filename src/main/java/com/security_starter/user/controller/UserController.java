package com.security_starter.user.controller;

import com.security_starter.common.response.ApiResponse;
import com.security_starter.user.dto.PageRequestDTO;
import com.security_starter.user.dto.UpdateUserRolesRequest;
import com.security_starter.user.dto.UpdateUserStatusRequest;
import com.security_starter.user.dto.UserResponse;
import com.security_starter.user.dto.UserSummaryResponse;
import com.security_starter.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    @PreAuthorize(
            "hasAuthority(T(com.security_starter.common.constants.Permissions).USER_READ)"
    )
    public UserResponse me() {
        return userService.getCurrentUser();
    }

    @GetMapping
    @Operation(summary = "Get paginated list of users")
    @PreAuthorize(
            "hasAuthority(T(com.security_starter.common.constants.Permissions).USER_READ)"
    )
    public ApiResponse<Page<UserSummaryResponse>> getUsers(
            @Valid @ModelAttribute @ParameterObject PageRequestDTO request
    ) {

        return ApiResponse.success(
                "Users retrieved successfully",
                userService.getUsers(request)
        );
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    @PreAuthorize(
            "hasAuthority(T(com.security_starter.common.constants.Permissions).USER_READ)"
    )
    public ApiResponse<UserResponse> getUserById(
            @Parameter(description = "User identifier")
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "User retrieved successfully",
                userService.getUserById(id)
        );
    }
    
    @PatchMapping("/{id}/roles")
    @Operation(summary = "Update user roles")
    @PreAuthorize(
            "hasAuthority(T(com.security_starter.common.constants.Permissions).USER_UPDATE)"
    )
    public ApiResponse<UserResponse> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {

        return ApiResponse.success(
                "User roles updated successfully",
                userService.updateUserRoles(id, request)
        );
    }
    
    @PatchMapping("/{id}/enabled")
    @Operation(summary = "Enable or disable a user")
    @PreAuthorize(
            "hasAuthority(T(com.security_starter.common.constants.Permissions).USER_UPDATE)"
    )
    public ApiResponse<UserResponse> updateUserEnabled(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {

        return ApiResponse.success(
                "User status updated successfully",
                userService.updateUserEnabled(id, request)
        );
    }
    
    @PatchMapping("/{id}/lock")
    @Operation(summary = "Lock or unlock a user account")
    @PreAuthorize(
            "hasAuthority(T(com.security_starter.common.constants.Permissions).USER_UPDATE)"
    )
    public ApiResponse<UserResponse> updateUserLock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {

        return ApiResponse.success(
                "User lock status updated successfully",
                userService.updateUserLock(id, request)
        );
    }
}
