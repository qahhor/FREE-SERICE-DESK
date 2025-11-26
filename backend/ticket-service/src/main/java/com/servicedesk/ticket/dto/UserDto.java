package com.servicedesk.ticket.dto;

import com.servicedesk.ticket.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private User.UserRole role;
    private User.UserStatus status;
    private String locale;
    private String timezone;
    private LocalDateTime lastLoginAt;
    private boolean emailVerified;
    private UUID teamId;
    private String teamName;
    private LocalDateTime createdAt;
}
