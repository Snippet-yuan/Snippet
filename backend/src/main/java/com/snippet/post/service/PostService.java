package com.snippet.post.service;

import com.snippet.post.dto.CreatePostRequest;
import com.snippet.post.dto.CreateCommentRequest;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.PostCommentResponse;
import com.snippet.post.dto.PostFavoriteStatusResponse;
import com.snippet.post.dto.PostLikeStatusResponse;
import com.snippet.post.dto.PublishRequest;
import com.snippet.post.dto.SaveDraftRuquest;
import com.snippet.post.dto.UpdatePostRequest;

import java.util.List;

public interface PostService {

    PostDetailResponse getPublicPost(String slug);

    PostDetailResponse createPost(Long ownerId, CreatePostRequest request);

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
