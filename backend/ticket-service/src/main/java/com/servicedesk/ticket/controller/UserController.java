package com.servicedesk.ticket.controller;

import com.servicedesk.common.dto.ApiResponse;
import com.servicedesk.common.dto.PageResponse;
import com.servicedesk.ticket.dto.UserDto;
import com.servicedesk.ticket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get User", description = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable UUID id) {
        UserDto user = userService.getUserDto(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    @Operation(summary = "List Users", description = "Get paginated list of users with search")
    public ResponseEntity<ApiResponse<Page<UserDto>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDto> users = userService.searchUsers(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/agents")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    @Operation(summary = "List Agents", description = "Get all active agents")
    public ResponseEntity<ApiResponse<List<UserDto>>> listAgents() {
        List<UserDto> agents = userService.findAllActiveAgents();
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    @Operation(summary = "List Team Members", description = "Get all users in a team")
    public ResponseEntity<ApiResponse<List<UserDto>>> listTeamMembers(@PathVariable UUID teamId) {
        List<UserDto> members = userService.findByTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update User", description = "Update user profile")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable UUID id,
            @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete User", description = "Soft delete a user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
