package com.servicedesk.channel.controller;

import com.servicedesk.channel.dto.ChannelDto;
import com.servicedesk.channel.entity.Channel;
import com.servicedesk.channel.repository.ChannelRepository;
import com.servicedesk.common.dto.ApiResponse;
import com.servicedesk.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelRepository channelRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<ChannelDto>>> getAllChannels() {
        List<ChannelDto> channels = channelRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ChannelDto>> getChannel(@PathVariable String id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));
        return ResponseEntity.ok(ApiResponse.success(toDto(channel)));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<ChannelDto>>> getChannelsByProject(@PathVariable String projectId) {
        List<ChannelDto> channels = channelRepository.findByProjectId(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<ChannelDto>>> getChannelsByType(@PathVariable Channel.ChannelType type) {
        List<ChannelDto> channels = channelRepository.findByType(type).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChannelDto>> createChannel(@RequestBody ChannelDto request) {
        Channel channel = Channel.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .projectId(request.getProjectId())
                .build();

        channel = channelRepository.save(channel);
        return ResponseEntity.ok(ApiResponse.success(toDto(channel)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChannelDto>> updateChannel(
            @PathVariable String id,
            @RequestBody ChannelDto request) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));

        if (request.getName() != null) channel.setName(request.getName());
        if (request.getDescription() != null) channel.setDescription(request.getDescription());
        if (request.getEnabled() != null) channel.setEnabled(request.getEnabled());

        channel = channelRepository.save(channel);
        return ResponseEntity.ok(ApiResponse.success(toDto(channel)));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChannelDto>> toggleChannel(@PathVariable String id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));

        channel.setEnabled(!channel.getEnabled());
        channel = channelRepository.save(channel);

        return ResponseEntity.ok(ApiResponse.success(toDto(channel)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteChannel(@PathVariable String id) {
        if (!channelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Channel not found");
        }
        channelRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private ChannelDto toDto(Channel channel) {
        return ChannelDto.builder()
                .id(channel.getId())
                .name(channel.getName())
                .type(channel.getType())
                .description(channel.getDescription())
                .enabled(channel.getEnabled())
                .projectId(channel.getProjectId())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .build();
    }
}
