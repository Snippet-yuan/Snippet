package com.snippet.user.controller;

import com.snippet.common.exception.BusinessException;
import com.snippet.user.dto.UpdateProfileRequest;
import com.snippet.user.dto.UserProfileResponse;
import com.snippet.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void currentUserPassesJwtSubjectToService() {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        UserProfileResponse expected = new UserProfileResponse(
                42L,
                "snippetuser1",
                "user@example.com",
                "Snippet 用户",
                10L,
                11L
        );
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        when(userService.getCurrentUser(42L)).thenReturn(expected);

        var result = controller.currentUser(jwt);

        assertEquals(expected, result.getData());
        verify(userService).getCurrentUser(42L);
    }

    @Test
    void updateProfilePassesJwtSubjectAndRequestToService() {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("新昵称");
        UserProfileResponse expected = new UserProfileResponse(
                42L,
                "snippetuser1",
                null,
                "新昵称",
                null,
                null
        );
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        when(userService.updateProfile(42L, request)).thenReturn(expected);

        var result = controller.updateProfile(jwt, request);

        assertEquals(expected, result.getData());
        verify(userService).updateProfile(42L, request);
    }

    @Test
    void malformedJwtSubjectIsRejectedAndIsNotPassedToService() {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("not-a-user-id")
                .build();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.currentUser(jwt)
        );

        assertEquals(HttpStatus.UNAUTHORIZED.value(), exception.getCode());
        verifyNoInteractions(userService);
    }
}
