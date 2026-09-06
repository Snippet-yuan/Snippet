package com.snippet.social.controller;

import com.snippet.auth.mapper.UserAccountMapper;
import com.snippet.security.SecurityConfig;
import com.snippet.security.handler.RestAccessDeniedHandler;
import com.snippet.security.handler.RestAuthenticationEntryPoint;
import com.snippet.security.token.JwtTokenVersionAuthenticationConverter;
import com.snippet.social.dto.FollowStatusResponse;
import com.snippet.social.dto.UserSummaryResponse;
import com.snippet.social.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FollowController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        FollowControllerWebMvcTest.JwtTestConfig.class
})
class FollowControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FollowService followService;

    @Test
    void followingListUsesJwtSubjectAndPagination() throws Exception {
        when(followService.getFollowing(42L, 10, 20))
                .thenReturn(List.of(new UserSummaryResponse(43L, "snippetuser2", "用户二", 11L)));

        mockMvc.perform(get("/api/v1/users/me/following")
                        .param("limit", "10")
                        .param("offset", "20")
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(43))
                .andExpect(jsonPath("$.data[0].username").value("snippetuser2"))
                .andExpect(jsonPath("$.data[0].email").doesNotExist());

        verify(followService).getFollowing(42L, 10, 20);
    }

    @Test
    void followUsesJwtSubjectAndTargetPathVariable() throws Exception {
        when(followService.follow(42L, 43L)).thenReturn(new FollowStatusResponse(true));

        mockMvc.perform(post("/api/v1/users/{followingId}/follow", 43L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));

        verify(followService).follow(42L, 43L);
    }

    @Test
    void unfollowUsesJwtSubjectAndTargetPathVariable() throws Exception {
        when(followService.unfollow(42L, 43L)).thenReturn(new FollowStatusResponse(false));

        mockMvc.perform(delete("/api/v1/users/{followingId}/follow", 43L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(false));

        verify(followService).unfollow(42L, 43L);
    }

    @Test
    void statusUsesJwtSubjectAndTargetPathVariable() throws Exception {
        when(followService.getFollowingStatus(42L, 43L))
                .thenReturn(new FollowStatusResponse(true));

        mockMvc.perform(get("/api/v1/users/{followingId}/follow", 43L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));

        verify(followService).getFollowingStatus(42L, 43L);
    }

    @Test
    void followEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/following"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/users/43/follow"))
                .andExpect(status().isUnauthorized());
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
        FollowService followService() {
            return mock(FollowService.class);
        }
    }
}
