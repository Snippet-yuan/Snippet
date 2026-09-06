package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostSummaryResponse;
import com.snippet.post.entity.Post;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServicePostListTest {

    @Mock
    private PostMapper postMapper;

    @Test
    void publicListUsesDefaultPageAndReturnsSummaries() {
        when(postMapper.selectPublicPosts(20, 0))
                .thenReturn(List.of(post(100L, "公开帖子", "PUBLISHED", "post-public")));

        List<PostSummaryResponse> responses = service()
                .getPublicPosts(null, null);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getId());
        assertEquals("公开帖子", responses.get(0).getTitle());
        assertEquals("PUBLISHED", responses.get(0).getStatus());
        assertEquals("post-public", responses.get(0).getSlug());
        verify(postMapper).selectPublicPosts(20, 0);
    }

    @Test
    void ownListUsesCurrentUserAndExplicitPage() {
        when(postMapper.selectPostsByOwnerId(42L, 10, 20))
                .thenReturn(List.of(post(101L, "我的草稿", "DRAFT", "post-draft")));

        List<PostSummaryResponse> responses = service()
                .getMyPosts(42L, 10, 20);

        assertEquals(1, responses.size());
        assertEquals(101L, responses.get(0).getId());
        assertEquals("DRAFT", responses.get(0).getStatus());
        verify(postMapper).selectPostsByOwnerId(42L, 10, 20);
    }

    @Test
    void invalidPublicPageDoesNotQueryDatabase() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getPublicPosts(101, 0)
        );

        assertEquals(400, exception.getCode());
        verify(postMapper, never()).selectPublicPosts(101, 0);
    }

    @Test
    void invalidOwnerDoesNotQueryDatabase() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getMyPosts(null, 20, 0)
        );

        assertEquals(401, exception.getCode());
        verify(postMapper, never()).selectPostsByOwnerId(null, 20, 0);
    }

    private Post post(Long id, String title, String status, String slug) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setStatus(status);
        post.setSlug(slug);
        return post;
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(
                postMapper,
                new ObjectMapper()
        );
    }
}
