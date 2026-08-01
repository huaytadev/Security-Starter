package com.security_starter.user.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.security_starter.user.dto.UserResponse;
import com.security_starter.user.dto.UserSummaryResponse;
import com.security_starter.user.entity.UserEntity;

@Component
public class UserMapper {

    public UserResponse toResponse(UserEntity user) {

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
                        .stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }

    public UserSummaryResponse toSummaryResponse(UserEntity user) {

        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getEnabled()
        );
    }

}