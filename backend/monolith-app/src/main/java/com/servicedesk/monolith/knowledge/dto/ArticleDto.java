package com.servicedesk.monolith.knowledge.dto;

import com.servicedesk.monolith.knowledge.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDto {
    private String id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String contentHtml;
    private String categoryId;
    private String categoryName;
    private String authorId;
    private String authorName;
    private Article.ArticleStatus status;
    private Long viewCount;
    private Long helpfulCount;
    private Long notHelpfulCount;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private String locale;
    private Boolean isFeatured;
    private Boolean isInternal;
    private String projectId;
    private Set<String> tags;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
