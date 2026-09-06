package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostFavoriteItemResponse;
import com.snippet.post.entity.PostFavorite;
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
class PostServiceFavoriteListTest {

    @Mock
    private PostMapper postMapper;

    @Test
    void listsCurrentUsersPublishedFavoritesWithDefaultPage() {
        PostFavorite favorite = favorite(
                501L,
                100L,
                42L,
                "第一篇帖子",
                "post-first"
        );
        when(postMapper.selectFavoritePostsByUserId(42L, 20, 0))
                .thenReturn(List.of(favorite));

        List<PostFavoriteItemResponse> responses = service()
                .getFavoritePosts(42L, null, null);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getPostId());
        assertEquals("第一篇帖子", responses.get(0).getTitle());
        assertEquals("post-first", responses.get(0).getSlug());
        verify(postMapper).selectFavoritePostsByUserId(42L, 20, 0);
    }

    @Test
    void passesExplicitPageToMapper() {
        when(postMapper.selectFavoritePostsByUserId(42L, 10, 20))
                .thenReturn(List.of());

        List<PostFavoriteItemResponse> responses = service()
                .getFavoritePosts(42L, 10, 20);

        assertEquals(List.of(), responses);
        verify(postMapper).selectFavoritePostsByUserId(42L, 10, 20);
    }

    @Test
    void invalidPageDoesNotQueryDatabase() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getFavoritePosts(42L, 0, 0)
        );

        assertEquals(400, exception.getCode());
        verify(postMapper, never())
                .selectFavoritePostsByUserId(42L, 0, 0);
    }

    @Test
    void invalidUserDoesNotQueryDatabase() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getFavoritePosts(null, 20, 0)
        );

        assertEquals(401, exception.getCode());
        verify(postMapper, never())
                .selectFavoritePostsByUserId(null, 20, 0);
    }

    private PostFavorite favorite(
            Long id,
            Long postId,
            Long userId,
            String title,
            String slug) {
        PostFavorite favorite = new PostFavorite();
        favorite.setId(id);
        favorite.setPostId(postId);
        favorite.setUserId(userId);
        favorite.setTitle(title);
        favorite.setSlug(slug);
        return favorite;
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(
                postMapper,
                new ObjectMapper()
        );
    }
}
