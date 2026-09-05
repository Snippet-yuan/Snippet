package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostLikeStatusResponse;
import com.snippet.post.entity.Post;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceLikeTest {

    @Mock
    private PostMapper postMapper;

    @Test
    void insertsLikeForPublishedPost() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.existsLike(100L, 42L)).thenReturn(false);
        when(postMapper.insertLike(100L, 42L)).thenReturn(1);

        PostLikeStatusResponse response = service().likePost(42L, 100L);

        assertTrue(response.isLiked());
        verify(postMapper).selectPublishedById(100L);
        verify(postMapper).existsLike(100L, 42L);
        verify(postMapper).insertLike(100L, 42L);
    }

    @Test
    void repeatedLikeRemainsSuccessfulWithoutDuplicateInsert() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.existsLike(100L, 42L)).thenReturn(true);

        PostLikeStatusResponse response = service().likePost(42L, 100L);

        assertTrue(response.isLiked());
        verify(postMapper, never()).insertLike(100L, 42L);
    }

    @Test
    void concurrentDuplicateLikeIsTreatedAsAlreadyLiked() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.existsLike(100L, 42L)).thenReturn(false);
        when(postMapper.insertLike(100L, 42L))
                .thenThrow(new DuplicateKeyException("duplicate like"));

        PostLikeStatusResponse response = service().likePost(42L, 100L);

        assertTrue(response.isLiked());
    }

    @Test
    void deletesLikeAndReturnsNotLiked() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.deleteLike(100L, 42L)).thenReturn(1);

        PostLikeStatusResponse response = service().unlikePost(42L, 100L);

        assertFalse(response.isLiked());
        verify(postMapper).deleteLike(100L, 42L);
    }

    @Test
    void queryingStatusReturnsCurrentRelationState() {
        when(postMapper.selectPublishedById(100L)).thenReturn(publishedPost());
        when(postMapper.existsLike(100L, 42L)).thenReturn(true);

        PostLikeStatusResponse response = service().getLikeStatus(42L, 100L);

        assertTrue(response.isLiked());
        verify(postMapper).existsLike(100L, 42L);
    }

    @Test
    void draftCannotBeLiked() {
        when(postMapper.selectPublishedById(100L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().likePost(42L, 100L)
        );

        assertEquals(404, exception.getCode());
        verify(postMapper, never()).existsLike(100L, 42L);
        verify(postMapper, never()).insertLike(100L, 42L);
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
