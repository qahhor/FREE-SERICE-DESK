package com.servicedesk.monolith.knowledge.repository;

import com.servicedesk.monolith.knowledge.entity.ArticleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleCategoryRepository extends JpaRepository<ArticleCategory, String> {

    Optional<ArticleCategory> findBySlug(String slug);

    List<ArticleCategory> findByParentIsNullOrderBySortOrderAsc();

    List<ArticleCategory> findByParentIdOrderBySortOrderAsc(String parentId);

    List<ArticleCategory> findByProjectIdOrderBySortOrderAsc(String projectId);

    List<ArticleCategory> findByEnabledTrueOrderBySortOrderAsc();

    @Query("SELECT c FROM ArticleCategory c WHERE c.parent IS NULL AND c.enabled = true ORDER BY c.sortOrder ASC")
    List<ArticleCategory> findRootCategories();

    @Query("SELECT c FROM ArticleCategory c WHERE c.locale = :locale AND c.enabled = true ORDER BY c.sortOrder ASC")
    List<ArticleCategory> findByLocale(String locale);

    boolean existsBySlug(String slug);
}
