package com.security_starter.user.dto;

import java.util.Set;

public record UserResponse(

        Long id,

        String username,

        String email,

        Set<String> roles

) {
}
