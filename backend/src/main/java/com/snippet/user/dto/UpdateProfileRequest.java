package com.snippet.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "修改当前用户资料参数，未提供的字段保持不变")
public class UpdateProfileRequest {

    @Size(min = 10, max = 20, message = "用户名长度必须在10到20个字符之间")
    @Schema(description = "用户名", example = "snippetuser2", nullable = true)
    private String username;

    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过255个字符")
    @Schema(description = "邮箱", example = "user@example.com", nullable = true)
    private String email;

    @Size(max = 64, message = "昵称长度不能超过64个字符")
    @Schema(description = "昵称", example = "Snippet 用户", nullable = true)
    private String nickname;

    @Positive(message = "头像资源 ID 必须为正数")
    @Schema(description = "头像资源 ID", example = "10", nullable = true)
    private Long avatarAssetId;

    @Positive(message = "背景图资源 ID 必须为正数")
    @Schema(description = "背景图资源 ID", example = "11", nullable = true)
    private Long backgroundAssetId;
}
