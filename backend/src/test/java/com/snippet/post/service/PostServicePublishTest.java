package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.PublishRequest;
import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostDraft;
import com.snippet.post.entity.PostRevision;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServicePublishTest {

    @Mock
    private PostMapper postMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void publishesCurrentDraftAsRevisionAndUpdatesPost() throws Exception {
        String contentJson = """
                {"type":"doc","children":[{"type":"paragraph","children":[{"text":"发布测试正文"}]}]}
                """;
        Post currentPost = post("DRAFT");
        Post savedPost = post("PUBLISHED");
        savedPost.setPublishedAt(LocalDateTime.of(2026, 9, 5, 0, 30));
        PostDraft draft = draft(contentJson);

        when(postMapper.selectByIdAndOwnerId(100L, 42L))
                .thenReturn(currentPost, savedPost);
        when(postMapper.selectDraftByPostId(100L)).thenReturn(draft, draft);
        when(postMapper.selectLatestRevisionNo(100L)).thenReturn(null);
        doAnswer(invocation -> {
            PostRevision revision = invocation.getArgument(0);
            revision.setId(300L);
            return 1;
        }).when(postMapper).insertPostRevision(any(PostRevision.class));
        when(postMapper.publishPost(100L, 42L, 300L)).thenReturn(1);

        PublishRequest request = new PublishRequest();
        request.setExpectedVersion(0);

        PostDetailResponse response = service().publishPost(42L, 100L, request);

        assertEquals("PUBLISHED", response.getStatus());
        assertEquals("发布测试正文", response.getContent().at("/children/0/children/0/text").textValue());
        assertNotNull(response.getPublishedAt());

        ArgumentCaptor<PostRevision> revisionCaptor = ArgumentCaptor.forClass(PostRevision.class);
        verify(postMapper).insertPostRevision(revisionCaptor.capture());
        assertEquals(100L, revisionCaptor.getValue().getPostId());
        assertEquals(1, revisionCaptor.getValue().getRevisionNo());
        assertEquals(contentJson, revisionCaptor.getValue().getContentJson());
        assertEquals(1, revisionCaptor.getValue().getSchemaVersion());
        assertEquals(42L, revisionCaptor.getValue().getCreatedBy());
        verify(postMapper).publishPost(100L, 42L, 300L);
    }

    @Test
    void rejectsStaleDraftVersionBeforeCreatingRevision() {
        when(postMapper.selectByIdAndOwnerId(100L, 42L)).thenReturn(post("DRAFT"));
        when(postMapper.selectDraftByPostId(100L)).thenReturn(
                staleDraft("""
                        {"type":"doc","children":[]}
                        """)
        );

        PublishRequest request = new PublishRequest();
        request.setExpectedVersion(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().publishPost(42L, 100L, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        verify(postMapper, never()).selectLatestRevisionNo(100L);
        verify(postMapper, never()).insertPostRevision(any(PostRevision.class));
    }

    @Test
    void incrementsRevisionNumberWhenHistoryExists() {
        when(postMapper.selectByIdAndOwnerId(100L, 42L))
                .thenReturn(post("DRAFT"), post("PUBLISHED"));
        PostDraft draft = draft("""
                {"type":"doc","children":[]}
                """);
        when(postMapper.selectDraftByPostId(100L)).thenReturn(draft, draft);
        when(postMapper.selectLatestRevisionNo(100L)).thenReturn(3);
        doAnswer(invocation -> {
            PostRevision revision = invocation.getArgument(0);
            revision.setId(301L);
            return 1;
        }).when(postMapper).insertPostRevision(any(PostRevision.class));
        when(postMapper.publishPost(100L, 42L, 301L)).thenReturn(1);

        PublishRequest request = new PublishRequest();
        request.setExpectedVersion(0);
        service().publishPost(42L, 100L, request);

        ArgumentCaptor<PostRevision> revisionCaptor = ArgumentCaptor.forClass(PostRevision.class);
        verify(postMapper).insertPostRevision(revisionCaptor.capture());
        assertEquals(4, revisionCaptor.getValue().getRevisionNo());
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(postMapper, objectMapper);
    }

    private Post post(String status) {
        Post post = new Post();
        post.setId(100L);
        post.setOwnerId(42L);
        post.setTitle("发布测试帖子");
        post.setDescription("发布测试描述");
        post.setSlug("post-test");
        post.setStatus(status);
        return post;
    }

    private PostDraft draft(String contentJson) {
        PostDraft draft = new PostDraft();
        draft.setPostId(100L);
        draft.setContentJson(contentJson);
        draft.setSchemaVersion(1);
        draft.setVersion(0);
        return draft;
    }
    private PostDraft staleDraft(String contentJson) {
        PostDraft draft = draft(contentJson);
        draft.setVersion(1);
        return draft;
    }

}
