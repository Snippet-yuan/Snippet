package com.snippet.post.service;

import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostDraft;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceDetailTest {

    @Mock
    private PostMapper postMapper;

    @Test
    void returnsPostAndDraftForCurrentOwner() {
        Post post = new Post();
        post.setId(100L);
        post.setOwnerId(42L);
        post.setTitle("我的帖子");
        post.setSlug("post-test");
        post.setStatus("DRAFT");

        PostDraft draft = new PostDraft();
        draft.setPostId(100L);
        draft.setContentJson("{\"type\":\"doc\",\"children\":[]}");
        draft.setSchemaVersion(1);
        draft.setVersion(0);

        when(postMapper.selectByIdAndOwnerId(100L, 42L)).thenReturn(post);
        when(postMapper.selectDraftByPostId(100L)).thenReturn(draft);

        PostDetailResponse response = service().getPostDetail(42L, 100L);

        assertEquals(100L, response.getId());
        assertEquals("我的帖子", response.getTitle());
        assertEquals("post-test", response.getSlug());
        assertEquals("DRAFT", response.getStatus());
        assertEquals(1, response.getSchemaVersion());
        assertEquals(0, response.getVersion());
        verify(postMapper).selectByIdAndOwnerId(100L, 42L);
        verify(postMapper).selectDraftByPostId(100L);
    }

    @Test
    void hidesPostOwnedByAnotherUserAsNotFound() {
        when(postMapper.selectByIdAndOwnerId(100L, 42L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getPostDetail(42L, 100L)
        );

        assertEquals(404, exception.getCode());
        verify(postMapper, never()).selectDraftByPostId(100L);
    }

    @Test
    void rejectsInvalidPostIdBeforeDatabaseRead() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getPostDetail(42L, 0L)
        );

        assertEquals(400, exception.getCode());
        verify(postMapper, never()).selectByIdAndOwnerId(0L, 42L);
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(
                postMapper,
                new com.fasterxml.jackson.databind.ObjectMapper()
        );
    }
}
