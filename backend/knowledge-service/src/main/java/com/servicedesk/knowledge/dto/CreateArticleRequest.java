package com.servicedesk.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CreateArticleRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String slug;

    private String summary;

    @NotBlank(message = "Content is required")
    private String content;

    private String categoryId;

    private String locale = "en";

    private Boolean isFeatured = false;

    private Boolean isInternal = false;

    private String projectId;

    private Set<String> tags;

    private LocalDateTime expiresAt;

    private String metaTitle;

    private String metaDescription;

    private String metaKeywords;
}
