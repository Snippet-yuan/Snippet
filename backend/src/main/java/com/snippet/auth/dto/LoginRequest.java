package com.snippet.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "用户登录参数")
public class LoginRequest {

    @NotBlank(message = "账号不能为空")
    @Size(min = 10, max = 20, message = "账号长度必须在10到20个字符之间")
    @Schema(description = "登录账号", example = "snippetuser1")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 10, max = 20, message = "密码长度必须在10到20个字符之间")
    @Schema(description = "登录密码", example = "1234567890")
    private String password;
}
