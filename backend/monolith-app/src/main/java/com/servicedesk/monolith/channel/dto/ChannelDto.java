package com.servicedesk.monolith.channel.dto;

import com.servicedesk.monolith.channel.entity.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelDto {
    private String id;
    private String name;
    private Channel.ChannelType type;
    private String description;
    private Boolean enabled;
    private String projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
