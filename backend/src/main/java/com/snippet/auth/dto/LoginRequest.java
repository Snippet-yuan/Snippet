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

    @NotBlank
    @Size(min = 10, max = 20)
    @Schema(description = "登录账号", example = "snippetuser1")
    private String username;

    @NotBlank
    @Size(min = 10, max = 20)
    @Schema(description = "登录密码", example = "1234567890")
    private String password;
}
