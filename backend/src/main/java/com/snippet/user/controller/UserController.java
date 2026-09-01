package com.snippet.user.controller;

import com.snippet.common.api.CommonResult;
import com.snippet.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户接口", description = "当前用户资料相关接口")
public class UserController {

    @GetMapping("/me")
    @Operation(
            operationId = "getCurrentUser",
            summary = "获取当前用户",
            description = "获取当前登录用户的基本资料"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public CommonResult<UserProfileResponse> currentUser() {
        throw new UnsupportedOperationException("当前用户业务尚未实现");
    }
}
