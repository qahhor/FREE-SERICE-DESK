package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_rcas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemRca extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private RcaTemplate template;

    @Column(name = "analysis_data", columnDefinition = "jsonb")
    private String analysisData; // JSON

    @Column(columnDefinition = "TEXT")
    private String findings;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducted_by_id")
    private User conductedBy;

    @Column(name = "conducted_at")
    private LocalDateTime conductedAt;
}
