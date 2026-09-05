package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.CreateCommentRequest;
import com.snippet.post.dto.PostCommentResponse;
import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostComment;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceCommentTest {

    @Mock
    private PostMapper postMapper;

    @Test
    void createsCommentForPublishedPostAndUsesNormalizedContent() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        doAnswer(invocation -> {
            PostComment comment = invocation.getArgument(0);
            comment.setId(501L);
            return 1;
        }).when(postMapper).insertComment(any(PostComment.class));
        when(postMapper.selectCommentById(501L)).thenReturn(
                comment(501L, 100L, 42L, "Snippet 用户", "有帮助的评论")
        );

        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("  有帮助的评论  ");

        PostCommentResponse response = service().createComment(42L, 100L, request);

        assertEquals(501L, response.getId());
        assertEquals("有帮助的评论", response.getContent());
        verify(postMapper).selectPublishedById(100L);
        verify(postMapper).selectCommentById(501L);
    }

    @Test
    void rejectsBlankCommentBeforeDatabaseWrite() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("   ");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().createComment(42L, 100L, request)
        );

        assertEquals(400, exception.getCode());
        verify(postMapper, never()).selectPublishedById(100L);
        verify(postMapper, never()).insertComment(any(PostComment.class));
    }

    @Test
    void rejectsDisallowedControlCharacter() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("正常文本" + (char) 0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().createComment(42L, 100L, request)
        );

        assertEquals(400, exception.getCode());
        verify(postMapper, never()).insertComment(any(PostComment.class));
    }

    @Test
    void publicCommentsUsePublishedSlugAndBoundedPage() {
        when(postMapper.selectPublishedBySlug("post-public"))
                .thenReturn(publishedPost());
        when(postMapper.selectPublicCommentsBySlug("post-public", 20, 0))
                .thenReturn(List.of(
                        comment(501L, 100L, 42L, "Snippet 用户", "第一条评论")
                ));

        List<PostCommentResponse> responses = service()
                .getPublicComments("post-public", null, null);

        assertEquals(1, responses.size());
        assertEquals("第一条评论", responses.get(0).getContent());
        verify(postMapper).selectPublicCommentsBySlug("post-public", 20, 0);
    }

    @Test
    void rejectsUnpublishedPostCommentList() {
        when(postMapper.selectPublishedBySlug("post-draft")).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getPublicComments("post-draft", 20, 0)
        );

        assertEquals(404, exception.getCode());
        verify(postMapper, never())
                .selectPublicCommentsBySlug("post-draft", 20, 0);
    }

    @Test
    void deletesOnlyCurrentUsersComment() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.deleteComment(100L, 501L, 42L)).thenReturn(1);

        service().deleteComment(42L, 100L, 501L);

        verify(postMapper).deleteComment(100L, 501L, 42L);
    }

    @Test
    void returnsNotFoundWhenCommentDoesNotBelongToCurrentUser() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.deleteComment(100L, 501L, 42L)).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().deleteComment(42L, 100L, 501L)
        );

        assertEquals(404, exception.getCode());
        assertTrue(exception.getMessage().contains("无权删除"));
    }

    private Post publishedPost() {
        Post post = new Post();
        post.setId(100L);
        post.setStatus("PUBLISHED");
        return post;
    }

    private PostComment comment(
            Long id,
            Long postId,
            Long authorId,
            String authorName,
            String content) {
        return new PostComment(
                id,
                postId,
                authorId,
                authorName,
                null,
                content,
                LocalDateTime.of(2026, 9, 5, 12, 30)
        );
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(
                postMapper,
                new ObjectMapper()
        );
    }
}
