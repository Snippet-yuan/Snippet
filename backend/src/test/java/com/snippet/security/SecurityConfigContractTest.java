package com.snippet.security;

import com.snippet.security.handler.RestAccessDeniedHandler;
import com.snippet.security.handler.RestAuthenticationEntryPoint;
import com.snippet.user.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        SecurityConfigContractTest.JwtTestConfig.class
})
class SecurityConfigContractTest {

    private final MockMvc mockMvc;

    @Autowired
    SecurityConfigContractTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void requestWithoutTokenReturnsJson401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或访问凭证无效"));
    }

    @TestConfiguration
    static class JwtTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new UnsupportedOperationException("JWT decoding is not used in this test");
            };
        }
    }
}
