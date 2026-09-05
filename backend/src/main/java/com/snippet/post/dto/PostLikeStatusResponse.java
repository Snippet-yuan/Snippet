package com.snippet.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "帖子点赞状态")
public class PostLikeStatusResponse {

    @Schema(description = "当前用户是否已点赞", example = "true")
    private boolean liked;
}
