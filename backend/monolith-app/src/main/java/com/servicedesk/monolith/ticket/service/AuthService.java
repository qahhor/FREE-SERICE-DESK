package com.servicedesk.monolith.ticket.service;

import com.servicedesk.common.exception.BadRequestException;
import com.servicedesk.common.exception.UnauthorizedException;
import com.servicedesk.common.security.JwtTokenProvider;
import com.servicedesk.monolith.ticket.dto.UserDto;
import com.servicedesk.monolith.ticket.dto.auth.AuthResponse;
import com.servicedesk.monolith.ticket.dto.auth.LoginRequest;
import com.servicedesk.monolith.ticket.dto.auth.RefreshTokenRequest;
import com.servicedesk.monolith.ticket.dto.auth.RegisterRequest;
import com.servicedesk.monolith.ticket.entity.User;
import com.servicedesk.monolith.ticket.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication.getName());

        User user = userService.findByEmail(request.getEmail());
        userService.updateLastLogin(user.getId());

        UserDto userDto = userMapper.toDto(user);

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.of(accessToken, refreshToken, tokenProvider.getExpirationTime(), userDto);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.createUser(request);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication.getName());

        userService.updateLastLogin(user.getId());

        UserDto userDto = userMapper.toDto(user);

        log.info("New user registered: {}", user.getEmail());

        return AuthResponse.of(accessToken, refreshToken, tokenProvider.getExpirationTime(), userDto);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Token is not a refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userService.findByEmail(username);
        UserDto userDto = userMapper.toDto(user);

        Authentication authentication = tokenProvider.getAuthentication(refreshToken);
        String newAccessToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        return AuthResponse.of(newAccessToken, newRefreshToken, tokenProvider.getExpirationTime(), userDto);
    }

    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);
        return userMapper.toDto(user);
    }
}
