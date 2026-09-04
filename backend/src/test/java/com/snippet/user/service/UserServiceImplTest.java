package com.snippet.user.service;

import com.snippet.asset.mapper.AssetMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.user.dto.UpdateProfileRequest;
import com.snippet.user.entity.UserProfile;
import com.snippet.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private AssetMapper assetMapper;

    @Test
    void returnsOnlyPublicProfileFieldsForActiveUser() {
        when(userMapper.selectById(42L)).thenReturn(
                new UserProfile(
                        42L,
                        "snippetuser1",
                        "user@example.com",
                        "Snippet 用户",
                        10L,
                        11L,
                        "ACTIVE"
                )
        );
        UserServiceImpl service = new UserServiceImpl(userMapper, assetMapper);

        var response = service.getCurrentUser(42L);

        assertEquals(42L, response.getId());
        assertEquals("snippetuser1", response.getUsername());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("Snippet 用户", response.getNickname());
        assertEquals(10L, response.getAvatarAssetId());
        assertEquals(11L, response.getBackgroundAssetId());
        verify(userMapper).selectById(42L);
    }

    @Test
    void updatesProfileFieldsAfterCheckingOwnedAssets() {
        UserProfile current = new UserProfile(
                42L,
                "snippetuser1",
                "old@example.com",
                "旧昵称",
                null,
                null,
                "ACTIVE"
        );
        UserProfile updated = new UserProfile(
                42L,
                "snippetuser1",
                "new@example.com",
                "新昵称",
                10L,
                11L,
                "ACTIVE"
        );
        when(userMapper.selectById(42L)).thenReturn(current, updated);
        when(userMapper.selectByEmail("new@example.com")).thenReturn(null);
        when(assetMapper.existsOwnedReadyById(10L, 42L)).thenReturn(true);
        when(assetMapper.existsOwnedReadyById(11L, 42L)).thenReturn(true);
        when(userMapper.updateProfile(
                42L,
                null,
                "new@example.com",
                "新昵称",
                10L,
                11L
        )).thenReturn(1);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("new@example.com");
        request.setNickname("新昵称");
        request.setAvatarAssetId(10L);
        request.setBackgroundAssetId(11L);
        UserServiceImpl service = new UserServiceImpl(userMapper, assetMapper);

        var response = service.updateProfile(42L, request);

        assertEquals("new@example.com", response.getEmail());
        assertEquals("新昵称", response.getNickname());
        verify(userMapper).updateProfile(
                42L,
                null,
                "new@example.com",
                "新昵称",
                10L,
                11L
        );
        verify(assetMapper).existsOwnedReadyById(10L, 42L);
        verify(assetMapper).existsOwnedReadyById(11L, 42L);
    }

    @Test
    void rejectsAssetOwnedByAnotherUserBeforeUpdate() {
        when(userMapper.selectById(42L)).thenReturn(
                new UserProfile(42L, "snippetuser1", null, null, null, null, "ACTIVE")
        );
        when(assetMapper.existsOwnedReadyById(10L, 42L)).thenReturn(false);
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setAvatarAssetId(10L);
        UserServiceImpl service = new UserServiceImpl(userMapper, assetMapper);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.updateProfile(42L, request)
        );

        assertEquals(400, exception.getCode());
        verifyNoInteractionsAfterAssetCheck();
    }

    @Test
    void missingUserReturnsNotFound() {
        when(userMapper.selectById(42L)).thenReturn(null);
        UserServiceImpl service = new UserServiceImpl(userMapper, assetMapper);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getCurrentUser(42L)
        );

        assertEquals(404, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void inactiveUserCannotReadProfile() {
        when(userMapper.selectById(42L)).thenReturn(
                new UserProfile(42L, "snippetuser1", null, null, null, null, "DISABLED")
        );
        UserServiceImpl service = new UserServiceImpl(userMapper, assetMapper);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getCurrentUser(42L)
        );

        assertEquals(403, exception.getCode());
        assertEquals("用户账号不可用", exception.getMessage());
    }

    @Test
    void invalidUserIdIsRejectedBeforeDatabaseQuery() {
        UserServiceImpl service = new UserServiceImpl(userMapper, assetMapper);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getCurrentUser(0L)
        );

        assertEquals(401, exception.getCode());
        verifyNoInteractions(userMapper, assetMapper);
    }

    private void verifyNoInteractionsAfterAssetCheck() {
        org.mockito.Mockito.verify(userMapper, org.mockito.Mockito.never())
                .updateProfile(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }
}
