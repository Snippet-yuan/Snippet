package com.snippet.social.controller;

import com.snippet.common.api.CommonResult;
import com.snippet.common.exception.BusinessException;
import com.snippet.social.dto.FriendRequestResponse;
import com.snippet.social.dto.SendFriendRequest;
import com.snippet.social.service.FriendRelationService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "用户关系接口", description = "关系列表和好友申请处理")
public class FriendRelationController {

    private final FriendRelationService friendRelationService;

    public FriendRelationController(FriendRelationService friendRelationService) {
        this.friendRelationService = friendRelationService;
    }

    @GetMapping("/friend-requests")
    @Operation(
            operationId = "getIncomingFriendRequests",
            summary = "获取待处理好友申请",
            description = "分页获取发给当前用户且仍处于待处理状态的好友申请"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "分页参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public CommonResult<List<FriendRequestResponse>> getIncomingRequests(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                friendRelationService.getIncomingRequests(currentUserId(jwt), limit, offset)
        );
    }

    @PostMapping("/friend-requests")
    @Operation(
            operationId = "sendFriendRequest",
            summary = "发起好友申请",
            description = "向目标用户发起好友申请，申请人身份从 JWT.sub 获取"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "申请已创建"),
            @ApiResponse(responseCode = "400", description = "目标用户或请求参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "目标用户不存在"),
            @ApiResponse(responseCode = "409", description = "关系状态冲突")
    })
    public CommonResult<FriendRequestResponse> sendFriendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SendFriendRequest request) {
        return CommonResult.success(
                friendRelationService.sendFriendRequest(currentUserId(jwt), request)
        );
    }

    @PostMapping("/friend-requests/{requestId}/accept")
    @Operation(
            operationId = "acceptFriendRequest",
            summary = "接受好友申请",
            description = "只能处理发给当前用户的待处理申请"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "申请已接受"),
            @ApiResponse(responseCode = "400", description = "申请记录 ID 不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "申请不存在或已处理"),
            @ApiResponse(responseCode = "409", description = "申请状态已变化")
    })
    public CommonResult<FriendRequestResponse> acceptFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                friendRelationService.acceptFriendRequest(currentUserId(jwt), requestId)
        );
    }

    @PostMapping("/friend-requests/{requestId}/reject")
    @Operation(
            operationId = "rejectFriendRequest",
            summary = "拒绝好友申请",
            description = "只能处理发给当前用户的待处理申请"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "申请已拒绝"),
            @ApiResponse(responseCode = "400", description = "申请记录 ID 不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "申请不存在或已处理"),
            @ApiResponse(responseCode = "409", description = "申请状态已变化")
    })
    public CommonResult<FriendRequestResponse> rejectFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                friendRelationService.rejectFriendRequest(currentUserId(jwt), requestId)
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
