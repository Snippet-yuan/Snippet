package com.snippet.post.service;

import com.snippet.post.dto.CreatePostRequest;
import com.snippet.post.dto.CreateCommentRequest;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.PostCommentResponse;
import com.snippet.post.dto.PostFavoriteStatusResponse;
import com.snippet.post.dto.PostFavoriteItemResponse;
import com.snippet.post.dto.PostSummaryResponse;
import com.snippet.post.dto.PostLikeStatusResponse;
import com.snippet.post.dto.PublishRequest;
import com.snippet.post.dto.SaveDraftRuquest;
import com.snippet.post.dto.UpdatePostRequest;

import java.util.List;

public interface PostService {

    List<PostSummaryResponse> getPublicPosts(
            Integer limit,
            Integer offset
    );

    PostDetailResponse getPublicPost(String slug);

    PostDetailResponse createPost(Long ownerId, CreatePostRequest request);

    List<PostSummaryResponse> getMyPosts(
            Long ownerId,
            Integer limit,
            Integer offset
    );

    PostDetailResponse getPostDetail(Long ownerId, Long postId);

    PostDetailResponse saveDraft(Long ownerId, Long postId, SaveDraftRuquest request);

    PostDetailResponse updatePost(Long ownerId, Long postId, UpdatePostRequest request);

    void deletePost(Long ownerId, Long postId);

    PostDetailResponse publishPost(Long ownerId, Long postId, PublishRequest request);

    PostLikeStatusResponse likePost(Long userId, Long postId);

    PostLikeStatusResponse unlikePost(Long userId, Long postId);

    PostLikeStatusResponse getLikeStatus(Long userId, Long postId);

    PostFavoriteStatusResponse favoritePost(Long userId, Long postId);

    PostFavoriteStatusResponse unfavoritePost(Long userId, Long postId);

    PostFavoriteStatusResponse getFavoriteStatus(Long userId, Long postId);

    List<PostFavoriteItemResponse> getFavoritePosts(
            Long userId,
            Integer limit,
            Integer offset
    );

    PostCommentResponse createComment(
            Long userId,
            Long postId,
            CreateCommentRequest request
    );

    List<PostCommentResponse> getPublicComments(
            String slug,
            Integer limit,
            Integer offset
    );

    void deleteComment(Long userId, Long postId, Long commentId);
}
