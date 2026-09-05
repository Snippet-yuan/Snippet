package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostFavoriteStatusResponse;
import com.snippet.post.entity.Post;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceFavoriteTest {

    @Mock
    private PostMapper postMapper;

    @Test
    void insertsFavoriteForPublishedPost() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.existsFavorite(100L, 42L)).thenReturn(false);
        when(postMapper.insertFavorite(100L, 42L)).thenReturn(1);

        PostFavoriteStatusResponse response = service().favoritePost(42L, 100L);

        assertTrue(response.isFavorited());
        verify(postMapper).selectPublishedById(100L);
        verify(postMapper).existsFavorite(100L, 42L);
        verify(postMapper).insertFavorite(100L, 42L);
    }

    @Test
    void repeatedFavoriteRemainsSuccessfulWithoutDuplicateInsert() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.existsFavorite(100L, 42L)).thenReturn(true);

        PostFavoriteStatusResponse response = service().favoritePost(42L, 100L);

        assertTrue(response.isFavorited());
        verify(postMapper, never()).insertFavorite(100L, 42L);
    }

    @Test
    void concurrentDuplicateFavoriteIsTreatedAsAlreadyFavorited() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.existsFavorite(100L, 42L)).thenReturn(false);
        when(postMapper.insertFavorite(100L, 42L))
                .thenThrow(new DuplicateKeyException("duplicate favorite"));

        PostFavoriteStatusResponse response = service().favoritePost(42L, 100L);

        assertTrue(response.isFavorited());
    }

    @Test
    void deletesFavoriteAndReturnsNotFavorited() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.deleteFavorite(100L, 42L)).thenReturn(1);

        PostFavoriteStatusResponse response = service().unfavoritePost(42L, 100L);

        assertFalse(response.isFavorited());
        verify(postMapper).deleteFavorite(100L, 42L);
    }

    @Test
    void repeatedUnfavoriteRemainsSuccessful() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.deleteFavorite(100L, 42L)).thenReturn(0);

        PostFavoriteStatusResponse response = service().unfavoritePost(42L, 100L);

        assertFalse(response.isFavorited());
    }

    @Test
    void queryingFavoriteStatusReturnsCurrentRelationState() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.existsFavorite(100L, 42L)).thenReturn(true);

        PostFavoriteStatusResponse response = service().getFavoriteStatus(42L, 100L);

        assertTrue(response.isFavorited());
        verify(postMapper).existsFavorite(100L, 42L);
    }

    @Test
    void draftCannotBeFavorited() {
        when(postMapper.selectPublishedById(100L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().favoritePost(42L, 100L)
        );

        assertEquals(404, exception.getCode());
        verify(postMapper, never()).existsFavorite(100L, 42L);
        verify(postMapper, never()).insertFavorite(100L, 42L);
    }

    private Post publishedPost() {
        Post post = new Post();
        post.setId(100L);
        post.setStatus("PUBLISHED");
        return post;
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(
                postMapper,
                new ObjectMapper()
        );
    }
}
