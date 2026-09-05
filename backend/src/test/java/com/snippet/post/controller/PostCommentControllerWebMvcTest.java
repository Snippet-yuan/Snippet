package com.snippet.post.controller;

import com.snippet.auth.mapper.UserAccountMapper;
import com.snippet.post.dto.PostCommentResponse;
import com.snippet.post.service.PostService;
import com.snippet.security.SecurityConfig;
import com.snippet.security.handler.RestAccessDeniedHandler;
import com.snippet.security.handler.RestAuthenticationEntryPoint;
import com.snippet.security.token.JwtTokenVersionAuthenticationConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        PostCommentControllerWebMvcTest.JwtTestConfig.class
})
class PostCommentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostService postService;

    @BeforeEach
    void clearServiceInvocations() {
        clearInvocations(postService);
    }

    @Test
    void authenticatedCreateUsesJwtSubject() throws Exception {
        when(postService.createComment(eq(42L), eq(100L), any())).thenReturn(
                new PostCommentResponse(
                        501L,
                        100L,
                        42L,
                        "Snippet 用户",
                        null,
                        "评论正文",
                        LocalDateTime.of(2026, 9, 5, 12, 30)
                )
        );

        mockMvc.perform(post("/api/v1/posts/{postId}/comments", 100L)
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType("application/json")
                        .content("{\"content\":\"评论正文\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.authorId").value(42))
                .andExpect(jsonPath("$.data.content").value("评论正文"));

        verify(postService).createComment(eq(42L), eq(100L), any());
    }

    @Test
    void publicListDoesNotRequireAuthentication() throws Exception {
        when(postService.getPublicComments("post-public", 20, 0)).thenReturn(
                List.of(new PostCommentResponse(
                        501L,
                        100L,
                        42L,
                        "Snippet 用户",
                        null,
                        "公开评论",
                        LocalDateTime.of(2026, 9, 5, 12, 30)
                ))
        );

        mockMvc.perform(get("/api/v1/public/posts/{slug}/comments", "post-public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(501))
                .andExpect(jsonPath("$.data[0].authorName").value("Snippet 用户"))
                .andExpect(jsonPath("$.data[0].content").value("公开评论"));

        verify(postService).getPublicComments("post-public", 20, 0);
    }

    @Test
    void authenticatedDeleteUsesJwtSubject() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/posts/{postId}/comments/{commentId}",
                        100L,
                        501L
                )
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(postService).deleteComment(42L, 100L, 501L);
    }

    @Test
    void createRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", 100L)
                        .contentType("application/json")
                        .content("{\"content\":\"评论正文\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAuthorIdFromRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", 100L)
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType("application/json")
                        .content("{\"content\":\"评论正文\",\"authorId\":999}"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class JwtTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new UnsupportedOperationException("JWT decoding is not used in this test");
            };
        }

        @Bean
        UserAccountMapper userAccountMapper() {
            return mock(UserAccountMapper.class);
        }

        @Bean
        JwtTokenVersionAuthenticationConverter jwtTokenVersionAuthenticationConverter(
                UserAccountMapper userAccountMapper) {
            return new JwtTokenVersionAuthenticationConverter(userAccountMapper);
        }

        @Bean
        PostService postService() {
            return mock(PostService.class);
        }
    }
}
