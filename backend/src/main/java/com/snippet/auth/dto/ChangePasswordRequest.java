package com.snippet.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "修改密码参数")
public class ChangePasswordRequest {

    @NotBlank(message = "当前密码不能为空")
    @Schema(description = "当前密码", example = "1234567890")
    private String currentPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 10, max = 20, message = "新密码长度必须在10到20个字符之间")
    @Schema(description = "新密码", example = "0987654321")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认新密码", example = "0987654321")
    private String confirmPassword;
}
