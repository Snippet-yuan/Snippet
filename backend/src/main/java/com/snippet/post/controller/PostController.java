package com.snippet.post.controller;

import com.snippet.common.api.CommonResult;
import com.snippet.post.dto.CreatePostRequest;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.PublishRequest;
import com.snippet.post.dto.SaveDraftRuquest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "文章接口", description = "创建、编辑、发布和公开查看文章")
public class PostController {

    @PostMapping("/posts")
    @Operation(
            operationId = "createPost",
            summary = "创建文章",
            description = "创建一篇文章并生成初始草稿"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public CommonResult<PostDetailResponse> createPost(
            @Valid @RequestBody CreatePostRequest request) {
        throw new UnsupportedOperationException("创建文章业务尚未实现");
    }

    @PutMapping("/posts/{postId}/draft")
    @Operation(
            operationId = "savePostDraft",
            summary = "保存文章草稿",
            description = "保存编辑器内容，并使用 version 进行乐观锁校验"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "400", description = "草稿内容或版本不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "409", description = "草稿版本冲突")
    })
    public CommonResult<PostDetailResponse> saveDraft(
            @PathVariable Long postId,
            @Valid @RequestBody SaveDraftRuquest request) {
        throw new UnsupportedOperationException("保存草稿业务尚未实现");
    }

    @PostMapping("/posts/{postId}/publish")
    @Operation(
            operationId = "publishPost",
            summary = "发布文章",
            description = "将指定版本的草稿发布为公开文章"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "400", description = "发布参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在"),
            @ApiResponse(responseCode = "409", description = "草稿版本冲突")
    })
    public CommonResult<PostDetailResponse> publish(
            @PathVariable Long postId,
            @Valid @RequestBody PublishRequest request) {
        throw new UnsupportedOperationException("发布文章业务尚未实现");
    }

    @GetMapping("/posts/{postId}")
    @Operation(
            operationId = "getPostDetail",
            summary = "获取文章详情",
            description = "获取当前用户有权限查看的文章和草稿内容"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public CommonResult<PostDetailResponse> getDetail(
            @PathVariable Long postId) {
        throw new UnsupportedOperationException("文章详情业务尚未实现");
    }

    @GetMapping("/public/posts/{slug}")
    @Operation(
            operationId = "getPublicPost",
            summary = "公开查看文章",
            description = "无需登录，根据公开标识查看已发布文章"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "404", description = "公开文章不存在")
    })
    public CommonResult<PostDetailResponse> getPublicPost(
            @PathVariable String slug) {
        throw new UnsupportedOperationException("公开文章业务尚未实现");
    }
}
