package com.snippet.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "帖子评论")
public class PostCommentResponse {

    @Schema(description = "评论 ID", example = "10")
    private Long id;

    @Schema(description = "帖子 ID", example = "7")
    private Long postId;

    @Schema(description = "评论作者 ID", example = "42")
    private Long authorId;

    @Schema(description = "评论作者展示名称，优先使用昵称", example = "Snippet 用户")
    private String authorName;

    @Schema(description = "评论作者头像资源 ID", example = "11", nullable = true)
    private Long authorAvatarAssetId;

    @Schema(description = "评论纯文本内容", example = "这篇文章很有帮助")
    private String content;

    @Schema(description = "评论创建时间", example = "2026-09-05T12:30:00")
    private LocalDateTime createdAt;
}
