package com.snippet.post.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "文章详情")
public class PostDetailResponse {

    @Schema(description = "文章id", example = "1")
    private Long id;

    @Schema(description = "文章标题", example = "我的第一篇 Snippet")
    private String title;

    @Schema(description = "公开访问标识", example = "my-first-snippet")
    private String slug;

    @Schema(description = "文章状态", example = "DRAFT")
    private String status;

    @Schema(description = "编辑器结构化内容")
    private JsonNode content;

    @Schema(description = "内容结构版本", example = "1")
    private Integer schemaVersion;

    @Schema(description = "当前草稿或发布版本", example = "3")
    private Integer version;

    @Schema(description = "创建时间", example = "2026-08-30T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-08-30T12:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "发布时间，未发布时为空", nullable = true)
    private LocalDateTime publishedAt;
}
