package com.snippet.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.CreatePostRequest;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostControllerTest {

    private final PostService postService = mock(PostService.class);
    private final PostController controller = new PostController(postService);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createPostUsesJwtSubjectAsOwnerId() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("第一篇帖子");
        PostDetailResponse expected = new PostDetailResponse(
                100L,
                "第一篇帖子",
                "post-test",
                "DRAFT",
                objectMapper.readTree("{\"type\":\"doc\",\"children\":[]}"),
                1,
                0,
                null,
                null,
                null
        );
        when(postService.createPost(eq(42L), any(CreatePostRequest.class)))
                .thenReturn(expected);

        var result = controller.createPost(jwt("42"), request);

        assertEquals(expected, result.getData());
        verify(postService).createPost(eq(42L), eq(request));
    }

    @Test
    void rejectsMalformedJwtSubjectBeforeCallingService() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("第一篇帖子");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.createPost(jwt("not-a-number"), request)
        );

        assertEquals(401, exception.getCode());
        verify(postService, org.mockito.Mockito.never())
                .createPost(any(), any());
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
