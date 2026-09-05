package com.snippet.post.mapper;

import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostComment;
import com.snippet.post.entity.PostDraft;
import com.snippet.post.entity.PostRevision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    int insertPost(Post post);

    int insertInitialDraft(PostDraft postDraft);

    Post selectByIdAndOwnerId(
            @Param("postId") Long postId,
            @Param("ownerId") Long ownerId
    );

    Post selectPublishedBySlug(@Param("slug") String slug);

    PostRevision selectRevisionById(@Param("revisionId") Long revisionId);

    Post selectPublishedById(@Param("postId") Long postId);

    boolean existsLike(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    int insertLike(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    int deleteLike(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    boolean existsFavorite(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    int insertFavorite(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    int deleteFavorite(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    int insertComment(PostComment comment);

    PostComment selectCommentById(@Param("commentId") Long commentId);

    List<PostComment> selectPublicCommentsBySlug(
            @Param("slug") String slug,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    int deleteComment(
            @Param("postId") Long postId,
            @Param("commentId") Long commentId,
            @Param("authorId") Long authorId
    );

    PostDraft selectDraftByPostId(@Param("postId") Long postId);

    int updateDraftContent(
            @Param("postId") Long postId,
            @Param("contentJson") String contentJson,
            @Param("schemaVersion") Integer schemaVersion,
            @Param("expectedVersion") Integer expectedVersion,
            @Param("updatedBy") Long updatedBy
    );

    int touchPostUpdatedAt(
            @Param("postId") Long postId,
            @Param("ownerId") Long ownerId
    );

    int updatePostBasicInfo(
            @Param("postId") Long postId,
            @Param("ownerId") Long ownerId,
            @Param("title") String title,
            @Param("description") String description
    );

    int clearPublishedRevisionId(
            @Param("postId") Long postId,
            @Param("ownerId") Long ownerId
    );

    int deletePostImages(@Param("postId") Long postId);

    int deletePostLikes(@Param("postId") Long postId);

    int deletePostFavorites(@Param("postId") Long postId);

    int deletePostShares(@Param("postId") Long postId);

    int deletePostComments(@Param("postId") Long postId);

    int deletePostRevisions(@Param("postId") Long postId);

    int deletePostDraft(@Param("postId") Long postId);

    int deletePost(
            @Param("postId") Long postId,
            @Param("ownerId") Long ownerId
    );
    Integer selectLatestRevisionNo(@Param("postId") Long postId);

    int insertPostRevision(PostRevision revision);

    int publishPost(
            @Param("postId") Long postId,
            @Param("ownerId") Long ownerId,
            @Param("revisionId") Long revisionId
    );
}
