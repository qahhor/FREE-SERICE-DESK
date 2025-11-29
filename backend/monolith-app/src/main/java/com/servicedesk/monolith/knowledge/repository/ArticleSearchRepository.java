package com.servicedesk.monolith.knowledge.repository;

import com.servicedesk.monolith.knowledge.document.ArticleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleSearchRepository extends ElasticsearchRepository<ArticleDocument, String> {

    List<ArticleDocument> findByTitleContaining(String title);

    List<ArticleDocument> findByCategoryId(String categoryId);

    List<ArticleDocument> findByLocale(String locale);

    List<ArticleDocument> findByProjectId(String projectId);

    List<ArticleDocument> findByTagsContaining(String tag);

    List<ArticleDocument> findByStatusAndLocale(String status, String locale);
}
