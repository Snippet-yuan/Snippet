package com.snippet.post.controller;

import com.snippet.common.api.CommonResult;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.CreateCommentRequest;
import com.snippet.post.dto.CreatePostRequest;
import com.snippet.post.dto.PostCommentResponse;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.PostFavoriteStatusResponse;
import com.snippet.post.dto.PostFavoriteItemResponse;
import com.snippet.post.dto.PostLikeStatusResponse;
import com.snippet.post.dto.PostSummaryResponse;
import com.snippet.post.dto.PublishRequest;
import com.snippet.post.dto.SaveDraftRuquest;
import com.snippet.post.dto.UpdatePostRequest;
import com.snippet.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "文章接口", description = "创建、编辑、发布和公开查看文章")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/posts")
    @Operation(
            operationId = "createPost",
            summary = "创建文章",
            description = "创建一篇文章并生成初始草稿"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "409", description = "公开标识冲突")
    })
    public CommonResult<PostDetailResponse> createPost(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePostRequest request) {
        return CommonResult.success(postService.createPost(currentUserId(jwt), request));
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
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SaveDraftRuquest request) {
        return CommonResult.success(
                postService.saveDraft(currentUserId(jwt), postId, request)
        );
    }

    @PatchMapping("/posts/{postId}")
    @Operation(
            operationId = "updatePost",
            summary = "修改文章基本信息",
            description = "修改文章标题和描述，正文通过草稿接口单独保存"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public CommonResult<PostDetailResponse> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdatePostRequest request) {
        return CommonResult.success(
                postService.updatePost(currentUserId(jwt), postId, request)
        );
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(
            operationId = "deletePost",
            summary = "删除文章",
            description = "删除当前用户的文章及其草稿、历史版本和互动关联"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public CommonResult<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        postService.deletePost(currentUserId(jwt), postId);
        return CommonResult.success();
    }

    @PostMapping("/posts/{postId}/like")
    @Operation(
            operationId = "likePost",
            summary = "点赞文章",
            description = "为已发布文章添加当前用户的点赞关系，重复请求保持成功"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "点赞成功"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在或尚未发布")
    })
    public CommonResult<PostLikeStatusResponse> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.likePost(currentUserId(jwt), postId)
        );
    }

    @DeleteMapping("/posts/{postId}/like")
    @Operation(
            operationId = "unlikePost",
            summary = "取消点赞",
            description = "删除当前用户对文章的点赞关系，重复请求保持成功"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消点赞成功"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在或尚未发布")
    })
    public CommonResult<PostLikeStatusResponse> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.unlikePost(currentUserId(jwt), postId)
        );
    }

    @GetMapping("/posts/{postId}/like")
    @Operation(
            operationId = "getLikeStatus",
            summary = "查询点赞状态",
            description = "查询当前用户是否已点赞该已发布文章"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在或尚未发布")
    })
    public CommonResult<PostLikeStatusResponse> getLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.getLikeStatus(currentUserId(jwt), postId)
        );
    }

    @PostMapping("/posts/{postId}/favorite")
    @Operation(
            operationId = "favoritePost",
            summary = "收藏文章",
            description = "为已发布文章添加当前用户的收藏关系，重复请求保持成功"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "收藏成功"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在或尚未发布")
    })
    public CommonResult<PostFavoriteStatusResponse> favoritePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.favoritePost(currentUserId(jwt), postId)
        );
    }

    @DeleteMapping("/posts/{postId}/favorite")
    @Operation(
            operationId = "unfavoritePost",
            summary = "取消收藏",
            description = "删除当前用户对文章的收藏关系，重复请求保持成功"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消收藏成功"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在或尚未发布")
    })
    public CommonResult<PostFavoriteStatusResponse> unfavoritePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.unfavoritePost(currentUserId(jwt), postId)
        );
    }

    @GetMapping("/posts/{postId}/favorite")
    @Operation(
            operationId = "getFavoriteStatus",
            summary = "查询收藏状态",
            description = "查询当前用户是否已收藏该已发布文章"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在或尚未发布")
    })
    public CommonResult<PostFavoriteStatusResponse> getFavoriteStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.getFavoriteStatus(currentUserId(jwt), postId)
        );
    }

    @GetMapping("/me/favorites")
    @Operation(
            operationId = "getMyFavoritePosts",
            summary = "查看我的收藏",
            description = "分页查看当前用户收藏且仍然公开的文章，用户身份从 JWT 获取"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "分页参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public CommonResult<List<PostFavoriteItemResponse>> getMyFavoritePosts(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.getFavoritePosts(currentUserId(jwt), limit, offset)
        );
    }

    @PostMapping("/posts/{postId}/comments")
    @Operation(
            operationId = "createPostComment",
            summary = "发表评论",
            description = "为已发布文章创建一条纯文本评论，作者身份从 JWT 获取"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "评论成功"),
            @ApiResponse(responseCode = "400", description = "评论内容或帖子 ID 不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "文章不存在或尚未发布")
    })
    public CommonResult<PostCommentResponse> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCommentRequest request) {
        return CommonResult.success(
                postService.createComment(currentUserId(jwt), postId, request)
        );
    }

    @GetMapping("/public/posts/{slug}/comments")
    @Operation(
            operationId = "getPublicPostComments",
            summary = "查看文章评论",
            description = "无需登录，分页查看已发布文章的公开评论"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "公开标识或分页参数不合法"),
            @ApiResponse(responseCode = "404", description = "公开文章不存在")
    })
    public CommonResult<List<PostCommentResponse>> getPublicComments(
            @PathVariable String slug,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return CommonResult.success(
                postService.getPublicComments(slug, limit, offset)
        );
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @Operation(
            operationId = "deletePostComment",
            summary = "删除自己的评论",
            description = "只能删除当前用户自己创建的评论"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "帖子 ID 或评论 ID 不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期"),
            @ApiResponse(responseCode = "404", description = "评论不存在、无权删除或文章未发布")
    })
    public CommonResult<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Jwt jwt) {
        postService.deleteComment(currentUserId(jwt), postId, commentId);
        return CommonResult.success();
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
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PublishRequest request) {
        return CommonResult.success(
                postService.publishPost(currentUserId(jwt), postId, request)
        );
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
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.getPostDetail(currentUserId(jwt), postId)
        );
    }

    @GetMapping("/users/me/posts")
    @Operation(
            operationId = "getMyPosts",
            summary = "查看我的文章",
            description = "分页查看当前用户创建的文章，包含草稿和已发布文章，用户身份从 JWT 获取"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "分页参数不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public CommonResult<List<PostSummaryResponse>> getMyPosts(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @AuthenticationPrincipal Jwt jwt) {
        return CommonResult.success(
                postService.getMyPosts(currentUserId(jwt), limit, offset)
        );
    }

    @GetMapping("/public/posts")
    @Operation(
            operationId = "getPublicPosts",
            summary = "查看公开文章列表",
            description = "无需登录，分页查看已发布文章摘要"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "分页参数不合法")
    })
    public CommonResult<List<PostSummaryResponse>> getPublicPosts(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return CommonResult.success(
                postService.getPublicPosts(limit, offset)
        );
    }

    @GetMapping("/public/posts/{slug}")
    @Operation(
            operationId = "getPublicPost",
            summary = "公开查看文章",
            description = "无需登录，根据公开标识查看已发布文章"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "400", description = "公开标识不合法"),
            @ApiResponse(responseCode = "404", description = "公开文章不存在")
    })
    public CommonResult<PostDetailResponse> getPublicPost(
            @PathVariable String slug) {
        return CommonResult.success(postService.getPublicPost(slug));
    }

    private Long currentUserId(Jwt jwt) {
        if (jwt == null || !StringUtils.hasText(jwt.getSubject())) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "访问凭证中的用户身份无效"
            );
        }

        try {
            long userId = Long.parseLong(jwt.getSubject());
            if (userId <= 0) {
                throw new NumberFormatException("用户 ID 必须为正数");
            }
            return userId;
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "访问凭证中的用户身份无效"
            );
        }
    }
}
