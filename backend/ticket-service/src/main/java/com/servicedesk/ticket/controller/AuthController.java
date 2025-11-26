package com.servicedesk.ticket.controller;

import com.servicedesk.common.dto.ApiResponse;
import com.servicedesk.ticket.dto.UserDto;
import com.servicedesk.ticket.dto.auth.AuthResponse;
import com.servicedesk.ticket.dto.auth.LoginRequest;
import com.servicedesk.ticket.dto.auth.RefreshTokenRequest;
import com.servicedesk.ticket.dto.auth.RegisterRequest;
import com.servicedesk.ticket.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and get JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Get the currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        UserDto user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout the current user")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // In a stateless JWT setup, logout is handled client-side
        // For server-side token invalidation, implement token blacklist
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }
}
