package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ticket_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private CommentType type = CommentType.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20)
    private Ticket.TicketChannel channel;

    @Column(name = "external_id")
    private String externalId;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TicketAttachment> attachments = new ArrayList<>();

    public void addAttachment(TicketAttachment attachment) {
        attachments.add(attachment);
        attachment.setComment(this);
    }

    public enum CommentType {
        PUBLIC,      // Visible to customer
        INTERNAL,    // Only visible to agents
        SYSTEM       // System-generated messages
    }
}
