package com.snippet.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "用户基本信息")
public class UserInfoVO {

    @Schema(description = "用户id", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "snippetuser1")
    private String username;
}
