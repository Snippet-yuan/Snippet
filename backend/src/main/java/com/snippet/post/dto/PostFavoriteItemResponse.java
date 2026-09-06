package com.snippet.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "收藏列表中的帖子")
public class PostFavoriteItemResponse {

    @Schema(description = "帖子 ID", example = "100")
    private Long postId;

    @Schema(description = "帖子标题", example = "我的第一篇 Snippet")
    private String title;

    @Schema(description = "帖子描述", nullable = true, example = "这是文章简介")
    private String description;

    @Schema(description = "公开访问标识", example = "my-first-snippet")
    private String slug;

    @Schema(description = "收藏时间", example = "2026-09-06T12:30:00")
    private LocalDateTime favoritedAt;

    @Schema(description = "帖子发布时间", example = "2026-09-06T12:00:00")
    private LocalDateTime publishedAt;
}
