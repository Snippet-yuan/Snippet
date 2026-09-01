package com.snippet.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "当前用户资料")
public class UserProfileResponse {

    @Schema(description = "用户id", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "snippetuser1")
    private String username;

    @Schema(description = "头像资源id", example = "10", nullable = true)
    private Long avatarAssetId;
}
