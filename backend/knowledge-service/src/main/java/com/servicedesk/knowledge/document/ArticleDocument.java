package com.servicedesk.knowledge.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.Set;

@Document(indexName = "articles")
@Setting(settingPath = "/elasticsearch/article-settings.json")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String summary;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Text)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String authorId;

    @Field(type = FieldType.Text)
    private String authorName;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Long)
    private Long helpfulCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime publishedAt;

    @Field(type = FieldType.Keyword)
    private String locale;

    @Field(type = FieldType.Boolean)
    private Boolean isFeatured;

    @Field(type = FieldType.Boolean)
    private Boolean isInternal;

    @Field(type = FieldType.Keyword)
    private String projectId;

    @Field(type = FieldType.Keyword)
    private Set<String> tags;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] embedding;
}
