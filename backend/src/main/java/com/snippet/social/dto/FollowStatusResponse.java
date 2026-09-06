package com.snippet.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "关注状态")
public class FollowStatusResponse {

    @Schema(description = "当前用户是否已关注目标用户", example = "true")
    private boolean following;
}
