package com.snippet.auth.dto;

import com.snippet.auth.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "LoginResponse", description = "用户登录响应")
public class LoginResponse {

    @Schema(description = "访问凭证", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "凭证类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "凭证有效期，单位为秒", example = "7200")
    private Long expiresIn;

    @Schema(description = "当前用户信息")
    private UserInfoVO user;
}
