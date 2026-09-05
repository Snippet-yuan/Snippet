package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostRevision;
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
class PostServicePublicTest {

    @Mock
    private PostMapper postMapper;

    @Test
    void returnsPublishedRevisionWithoutReadingDraft() {
        Post post = publishedPost();

        PostRevision revision = new PostRevision();
        revision.setId(200L);
        revision.setPostId(100L);
        revision.setRevisionNo(3);
        revision.setContentJson("""
                {"type":"doc","children":[{"type":"paragraph","children":[{"text":"正式正文"}]}]}
                """);
        revision.setSchemaVersion(1);

        when(postMapper.selectPublishedBySlug("post-public")).thenReturn(post);
        when(postMapper.selectRevisionById(200L)).thenReturn(revision);

        PostDetailResponse response = service().getPublicPost("post-public");

        assertEquals(100L, response.getId());
        assertEquals("PUBLISHED", response.getStatus());
        assertEquals(3, response.getVersion());
        assertEquals("正式正文", response.getContent()
                .get("children").get(0).get("children").get(0).get("text").textValue());
        verify(postMapper).selectPublishedBySlug("post-public");
        verify(postMapper).selectRevisionById(200L);
        verify(postMapper, never()).selectDraftByPostId(100L);
    }

    @Test
    void hidesDraftWhenNoPublishedPostMatchesSlug() {
        when(postMapper.selectPublishedBySlug("post-draft")).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getPublicPost("post-draft")
        );

        assertEquals(404, exception.getCode());
        verify(postMapper, never()).selectRevisionById(200L);
    }

    @Test
    void rejectsInvalidSlugBeforeDatabaseRead() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getPublicPost("  ")
        );

        assertEquals(400, exception.getCode());
        verify(postMapper, never()).selectPublishedBySlug("  ");
    }

    @Test
    void treatsMissingPublishedRevisionAsServerDataError() {
        Post post = publishedPost();
        when(postMapper.selectPublishedBySlug("post-public")).thenReturn(post);
        when(postMapper.selectRevisionById(200L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getPublicPost("post-public")
        );

        assertEquals(500, exception.getCode());
    }

    private Post publishedPost() {
        Post post = new Post();
        post.setId(100L);
        post.setOwnerId(42L);
        post.setTitle("公开帖子");
        post.setDescription("公开描述");
        post.setSlug("post-public");
        post.setStatus("PUBLISHED");
        post.setPublishedRevisionId(200L);
        return post;
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(
                postMapper,
                new ObjectMapper()
        );
    }
}
