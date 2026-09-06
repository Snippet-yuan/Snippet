package com.snippet.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "帖子列表摘要")
public class PostSummaryResponse {

    @Schema(description = "帖子 ID", example = "100")
    private Long id;

    @Schema(description = "帖子标题", example = "我的第一篇 Snippet")
    private String title;

    @Schema(description = "帖子描述", nullable = true, example = "这是文章简介")
    private String description;

    @Schema(description = "公开访问标识", example = "my-first-snippet")
    private String slug;

    @Schema(description = "帖子状态", example = "PUBLISHED")
    private String status;

    @Schema(description = "创建时间", example = "2026-09-06T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-09-06T12:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "发布时间，未发布时为空", nullable = true)
    private LocalDateTime publishedAt;
}
