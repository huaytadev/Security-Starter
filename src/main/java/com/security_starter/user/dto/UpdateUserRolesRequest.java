package com.security_starter.user.dto;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserRolesRequest(

        @NotEmpty(message = "At least one role is required")
        Set<String> roles

) {}