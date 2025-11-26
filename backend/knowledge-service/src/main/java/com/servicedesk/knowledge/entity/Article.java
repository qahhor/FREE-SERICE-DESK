package com.servicedesk.knowledge.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ArticleCategory category;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "author_name")
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "helpful_count")
    private Long helpfulCount = 0L;

    @Column(name = "not_helpful_count")
    private Long notHelpfulCount = 0L;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private String locale = "en";

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_internal")
    private Boolean isInternal = false;

    @Column(name = "project_id")
    private String projectId;

    @ElementCollection
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @Column(name = "meta_keywords")
    private String metaKeywords;

    public enum ArticleStatus {
        DRAFT,
        PENDING_REVIEW,
        PUBLISHED,
        ARCHIVED
    }
}
