package com.servicedesk.knowledge.controller;

import com.servicedesk.common.dto.ApiResponse;
import com.servicedesk.common.dto.PageResponse;
import com.servicedesk.common.security.SecurityUtils;
import com.servicedesk.knowledge.dto.ArticleDto;
import com.servicedesk.knowledge.dto.CreateArticleRequest;
import com.servicedesk.knowledge.dto.SearchRequest;
import com.servicedesk.knowledge.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ArticleDto>>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Page<ArticleDto> articles = articleService.getArticles(PageRequest.of(page, size, sort));

        PageResponse<ArticleDto> response = new PageResponse<>(
                articles.getContent(),
                articles.getNumber(),
                articles.getSize(),
                articles.getTotalElements(),
                articles.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<PageResponse<ArticleDto>>> getPublishedArticles(
            @RequestParam(defaultValue = "en") String locale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ArticleDto> articles = articleService.getPublishedArticles(locale, PageRequest.of(page, size));

        PageResponse<ArticleDto> response = new PageResponse<>(
                articles.getContent(),
                articles.getNumber(),
                articles.getSize(),
                articles.getTotalElements(),
                articles.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDto>> getArticle(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(articleService.getArticle(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ArticleDto>> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(articleService.getArticleBySlug(slug)));
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<ApiResponse<ArticleDto>> viewArticle(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(articleService.viewArticle(id)));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<Void>> submitFeedback(
            @PathVariable String id,
            @RequestParam boolean helpful) {
        articleService.markHelpful(id, helpful);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ArticleDto>>> getFeaturedArticles(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(articleService.getFeaturedArticles(limit)));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ArticleDto>>> getPopularArticles(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(articleService.getPopularArticles(limit)));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ArticleDto>>> getRecentArticles(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(articleService.getRecentArticles(limit)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<ArticleDto>>> getArticlesByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ArticleDto> articles = articleService.getArticlesByCategory(
                categoryId, PageRequest.of(page, size));

        PageResponse<ArticleDto> response = new PageResponse<>(
                articles.getContent(),
                articles.getNumber(),
                articles.getSize(),
                articles.getTotalElements(),
                articles.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<ArticleDto>>> search(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(articleService.search(request)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<ArticleDto>> createArticle(
            @Valid @RequestBody CreateArticleRequest request) {
        String authorId = SecurityUtils.getCurrentUserId();
        String authorName = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(ApiResponse.success(
                articleService.createArticle(request, authorId, authorName)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<ArticleDto>> updateArticle(
            @PathVariable String id,
            @Valid @RequestBody CreateArticleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(articleService.updateArticle(id, request)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ArticleDto>> publishArticle(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(articleService.publishArticle(id)));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ArticleDto>> archiveArticle(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(articleService.archiveArticle(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable String id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
