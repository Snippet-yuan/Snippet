package com.snippet.auth.controller;

import com.snippet.auth.dto.ChangePasswordRequest;
import com.snippet.auth.dto.LoginRequest;
import com.snippet.auth.dto.LoginResponse;
import com.snippet.auth.dto.RegisterRequest;
import com.snippet.auth.service.AuthService;
import com.snippet.auth.vo.UserInfoVO;
import com.snippet.common.api.CommonResult;
import com.snippet.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证接口", description = "注册、登录和密码相关接口")
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
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法"),
            @ApiResponse(responseCode = "409", description = "用户名已存在")
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
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法"),
            @ApiResponse(responseCode = "401", description = "账号或密码错误"),
            @ApiResponse(responseCode = "403", description = "账号不可用")
    })
    public CommonResult<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return CommonResult.success(authService.login(request));
    }

    @PutMapping("/password")
    @Operation(
            operationId = "changePassword",
            summary = "修改密码",
            description = "验证当前密码后更新密码哈希，并使该用户此前签发的 JWT 全部失效；成功后必须重新登录"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "密码修改成功"),
            @ApiResponse(responseCode = "400", description = "密码参数或当前密码不正确"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "403", description = "用户账号不可用"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public CommonResult<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUserId(jwt), request);
        return CommonResult.success();
    }

    private Long currentUserId(Jwt jwt) {
        if (jwt == null || !StringUtils.hasText(jwt.getSubject())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "访问凭证中的用户身份无效");
        }

        try {
            long userId = Long.parseLong(jwt.getSubject());
            if (userId <= 0) {
                throw new NumberFormatException("用户 ID 必须为正数");
            }
            return userId;
        } catch (NumberFormatException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "访问凭证中的用户身份无效");
        }
    }
}
