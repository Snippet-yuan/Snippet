package com.snippet.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "当前用户资料")
public class UserProfileResponse {

    @Schema(description = "用户 ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "snippetuser1")
    private String username;

    @Schema(description = "邮箱", example = "user@example.com", nullable = true)
    private String email;

    @Schema(description = "昵称", example = "Snippet 用户", nullable = true)
    private String nickname;

    @Schema(description = "头像资源 ID", example = "10", nullable = true)
    private Long avatarAssetId;

    @Schema(description = "背景图资源 ID", example = "11", nullable = true)
    private Long backgroundAssetId;
}
