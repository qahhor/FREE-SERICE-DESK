package com.servicedesk.ticket.service;

import com.servicedesk.common.exception.BadRequestException;
import com.servicedesk.common.exception.ResourceNotFoundException;
import com.servicedesk.common.security.UserPrincipal;
import com.servicedesk.ticket.dto.UserDto;
import com.servicedesk.ticket.dto.auth.RegisterRequest;
import com.servicedesk.ticket.entity.User;
import com.servicedesk.ticket.mapper.UserMapper;
import com.servicedesk.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getStatus() == User.UserStatus.ACTIVE)
                .accountNonExpired(true)
                .accountNonLocked(user.getStatus() != User.UserStatus.SUSPENDED)
                .credentialsNonExpired(true)
                .roles(Collections.singleton(user.getRole().name()))
                .teamId(user.getTeam() != null ? user.getTeam().getId() : null)
                .build();
    }

    @Transactional
    public User createUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(User.UserRole.CUSTOMER)
                .status(User.UserStatus.ACTIVE)
                .locale(request.getLocale() != null ? request.getLocale() : "en")
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .emailVerified(false)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User createAgent(RegisterRequest request, User.UserRole role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .status(User.UserStatus.ACTIVE)
                .locale(request.getLocale() != null ? request.getLocale() : "en")
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Transactional(readOnly = true)
    public UserDto getUserDto(UUID id) {
        return userMapper.toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String search, Pageable pageable) {
        Page<User> users = search != null && !search.isEmpty()
                ? userRepository.searchUsers(search, pageable)
                : userRepository.findAll(pageable);

        return users.map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAllActiveAgents() {
        return userMapper.toDtoList(userRepository.findAllActiveAgents());
    }

    @Transactional(readOnly = true)
    public List<UserDto> findByTeam(UUID teamId) {
        return userMapper.toDtoList(userRepository.findByTeamId(teamId));
    }

    @Transactional
    public void updateLastLogin(UUID userId) {
        User user = findById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = findById(id);

        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getPhone() != null) {
            user.setPhone(userDto.getPhone());
        }
        if (userDto.getLocale() != null) {
            user.setLocale(userDto.getLocale());
        }
        if (userDto.getTimezone() != null) {
            user.setTimezone(userDto.getTimezone());
        }
        if (userDto.getAvatarUrl() != null) {
            user.setAvatarUrl(userDto.getAvatarUrl());
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findById(id);
        user.setDeleted(true);
        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);
    }
}
