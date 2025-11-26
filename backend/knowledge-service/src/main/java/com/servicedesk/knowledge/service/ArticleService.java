package com.servicedesk.knowledge.service;

import com.servicedesk.common.exception.ResourceNotFoundException;
import com.servicedesk.knowledge.document.ArticleDocument;
import com.servicedesk.knowledge.dto.ArticleDto;
import com.servicedesk.knowledge.dto.CreateArticleRequest;
import com.servicedesk.knowledge.dto.SearchRequest;
import com.servicedesk.knowledge.entity.Article;
import com.servicedesk.knowledge.entity.ArticleCategory;
import com.servicedesk.knowledge.repository.ArticleCategoryRepository;
import com.servicedesk.knowledge.repository.ArticleRepository;
import com.servicedesk.knowledge.repository.ArticleSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleCategoryRepository categoryRepository;
    private final ArticleSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Transactional
    public ArticleDto createArticle(CreateArticleRequest request, String authorId, String authorName) {
        String slug = request.getSlug() != null ? request.getSlug() : generateSlug(request.getTitle());

        // Ensure unique slug
        if (articleRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Article article = Article.builder()
                .title(request.getTitle())
                .slug(slug)
                .summary(request.getSummary())
                .content(request.getContent())
                .contentHtml(convertToHtml(request.getContent()))
                .authorId(authorId)
                .authorName(authorName)
                .status(Article.ArticleStatus.DRAFT)
                .locale(request.getLocale() != null ? request.getLocale() : "en")
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isInternal(request.getIsInternal() != null ? request.getIsInternal() : false)
                .projectId(request.getProjectId())
                .tags(request.getTags())
                .expiresAt(request.getExpiresAt())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .build();

        if (request.getCategoryId() != null) {
            ArticleCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
            article.setCategory(category);
        }

        article = articleRepository.save(article);

        // Index in Elasticsearch
        indexArticle(article);

        return toDto(article);
    }

    @Transactional
    public ArticleDto updateArticle(String id, CreateArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
            article.setContentHtml(convertToHtml(request.getContent()));
        }
        if (request.getCategoryId() != null) {
            ArticleCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
            article.setCategory(category);
        }
        if (request.getLocale() != null) {
            article.setLocale(request.getLocale());
        }
        if (request.getIsFeatured() != null) {
            article.setIsFeatured(request.getIsFeatured());
        }
        if (request.getIsInternal() != null) {
            article.setIsInternal(request.getIsInternal());
        }
        if (request.getTags() != null) {
            article.setTags(request.getTags());
        }
        if (request.getExpiresAt() != null) {
            article.setExpiresAt(request.getExpiresAt());
        }
        if (request.getMetaTitle() != null) {
            article.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            article.setMetaDescription(request.getMetaDescription());
        }
        if (request.getMetaKeywords() != null) {
            article.setMetaKeywords(request.getMetaKeywords());
        }

        article = articleRepository.save(article);

        // Re-index in Elasticsearch
        indexArticle(article);

        return toDto(article);
    }

    @Transactional
    public ArticleDto publishArticle(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        article.setStatus(Article.ArticleStatus.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());
        article = articleRepository.save(article);

        // Re-index in Elasticsearch
        indexArticle(article);

        return toDto(article);
    }

    @Transactional
    public ArticleDto archiveArticle(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        article.setStatus(Article.ArticleStatus.ARCHIVED);
        article = articleRepository.save(article);

        // Update in Elasticsearch
        indexArticle(article);

        return toDto(article);
    }

    public ArticleDto getArticle(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        return toDto(article);
    }

    public ArticleDto getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        return toDto(article);
    }

    @Transactional
    public ArticleDto viewArticle(String id) {
        articleRepository.incrementViewCount(id);
        return getArticle(id);
    }

    @Transactional
    public void markHelpful(String id, boolean helpful) {
        if (helpful) {
            articleRepository.incrementHelpfulCount(id);
        } else {
            articleRepository.incrementNotHelpfulCount(id);
        }
    }

    public Page<ArticleDto> getArticles(Pageable pageable) {
        return articleRepository.findAll(pageable).map(this::toDto);
    }

    public Page<ArticleDto> getPublishedArticles(String locale, Pageable pageable) {
        return articleRepository.findPublishedByLocale(locale, pageable).map(this::toDto);
    }

    public Page<ArticleDto> getArticlesByCategory(String categoryId, Pageable pageable) {
        return articleRepository.findByCategoryId(categoryId, pageable).map(this::toDto);
    }

    public List<ArticleDto> getFeaturedArticles(int limit) {
        return articleRepository.findFeaturedArticles(PageRequest.of(0, limit))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ArticleDto> getPopularArticles(int limit) {
        return articleRepository.findPopularArticles(PageRequest.of(0, limit))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ArticleDto> getRecentArticles(int limit) {
        return articleRepository.findRecentArticles(PageRequest.of(0, limit))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ArticleDto> search(SearchRequest request) {
        // Build Elasticsearch query
        co.elastic.clients.elasticsearch._types.query_dsl.Query boolQuery = buildSearchQuery(request);

        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(request.getPage(), request.getSize()))
                .build();

        SearchHits<ArticleDocument> hits = elasticsearchOperations.search(query, ArticleDocument.class);

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private co.elastic.clients.elasticsearch._types.query_dsl.Query buildSearchQuery(SearchRequest request) {
        return co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q
                .bool(b -> {
                    // Main search query
                    if (request.getQuery() != null && !request.getQuery().isEmpty()) {
                        b.must(m -> m
                                .multiMatch(mm -> mm
                                        .query(request.getQuery())
                                        .fields("title^3", "summary^2", "content", "tags^2")
                                        .fuzziness("AUTO")
                                )
                        );
                    }

                    // Status filter
                    b.filter(f -> f.term(t -> t.field("status").value("PUBLISHED")));

                    // Locale filter
                    if (request.getLocale() != null) {
                        b.filter(f -> f.term(t -> t.field("locale").value(request.getLocale())));
                    }

                    // Category filter
                    if (request.getCategoryId() != null) {
                        b.filter(f -> f.term(t -> t.field("categoryId").value(request.getCategoryId())));
                    }

                    // Project filter
                    if (request.getProjectId() != null) {
                        b.filter(f -> f.term(t -> t.field("projectId").value(request.getProjectId())));
                    }

                    // Internal filter
                    if (request.getIsInternal() != null) {
                        b.filter(f -> f.term(t -> t.field("isInternal").value(request.getIsInternal())));
                    }

                    // Tags filter
                    if (request.getTags() != null && !request.getTags().isEmpty()) {
                        for (String tag : request.getTags()) {
                            b.filter(f -> f.term(t -> t.field("tags").value(tag)));
                        }
                    }

                    return b;
                })
        );
    }

    @Async
    public void indexArticle(Article article) {
        try {
            ArticleDocument document = ArticleDocument.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .slug(article.getSlug())
                    .summary(article.getSummary())
                    .content(article.getContent())
                    .categoryId(article.getCategory() != null ? article.getCategory().getId() : null)
                    .categoryName(article.getCategory() != null ? article.getCategory().getName() : null)
                    .authorId(article.getAuthorId())
                    .authorName(article.getAuthorName())
                    .status(article.getStatus().name())
                    .viewCount(article.getViewCount())
                    .helpfulCount(article.getHelpfulCount())
                    .publishedAt(article.getPublishedAt())
                    .locale(article.getLocale())
                    .isFeatured(article.getIsFeatured())
                    .isInternal(article.getIsInternal())
                    .projectId(article.getProjectId())
                    .tags(article.getTags())
                    .createdAt(article.getCreatedAt())
                    .updatedAt(article.getUpdatedAt())
                    .build();

            searchRepository.save(document);
            log.debug("Indexed article: {}", article.getId());
        } catch (Exception e) {
            log.error("Failed to index article: {}", article.getId(), e);
        }
    }

    @Transactional
    public void deleteArticle(String id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found");
        }
        articleRepository.deleteById(id);
        searchRepository.deleteById(id);
    }

    private String generateSlug(String title) {
        String normalized = Normalizer.normalize(title.toLowerCase(Locale.ENGLISH), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-+", "-");
        return slug.replaceAll("^-|-$", "");
    }

    private String convertToHtml(String markdown) {
        // Simple markdown to HTML conversion
        // In production, use a proper markdown library like commonmark
        if (markdown == null) return null;
        return markdown
                .replaceAll("(?m)^### (.+)$", "<h3>$1</h3>")
                .replaceAll("(?m)^## (.+)$", "<h2>$1</h2>")
                .replaceAll("(?m)^# (.+)$", "<h1>$1</h1>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\*(.+?)\\*", "<em>$1</em>")
                .replaceAll("\\[(.+?)\\]\\((.+?)\\)", "<a href=\"$2\">$1</a>")
                .replaceAll("(?m)^- (.+)$", "<li>$1</li>")
                .replaceAll("(?m)^$", "<br/>");
    }

    private ArticleDto toDto(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .summary(article.getSummary())
                .content(article.getContent())
                .contentHtml(article.getContentHtml())
                .categoryId(article.getCategory() != null ? article.getCategory().getId() : null)
                .categoryName(article.getCategory() != null ? article.getCategory().getName() : null)
                .authorId(article.getAuthorId())
                .authorName(article.getAuthorName())
                .status(article.getStatus())
                .viewCount(article.getViewCount())
                .helpfulCount(article.getHelpfulCount())
                .notHelpfulCount(article.getNotHelpfulCount())
                .publishedAt(article.getPublishedAt())
                .expiresAt(article.getExpiresAt())
                .locale(article.getLocale())
                .isFeatured(article.getIsFeatured())
                .isInternal(article.getIsInternal())
                .projectId(article.getProjectId())
                .tags(article.getTags())
                .metaTitle(article.getMetaTitle())
                .metaDescription(article.getMetaDescription())
                .metaKeywords(article.getMetaKeywords())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    private ArticleDto toDto(ArticleDocument doc) {
        return ArticleDto.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .slug(doc.getSlug())
                .summary(doc.getSummary())
                .categoryId(doc.getCategoryId())
                .categoryName(doc.getCategoryName())
                .authorId(doc.getAuthorId())
                .authorName(doc.getAuthorName())
                .viewCount(doc.getViewCount())
                .helpfulCount(doc.getHelpfulCount())
                .publishedAt(doc.getPublishedAt())
                .locale(doc.getLocale())
                .isFeatured(doc.getIsFeatured())
                .isInternal(doc.getIsInternal())
                .projectId(doc.getProjectId())
                .tags(doc.getTags())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
