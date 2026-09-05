package com.snippet.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostControllerDetailTest {

    private final PostService postService = mock(PostService.class);
    private final PostController controller = new PostController(postService);

    @Test
    void getsDetailUsingJwtSubjectAsOwnerId() throws Exception {
        PostDetailResponse expected = new PostDetailResponse(
                100L,
                "我的帖子",
                "post-test",
                "DRAFT",
                new ObjectMapper().readTree("{\"type\":\"doc\",\"children\":[]}"),
                1,
                0,
                null,
                null,
                null
        );
        when(postService.getPostDetail(42L, 100L)).thenReturn(expected);

        var result = controller.getDetail(100L, jwt("42"));

        assertEquals(expected, result.getData());
        verify(postService).getPostDetail(42L, 100L);
    }

    @Test
    void rejectsMalformedJwtSubjectBeforeCallingService() {
        assertThrows(
                BusinessException.class,
                () -> controller.getDetail(100L, jwt("not-a-number"))
        );

        verify(postService, never()).getPostDetail(anyLong(), anyLong());
    }

    private Jwt jwt(String subject) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
