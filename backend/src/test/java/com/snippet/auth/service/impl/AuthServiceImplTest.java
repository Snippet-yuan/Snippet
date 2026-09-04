package com.snippet.auth.service.impl;

import com.snippet.auth.dto.ChangePasswordRequest;
import com.snippet.auth.entity.UserAccount;
import com.snippet.auth.mapper.UserAccountMapper;
import com.snippet.security.token.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.snippet.common.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Test
    void changePasswordVerifiesCurrentPasswordAndStoresNewHash() {
        UserAccount user = new UserAccount();
        user.setId(42L);
        user.setUsername("snippetuser1");
        user.setPasswordHash("old-hash");
        user.setStatus("ACTIVE");
        when(userAccountMapper.selectById(42L)).thenReturn(user);
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userAccountMapper.updatePassword(42L, "new-hash")).thenReturn(1);

        ChangePasswordRequest request = request("old-password", "new-password", "new-password");
        AuthServiceImpl service = new AuthServiceImpl(
                userAccountMapper,
                passwordEncoder,
                tokenService
        );

        service.changePassword(42L, request);

        verify(passwordEncoder).matches("old-password", "old-hash");
        verify(passwordEncoder).encode("new-password");
        verify(userAccountMapper).updatePassword(42L, "new-hash");
    }

    @Test
    void wrongCurrentPasswordDoesNotUpdateDatabase() {
        UserAccount user = new UserAccount();
        user.setId(42L);
        user.setPasswordHash("old-hash");
        user.setStatus("ACTIVE");
        when(userAccountMapper.selectById(42L)).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        AuthServiceImpl service = new AuthServiceImpl(
                userAccountMapper,
                passwordEncoder,
                tokenService
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.changePassword(
                        42L,
                        request("wrong-password", "new-password", "new-password")
                )
        );

        assertEquals(400, exception.getCode());
        assertEquals("当前密码错误", exception.getMessage());
        verifyNoInteractionsAfterPasswordMatch();
    }

    @Test
    void mismatchedConfirmationDoesNotUpdateDatabase() {
        UserAccount user = new UserAccount();
        user.setId(42L);
        user.setPasswordHash("old-hash");
        user.setStatus("ACTIVE");
        when(userAccountMapper.selectById(42L)).thenReturn(user);
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);

        AuthServiceImpl service = new AuthServiceImpl(
                userAccountMapper,
                passwordEncoder,
                tokenService
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.changePassword(
                        42L,
                        request("old-password", "new-password", "different-password")
                )
        );

        assertEquals(400, exception.getCode());
        assertEquals("两次新密码不一致", exception.getMessage());
        verifyNoInteractionsAfterPasswordMatch();
    }

    private ChangePasswordRequest request(
            String currentPassword,
            String newPassword,
            String confirmPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private void verifyNoInteractionsAfterPasswordMatch() {
        org.mockito.Mockito.verify(passwordEncoder, org.mockito.Mockito.never())
                .encode(org.mockito.ArgumentMatchers.anyString());
        org.mockito.Mockito.verify(userAccountMapper, org.mockito.Mockito.never())
                .updatePassword(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }
}
