package com.snippet.auth.controller;

import com.snippet.auth.dto.LoginRequest;
import com.snippet.auth.dto.LoginResponse;
import com.snippet.auth.dto.RegisterRequest;
import com.snippet.auth.service.AuthService;
import com.snippet.auth.vo.UserInfoVO;
import com.snippet.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证接口", description = "注册和登录相关接口")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            operationId = "register",
            summary = "用户注册",
            description = "使用账号和密码创建用户，成功后返回用户基本信息"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理，具体结果查看 CommonResult.code"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法")
    })
    public CommonResult<UserInfoVO> register(
            @Valid @RequestBody RegisterRequest request) {
        return CommonResult.success(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            operationId = "login",
            summary = "用户登录",
            description = "校验账号和密码，成功后返回访问凭证和用户信息"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理，具体结果查看 CommonResult.code"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法"),
            @ApiResponse(responseCode = "401", description = "账号或密码错误")
    })
    public CommonResult<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return CommonResult.success(authService.login(request));
    }
}
