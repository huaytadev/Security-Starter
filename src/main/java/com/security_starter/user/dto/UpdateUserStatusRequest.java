package com.security_starter.user.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

        @NotNull(message = "Value is required")
        Boolean value

) {}
