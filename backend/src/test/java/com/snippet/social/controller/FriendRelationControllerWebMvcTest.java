package com.snippet.social.controller;

import com.snippet.auth.mapper.UserAccountMapper;
import com.snippet.security.SecurityConfig;
import com.snippet.security.handler.RestAccessDeniedHandler;
import com.snippet.security.handler.RestAuthenticationEntryPoint;
import com.snippet.security.token.JwtTokenVersionAuthenticationConverter;
import com.snippet.social.dto.FriendRequestResponse;
import com.snippet.social.dto.UserSummaryResponse;
import com.snippet.social.service.FriendRelationService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendRelationController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        FriendRelationControllerWebMvcTest.JwtTestConfig.class
})
class FriendRelationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FriendRelationService friendRelationService;

    @Test
    void incomingRequestsUsesJwtSubject() throws Exception {
        when(friendRelationService.getIncomingRequests(42L, 20, 0))
                .thenReturn(List.of(new FriendRequestResponse(
                        501L,
                        "PENDING",
                        new UserSummaryResponse(43L, "snippetuser2", "用户二", null),
                        null
                )));

        mockMvc.perform(get("/api/v1/users/me/friend-requests")
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].requestId").value(501))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].requester.id").value(43));

        verify(friendRelationService).getIncomingRequests(42L, 20, 0);
    }

    @Test
    void sendRequestUsesJwtSubjectAndRequestTarget() throws Exception {
        when(friendRelationService.sendFriendRequest(eq(42L), any()))
                .thenReturn(new FriendRequestResponse(
                        501L,
                        "PENDING",
                        new UserSummaryResponse(42L, "snippetuser1", null, null),
                        null
                ));

        mockMvc.perform(post("/api/v1/users/me/friend-requests")
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType("application/json")
                        .content("{\"targetUserId\":43}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value(501))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        verify(friendRelationService).sendFriendRequest(eq(42L), any());
    }

    @Test
    void acceptRequestUsesJwtSubject() throws Exception {
        when(friendRelationService.acceptFriendRequest(42L, 501L))
                .thenReturn(new FriendRequestResponse(
                        501L,
                        "FRIEND",
                        new UserSummaryResponse(43L, "snippetuser2", null, null),
                        null
                ));

        mockMvc.perform(post("/api/v1/users/me/friend-requests/{requestId}/accept", 501L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FRIEND"));

        verify(friendRelationService).acceptFriendRequest(42L, 501L);
    }

    @Test
    void rejectRequestUsesJwtSubject() throws Exception {
        when(friendRelationService.rejectFriendRequest(42L, 501L))
                .thenReturn(new FriendRequestResponse(
                        501L,
                        "REJECTED",
                        new UserSummaryResponse(43L, "snippetuser2", null, null),
                        null
                ));

        mockMvc.perform(post("/api/v1/users/me/friend-requests/{requestId}/reject", 501L)
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        verify(friendRelationService).rejectFriendRequest(42L, 501L);
    }

    @Test
    void relationEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/friend-requests/501/accept"))
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
        FriendRelationService friendRelationService() {
            return mock(FriendRelationService.class);
        }
    }
}
