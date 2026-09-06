package com.snippet.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 关系业务中对外展示的用户摘要，不包含邮箱、账号状态等私密字段。
 */
@Getter
@AllArgsConstructor
@Schema(description = "关系列表中的用户摘要")
public class UserSummaryResponse {

    @Schema(description = "用户 ID", example = "42")
    private Long id;

    @Schema(description = "用户名", example = "snippetuser1")
    private String username;

    @Schema(description = "昵称", nullable = true, example = "Snippet 用户")
    private String nickname;

    @Schema(description = "头像资源 ID", nullable = true, example = "10")
    private Long avatarAssetId;
}
