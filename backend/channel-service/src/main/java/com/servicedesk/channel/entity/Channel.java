package com.servicedesk.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(columnDefinition = "jsonb")
    private String configuration;

    @Column(name = "project_id")
    private String projectId;

    public enum ChannelType {
        EMAIL,
        TELEGRAM,
        WHATSAPP,
        WEB_WIDGET,
        PHONE,
        SMS,
        SLACK,
        MICROSOFT_TEAMS
    }
}
