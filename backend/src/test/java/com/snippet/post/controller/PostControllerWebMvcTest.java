package com.snippet.post.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.auth.mapper.UserAccountMapper;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.PostFavoriteItemResponse;
import com.snippet.post.dto.PostFavoriteStatusResponse;
import com.snippet.post.dto.PostLikeStatusResponse;
import com.snippet.post.dto.PostSummaryResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        PostControllerWebMvcTest.JwtTestConfig.class
})
class PostControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostService postService;

    @BeforeEach
    void clearServiceInvocations() {
        clearInvocations(postService);
    }

    @Test
    void authenticatedCreateUsesJwtSubjectAndReturnsPostResponse() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode content = objectMapper.readTree("{\"type\":\"doc\",\"children\":[]}");
        when(postService.createPost(eq(42L), any())).thenReturn(
                new PostDetailResponse(
                        100L,
                        "第一篇帖子",
                        "post-test",
                        "DRAFT",
                        content,
                        1,
                        0,
                        null,
                        null,
                        null
                )
        );

        mockMvc.perform(post("/api/v1/posts")
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType("application/json")
                        .content("{\"title\":\"第一篇帖子\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.title").value("第一篇帖子"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.version").value(0));

        verify(postService).createPost(eq(42L), any());
    }

    @Test
    void rejectsOwnerIdFromRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType("application/json")
                        .content("{\"title\":\"第一篇帖子\",\"ownerId\":999}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticatedGetUsesJwtSubjectAndReturnsPostDetail() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        when(postService.getPostDetail(42L, 100L)).thenReturn(
                new PostDetailResponse(
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
                )
        );

        mockMvc.perform(get("/api/v1/posts/{postId}", 100L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.title").value("第一篇帖子"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        verify(postService).getPostDetail(42L, 100L);
    }

    @Test
    void authenticatedSaveDraftUsesJwtSubjectAndReturnsUpdatedDraft() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode content = objectMapper.readTree(
                "{\"type\":\"doc\",\"children\":[{\"type\":\"paragraph\",\"children\":[{\"text\":\"这是测试正文\"}]}]}"
        );
        when(postService.saveDraft(eq(42L), eq(100L), any())).thenReturn(
                new PostDetailResponse(
                        100L,
                        "测试帖子",
                        "post-test",
                        "DRAFT",
                        content,
                        1,
                        1,
                        null,
                        null,
                        null
                )
        );

        mockMvc.perform(put("/api/v1/posts/{postId}/draft", 100L)
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType("application/json")
                        .content("{\"content\":{\"type\":\"doc\",\"children\":[{\"type\":\"paragraph\",\"children\":[{\"text\":\"这是测试正文\"}]}]},\"schemaVersion\":1,\"version\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.version").value(1))
                .andExpect(jsonPath("$.data.content.children[0].children[0].text").value("这是测试正文"));

        verify(postService).saveDraft(eq(42L), eq(100L), any());
    }

    @Test
    void authenticatedUpdateUsesJwtSubjectAndReturnsUpdatedBasicInfo() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        when(postService.updatePost(eq(42L), eq(100L), any())).thenReturn(
                new PostDetailResponse(
                        100L,
                        "修改后的标题",
                        "修改后的描述",
                        "post-test",
                        "DRAFT",
                        objectMapper.readTree("""
                                {"type":"doc","children":[]}
                                """),
                        1,
                        0,
                        null,
                        null,
                        null
                )
        );

        mockMvc.perform(patch("/api/v1/posts/{postId}", 100L)
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType("application/json")
                        .content("""
                                {"title":"修改后的标题","description":"修改后的描述"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("修改后的标题"))
                .andExpect(jsonPath("$.data.description").value("修改后的描述"));

        verify(postService).updatePost(eq(42L), eq(100L), any());
    }

    @Test
    void authenticatedDeleteUsesJwtSubject() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/{postId}", 100L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(postService).deletePost(42L, 100L);
    }

    @Test
    void authenticatedPublishUsesJwtSubjectAndReturnsPublishedPost() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        when(postService.publishPost(eq(42L), eq(100L), any())).thenReturn(
                new PostDetailResponse(
                        100L,
                        "发布测试帖子",
                        "发布测试描述",
                        "post-test",
                        "PUBLISHED",
                        objectMapper.readTree("""
                                {"type":"doc","children":[]}
                                """),
                        1,
                        0,
                        null,
                        null,
                        null
                )
        );

        mockMvc.perform(post("/api/v1/posts/{postId}/publish", 100L)
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType("application/json")
                        .content("""
                                {"expectedVersion":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        verify(postService).publishPost(eq(42L), eq(100L), any());
    }

    @Test
    void publicGetDoesNotRequireAuthentication() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        when(postService.getPublicPost("post-public")).thenReturn(
                new PostDetailResponse(
                        100L,
                        "公开帖子",
                        "公开描述",
                        "post-public",
                        "PUBLISHED",
                        objectMapper.readTree("""
                                {"type":"doc","children":[]}
                                """),
                        1,
                        3,
                        null,
                        null,
                        null
                )
        );

        mockMvc.perform(get("/api/v1/public/posts/{slug}", "post-public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.version").value(3));

        verify(postService).getPublicPost("post-public");
    }

    @Test
    void authenticatedLikeUsesJwtSubject() throws Exception {
        when(postService.likePost(42L, 100L)).thenReturn(
                new PostLikeStatusResponse(true)
        );

        mockMvc.perform(post("/api/v1/posts/{postId}/like", 100L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.liked").value(true));

        verify(postService).likePost(42L, 100L);
    }

    @Test
    void authenticatedUnlikeUsesJwtSubject() throws Exception {
        when(postService.unlikePost(42L, 100L)).thenReturn(
                new PostLikeStatusResponse(false)
        );

        mockMvc.perform(delete("/api/v1/posts/{postId}/like", 100L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.liked").value(false));

        verify(postService).unlikePost(42L, 100L);
    }

    @Test
    void authenticatedLikeStatusUsesJwtSubject() throws Exception {
        when(postService.getLikeStatus(42L, 100L)).thenReturn(
                new PostLikeStatusResponse(true)
        );

        mockMvc.perform(get("/api/v1/posts/{postId}/like", 100L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.liked").value(true));

        verify(postService).getLikeStatus(42L, 100L);
    }

    @Test
    void likeRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/posts/{postId}/like", 100L))
                .andExpect(status().isUnauthorized());

        verify(postService, org.mockito.Mockito.never()).likePost(42L, 100L);
    }

    @Test
    void authenticatedFavoriteUsesJwtSubject() throws Exception {
        when(postService.favoritePost(42L, 100L)).thenReturn(
                new PostFavoriteStatusResponse(true)
        );

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", 100L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.favorited").value(true));

        verify(postService).favoritePost(42L, 100L);
    }

    @Test
    void authenticatedUnfavoriteUsesJwtSubject() throws Exception {
        when(postService.unfavoritePost(42L, 100L)).thenReturn(
                new PostFavoriteStatusResponse(false)
        );

        mockMvc.perform(delete("/api/v1/posts/{postId}/favorite", 100L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.favorited").value(false));

        verify(postService).unfavoritePost(42L, 100L);
    }

    @Test
    void authenticatedFavoriteStatusUsesJwtSubject() throws Exception {
        when(postService.getFavoriteStatus(42L, 100L)).thenReturn(
                new PostFavoriteStatusResponse(true)
        );

        mockMvc.perform(get("/api/v1/posts/{postId}/favorite", 100L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.favorited").value(true));

        verify(postService).getFavoriteStatus(42L, 100L);
    }

    @Test
    void favoriteRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", 100L))
                .andExpect(status().isUnauthorized());

        verify(postService, org.mockito.Mockito.never()).favoritePost(42L, 100L);
    }

    @Test
    void authenticatedFavoriteListUsesJwtSubjectAndPagination() throws Exception {
        when(postService.getFavoritePosts(42L, 10, 20)).thenReturn(
                List.of(new PostFavoriteItemResponse(
                        100L,
                        "第一篇帖子",
                        "帖子简介",
                        "post-first",
                        null,
                        null
                ))
        );

        mockMvc.perform(get("/api/v1/me/favorites")
                        .param("limit", "10")
                        .param("offset", "20")
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].postId").value(100))
                .andExpect(jsonPath("$.data[0].slug").value("post-first"));

        verify(postService).getFavoritePosts(42L, 10, 20);
    }

    @Test
    void favoriteListRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/me/favorites"))
                .andExpect(status().isUnauthorized());

        verify(postService, org.mockito.Mockito.never())
                .getFavoritePosts(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void publicPostListDoesNotRequireAuthentication() throws Exception {
        when(postService.getPublicPosts(10, 20)).thenReturn(
                List.of(new PostSummaryResponse(
                        100L,
                        "公开帖子",
                        "公开描述",
                        "post-public",
                        "PUBLISHED",
                        null,
                        null,
                        null
                ))
        );

        mockMvc.perform(get("/api/v1/public/posts")
                        .param("limit", "10")
                        .param("offset", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"));

        verify(postService).getPublicPosts(10, 20);
    }

    @Test
    void myPostListUsesJwtSubjectAndPagination() throws Exception {
        when(postService.getMyPosts(42L, 10, 20)).thenReturn(
                List.of(new PostSummaryResponse(
                        101L,
                        "我的草稿",
                        null,
                        "post-draft",
                        "DRAFT",
                        null,
                        null,
                        null
                ))
        );

        mockMvc.perform(get("/api/v1/users/me/posts")
                        .param("limit", "10")
                        .param("offset", "20")
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].status").value("DRAFT"));

        verify(postService).getMyPosts(42L, 10, 20);
    }

    @Test
    void myPostListRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/posts"))
                .andExpect(status().isUnauthorized());

        verify(postService, org.mockito.Mockito.never())
                .getMyPosts(any(), any(), any());
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
