package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.UpdatePostRequest;
import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostDraft;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceUpdateDeleteTest {

    @Mock
    private PostMapper postMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void updatesTitleAndDescriptionWhileKeepingDraftContent() throws Exception {
        Post currentPost = post(100L, "旧标题", "旧描述");
        Post savedPost = post(100L, "新标题", "新描述");
        PostDraft savedDraft = draft(100L);
        when(postMapper.selectByIdAndOwnerId(100L, 42L))
                .thenReturn(currentPost, savedPost);
        when(postMapper.selectDraftByPostId(100L)).thenReturn(savedDraft);
        when(postMapper.updatePostBasicInfo(100L, 42L, "新标题", "新描述"))
                .thenReturn(1);

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("  新标题  ");
        request.setDescription("  新描述  ");

        PostDetailResponse response = service().updatePost(42L, 100L, request);

        assertEquals("新标题", response.getTitle());
        assertEquals("新描述", response.getDescription());
        assertEquals(0, response.getVersion());
        verify(postMapper).updatePostBasicInfo(100L, 42L, "新标题", "新描述");
    }

    @Test
    void rejectsEmptyUpdateBeforeDatabaseWrite() {
        UpdatePostRequest request = new UpdatePostRequest();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().updatePost(42L, 100L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(postMapper, never()).selectByIdAndOwnerId(100L, 42L);
    }

    @Test
    void deletesRelatedRowsBeforePostRow() {
        when(postMapper.selectByIdAndOwnerId(100L, 42L))
                .thenReturn(post(100L, "标题", "描述"));
        when(postMapper.deletePost(100L, 42L)).thenReturn(1);

        service().deletePost(42L, 100L);

        InOrder order = inOrder(postMapper);
        order.verify(postMapper).selectByIdAndOwnerId(100L, 42L);
        order.verify(postMapper).clearPublishedRevisionId(100L, 42L);
        order.verify(postMapper).deletePostImages(100L);
        order.verify(postMapper).deletePostLikes(100L);
        order.verify(postMapper).deletePostFavorites(100L);
        order.verify(postMapper).deletePostShares(100L);
        order.verify(postMapper).deletePostComments(100L);
        order.verify(postMapper).deletePostRevisions(100L);
        order.verify(postMapper).deletePostDraft(100L);
        order.verify(postMapper).deletePost(100L, 42L);
    }

    @Test
    void cannotDeleteAnotherUsersPost() {
        when(postMapper.selectByIdAndOwnerId(100L, 42L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().deletePost(42L, 100L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(postMapper, never()).deletePost(100L, 42L);
    }

    private PostService service() {
        return new PostServiceImpl(postMapper, objectMapper);
    }

    private Post post(Long id, String title, String description) {
        Post post = new Post();
        post.setId(id);
        post.setOwnerId(42L);
        post.setTitle(title);
        post.setDescription(description);
        post.setSlug("post-test");
        post.setStatus("DRAFT");
        return post;
    }

    private PostDraft draft(Long postId) {
        PostDraft draft = new PostDraft();
        draft.setPostId(postId);
        draft.setContentJson("""
                {"type":"doc","children":[]}
                """);
        draft.setSchemaVersion(1);
        draft.setVersion(0);
        return draft;
    }
}
