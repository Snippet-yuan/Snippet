package com.snippet.user.controller;

import com.snippet.common.api.CommonResult;
import com.snippet.common.exception.BusinessException;
import com.snippet.user.dto.UpdateProfileRequest;
import com.snippet.user.dto.UserProfileResponse;
import com.snippet.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户接口", description = "当前用户资料相关接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(
            operationId = "getCurrentUser",
            summary = "获取当前用户资料",
            description = "获取当前登录用户的基本资料"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "403", description = "用户账号不可用"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public CommonResult<UserProfileResponse> currentUser(
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(userService.getCurrentUser(currentUserId(jwt)));
    }

    @PatchMapping("/me")
    @Operation(
            operationId = "updateCurrentUserProfile",
            summary = "修改当前用户资料",
            description = "只修改请求中提供的资料字段，用户身份从 JWT 的 sub 获取"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "资料参数或资源归属不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "403", description = "用户账号不可用"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "409", description = "用户名或邮箱已存在")
    })
    public CommonResult<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        return CommonResult.success(
                userService.updateProfile(currentUserId(jwt), request)
        );
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
