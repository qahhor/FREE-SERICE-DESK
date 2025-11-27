package com.servicedesk.knowledge.repository;

import com.servicedesk.knowledge.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {

    Optional<Article> findBySlug(String slug);

    Page<Article> findByStatus(Article.ArticleStatus status, Pageable pageable);

    Page<Article> findByCategoryId(String categoryId, Pageable pageable);

    Page<Article> findByProjectId(String projectId, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' AND a.isFeatured = true ORDER BY a.publishedAt DESC")
    List<Article> findFeaturedArticles(Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' AND a.isInternal = false ORDER BY a.viewCount DESC")
    List<Article> findPopularArticles(Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    List<Article> findRecentArticles(Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' AND a.locale = :locale")
    Page<Article> findPublishedByLocale(String locale, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.authorId = :authorId")
    Page<Article> findByAuthorId(String authorId, Pageable pageable);

    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(String id);

    @Modifying
    @Query("UPDATE Article a SET a.helpfulCount = a.helpfulCount + 1 WHERE a.id = :id")
    void incrementHelpfulCount(String id);

    @Modifying
    @Query("UPDATE Article a SET a.notHelpfulCount = a.notHelpfulCount + 1 WHERE a.id = :id")
    void incrementNotHelpfulCount(String id);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(a) FROM Article a WHERE a.categoryId = :categoryId")
    long countByCategory(String categoryId);

    @Query("SELECT DISTINCT a.locale FROM Article a")
    List<String> findDistinctLocales();
}
