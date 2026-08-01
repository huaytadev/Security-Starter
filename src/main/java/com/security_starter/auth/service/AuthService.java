package com.security_starter.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.security_starter.auth.dto.AuthResponse;
import com.security_starter.auth.dto.LoginRequest;
import com.security_starter.auth.dto.LogoutRequest;
import com.security_starter.auth.dto.RefreshTokenRequest;
import com.security_starter.auth.dto.RegisterRequest;
import com.security_starter.auth.mapper.AuthMapper;
import com.security_starter.common.exception.BadRequestException;
import com.security_starter.common.exception.ForbiddenException;
import com.security_starter.common.util.SecurityUtils;
import com.security_starter.refreshtoken.entity.RefreshTokenEntity;
import com.security_starter.refreshtoken.service.RefreshTokenService;
import com.security_starter.role.entity.RoleEntity;
import com.security_starter.role.entity.RoleName;
import com.security_starter.role.repository.RoleRepository;
import com.security_starter.security.jwt.JwtService;
import com.security_starter.security.userdetails.CustomUserDetails;
import com.security_starter.user.entity.UserEntity;
import com.security_starter.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username is already taken");
        }

        RoleEntity userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException(
                        "Default USER role not found"
                ));

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.getRoles().add(userRole);

        UserEntity savedUser = userRepository.save(user);

        return authMapper.toRegisterResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = userDetails.getUser();

        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshTokenEntity refreshToken = refreshTokenService.create(user);

        return authMapper.toLoginResponse(
                user,
                accessToken,
                refreshToken
        );
    }
    
    public AuthResponse refresh(RefreshTokenRequest request) {

    	RefreshTokenEntity rotatedToken =
    	        refreshTokenService.rotate(request.refreshToken());

    	UserEntity user = rotatedToken.getUser();

    	String accessToken = jwtService.generateAccessToken(user);

    	return authMapper.toRefreshResponse(
    	        user,
    	        accessToken,
    	        rotatedToken
    	);
    }
    
    public void logout(LogoutRequest request) {

    	String email = SecurityUtils.getCurrentUserEmail();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user not found.")
                );

        RefreshTokenEntity refreshToken =
                refreshTokenService.verify(request.refreshToken());

        if (!refreshToken.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(
                    "Refresh token does not belong to authenticated user"
            );
        }

        refreshTokenService.revoke(request.refreshToken());
    }
}
