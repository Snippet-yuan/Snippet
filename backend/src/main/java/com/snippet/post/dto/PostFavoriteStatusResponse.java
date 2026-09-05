package com.snippet.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "帖子收藏状态")
public class PostFavoriteStatusResponse {

    @Schema(description = "当前用户是否已收藏", example = "true")
    private boolean favorited;
}
