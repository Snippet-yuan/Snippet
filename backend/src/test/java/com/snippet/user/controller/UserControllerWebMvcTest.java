package com.snippet.user.controller;

import com.snippet.auth.mapper.UserAccountMapper;
import com.snippet.security.SecurityConfig;
import com.snippet.security.handler.RestAccessDeniedHandler;
import com.snippet.security.handler.RestAuthenticationEntryPoint;
import com.snippet.security.token.JwtTokenVersionAuthenticationConverter;
import com.snippet.user.dto.UserProfileResponse;
import com.snippet.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        UserControllerWebMvcTest.JwtTestConfig.class
})
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void authenticatedRequestUsesJwtSubjectAndReturnsWhitelistedFields() throws Exception {
        when(userService.getCurrentUser(42L)).thenReturn(
                new UserProfileResponse(
                        42L,
                        "snippetuser1",
                        "user@example.com",
                        "Snippet 用户",
                        10L,
                        11L
                )
        );

        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.username").value("snippetuser1"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("Snippet 用户"))
                .andExpect(jsonPath("$.data.avatarAssetId").value(10))
                .andExpect(jsonPath("$.data.backgroundAssetId").value(11))
                .andExpect(jsonPath("$.data.status").doesNotExist());

        verify(userService).getCurrentUser(42L);
    }

    @Test
    void authenticatedPatchUsesJwtSubjectAndRequestBody() throws Exception {
        when(userService.updateProfile(eq(42L), any())).thenReturn(
                new UserProfileResponse(
                        42L,
                        "snippetuser1",
                        "user@example.com",
                        "新昵称",
                        10L,
                        11L
                )
        );

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(jwt().jwt(token -> token.subject("42")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"新昵称\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"));

        verify(userService).updateProfile(eq(42L), any());
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
        UserService userService() {
            return mock(UserService.class);
        }
    }
}
