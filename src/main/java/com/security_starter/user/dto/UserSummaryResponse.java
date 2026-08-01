package com.security_starter.user.dto;

public record UserSummaryResponse(

        Long id,
        String username,
        String email,
        boolean enabled

) {}