package com.snippet.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.auth.controller.AuthController;
import com.snippet.auth.dto.ChangePasswordRequest;
import com.snippet.auth.dto.LoginRequest;
import com.snippet.auth.dto.LoginResponse;
import com.snippet.auth.dto.RegisterRequest;
import com.snippet.auth.service.AuthService;
import com.snippet.auth.vo.UserInfoVO;
import com.snippet.common.exception.BusinessException;
import com.snippet.common.exception.GlobalExceptionHandler;
import com.snippet.security.handler.RestAccessDeniedHandler;
import com.snippet.security.handler.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HttpErrorContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void invalidRequestReturnsHttp400() throws Exception {
        MockMvc mockMvc = mockMvcWith(new StubAuthService());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void duplicateUsernameReturnsHttp409() throws Exception {
        StubAuthService authService = new StubAuthService();
        authService.registerFailure = new BusinessException(
                HttpStatus.CONFLICT,
                "用户已存在"
        );

        mockMvcWith(authService)
                .perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"snippetuser1","password":"1234567890"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("用户已存在"));
    }

    @Test
    void wrongCredentialsReturnHttp401() throws Exception {
        StubAuthService authService = new StubAuthService();
        authService.loginFailure = new BusinessException(
                HttpStatus.UNAUTHORIZED,
                "账号或密码错误"
        );

        mockMvcWith(authService)
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"snippetuser1","password":"1234567890"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号或密码错误"));
    }

    @Test
    void securityHandlersReturnJson401And403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse unauthorizedResponse = new MockHttpServletResponse();
        new RestAuthenticationEntryPoint(objectMapper).commence(
                request,
                unauthorizedResponse,
                new BadCredentialsException("bad token")
        );
        JsonNode unauthorizedBody = objectMapper.readTree(unauthorizedResponse.getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals(401, unauthorizedResponse.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(401, unauthorizedBody.get("code").asInt());

        MockHttpServletResponse forbiddenResponse = new MockHttpServletResponse();
        new RestAccessDeniedHandler(objectMapper).handle(
                request,
                forbiddenResponse,
                new AccessDeniedException("forbidden")
        );
        JsonNode forbiddenBody = objectMapper.readTree(forbiddenResponse.getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals(403, forbiddenResponse.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(403, forbiddenBody.get("code").asInt());
    }

    private MockMvc mockMvcWith(AuthService authService) {
        return MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static class StubAuthService implements AuthService {

        private RuntimeException registerFailure;
        private RuntimeException loginFailure;

        @Override
        public UserInfoVO register(RegisterRequest request) {
            if (registerFailure != null) {
                throw registerFailure;
            }
            return null;
        }

        @Override
        public LoginResponse login(LoginRequest request) {
            if (loginFailure != null) {
                throw loginFailure;
            }
            return null;
        }

        @Override
        public void changePassword(Long userId, ChangePasswordRequest request) {
            // 当前错误契约测试不覆盖密码修改业务。
        }
    }
}
