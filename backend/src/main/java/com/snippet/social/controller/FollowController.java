package com.snippet.social.controller;

import com.snippet.common.api.CommonResult;
import com.snippet.common.exception.BusinessException;
import com.snippet.social.dto.FollowStatusResponse;
import com.snippet.social.dto.UserSummaryResponse;
import com.snippet.social.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "关注接口", description = "单向关注、取消关注和关注列表")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @GetMapping("/users/me/following")
    @Operation(
            operationId = "getFollowing",
            summary = "获取我的关注列表",
            description = "分页获取当前用户关注的活跃用户，身份从 JWT.sub 获取"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "分页参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public CommonResult<List<UserSummaryResponse>> getFollowing(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                followService.getFollowing(currentUserId(jwt), limit, offset)
        );
    }

    @PostMapping("/users/{followingId}/follow")
    @Operation(
            operationId = "followUser",
            summary = "关注用户",
            description = "当前用户关注目标用户，当前用户身份从 JWT.sub 获取"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "关注成功"),
            @ApiResponse(responseCode = "400", description = "目标用户 ID 不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "目标用户不存在")
    })
    public CommonResult<FollowStatusResponse> follow(
            @PathVariable Long followingId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                followService.follow(currentUserId(jwt), followingId)
        );
    }

    @DeleteMapping("/users/{followingId}/follow")
    @Operation(
            operationId = "unfollowUser",
            summary = "取消关注",
            description = "删除当前用户到目标用户的单向关注关系"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消关注成功"),
            @ApiResponse(responseCode = "400", description = "目标用户 ID 不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "目标用户不存在")
    })
    public CommonResult<FollowStatusResponse> unfollow(
            @PathVariable Long followingId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                followService.unfollow(currentUserId(jwt), followingId)
        );
    }

    @GetMapping("/users/{followingId}/follow")
    @Operation(
            operationId = "getFollowingStatus",
            summary = "查询关注状态",
            description = "查询当前用户是否已关注目标用户"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "目标用户 ID 不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "目标用户不存在")
    })
    public CommonResult<FollowStatusResponse> getFollowingStatus(
            @PathVariable Long followingId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                followService.getFollowingStatus(currentUserId(jwt), followingId)
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
