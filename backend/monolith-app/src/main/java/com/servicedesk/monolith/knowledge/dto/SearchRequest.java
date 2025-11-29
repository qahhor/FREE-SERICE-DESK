package com.servicedesk.monolith.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {
    private String query;
    private String categoryId;
    private String locale = "en";
    private Set<String> tags;
    private String projectId;
    private Boolean isInternal;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "relevance";
    private String sortOrder = "desc";
    private Boolean useSemanticSearch = false;
}
