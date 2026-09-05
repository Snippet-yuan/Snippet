package com.snippet.post.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.CreatePostRequest;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostDraft;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createsPostAndInitialDraftForCurrentUser() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 9, 4, 14, 0);
        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(100L);
            return 1;
        }).when(postMapper).insertPost(any(Post.class));
        doAnswer(invocation -> {
            PostDraft draft = invocation.getArgument(0);
            draft.setId(200L);
            return 1;
        }).when(postMapper).insertInitialDraft(any(PostDraft.class));

        Post savedPost = new Post();
        savedPost.setId(100L);
        savedPost.setOwnerId(42L);
        savedPost.setTitle("第一篇帖子");
        savedPost.setSlug("post-test");
        savedPost.setStatus("DRAFT");
        savedPost.setCreatedAt(now);
        savedPost.setUpdatedAt(now);
        when(postMapper.selectByIdAndOwnerId(100L, 42L)).thenReturn(savedPost);

        PostDraft savedDraft = new PostDraft();
        savedDraft.setId(200L);
        savedDraft.setPostId(100L);
        savedDraft.setContentJson("{\"type\":\"doc\",\"children\":[]}");
        savedDraft.setSchemaVersion(1);
        savedDraft.setVersion(0);
        when(postMapper.selectDraftByPostId(100L)).thenReturn(savedDraft);

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("  第一篇帖子  ");

        PostDetailResponse response = service().createPost(42L, request);

        assertEquals(100L, response.getId());
        assertEquals("第一篇帖子", response.getTitle());
        assertEquals("post-test", response.getSlug());
        assertEquals("DRAFT", response.getStatus());
        assertEquals(1, response.getSchemaVersion());
        assertEquals(0, response.getVersion());
        assertEquals(objectMapper.readTree("{\"type\":\"doc\",\"children\":[]}"), response.getContent());
        assertEquals(now, response.getCreatedAt());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insertPost(postCaptor.capture());
        assertEquals(42L, postCaptor.getValue().getOwnerId());
        assertEquals("第一篇帖子", postCaptor.getValue().getTitle());
        assertEquals("DRAFT", postCaptor.getValue().getStatus());
        assertNotNull(postCaptor.getValue().getSlug());

        ArgumentCaptor<PostDraft> draftCaptor = ArgumentCaptor.forClass(PostDraft.class);
        verify(postMapper).insertInitialDraft(draftCaptor.capture());
        assertEquals(100L, draftCaptor.getValue().getPostId());
        assertEquals(42L, draftCaptor.getValue().getUpdatedBy());
        assertEquals(1, draftCaptor.getValue().getSchemaVersion());
        assertEquals(0, draftCaptor.getValue().getVersion());
    }

    @Test
    void rejectsBlankTitleBeforeDatabaseWrite() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("   ");

        assertThrows(BusinessException.class, () -> service().createPost(42L, request));

        verify(postMapper, never()).insertPost(any(Post.class));
        verify(postMapper, never()).insertInitialDraft(any(PostDraft.class));
    }

    @Test
    void rejectsControlCharacterInTitleBeforeDatabaseWrite() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("正常标题\n脏数据");

        assertThrows(BusinessException.class, () -> service().createPost(42L, request));

        verify(postMapper, never()).insertPost(any(Post.class));
    }

    @Test
    void rejectsInvalidOwnerIdBeforeDatabaseWrite() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("正常标题");

        assertThrows(BusinessException.class, () -> service().createPost(0L, request));

        verify(postMapper, never()).insertPost(any(Post.class));
    }

    @Test
    void stopsWhenPostInsertDoesNotWriteOneRow() {
        when(postMapper.insertPost(any(Post.class))).thenReturn(0);
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("正常标题");

        assertThrows(BusinessException.class, () -> service().createPost(42L, request));

        verify(postMapper, never()).insertInitialDraft(any(PostDraft.class));
    }

    private PostService service() {
        return new PostServiceImpl(postMapper, objectMapper);
    }
}
