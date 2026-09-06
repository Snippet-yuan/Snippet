package com.snippet.social.service;

import com.snippet.common.exception.BusinessException;
import com.snippet.social.dto.FollowStatusResponse;
import com.snippet.social.dto.UserSummaryResponse;
import com.snippet.social.entity.RelationUser;
import com.snippet.social.mapper.FollowMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowMapper followMapper;

    @Test
    void followingListUsesFollowerIdAndPagination() {
        when(followMapper.selectFollowingUsers(42L, 10, 20))
                .thenReturn(List.of(new RelationUser(43L, "snippetuser2", "用户二", 11L)));

        List<UserSummaryResponse> result = service().getFollowing(42L, 10, 20);

        assertEquals(1, result.size());
        assertEquals(43L, result.get(0).getId());
        assertEquals("snippetuser2", result.get(0).getUsername());
        assertEquals("用户二", result.get(0).getNickname());
        assertEquals(11L, result.get(0).getAvatarAssetId());
        verify(followMapper).selectFollowingUsers(42L, 10, 20);
    }

    @Test
    void followValidatesTargetAndCreatesOneWayRelation() {
        when(followMapper.selectActiveUserById(43L))
                .thenReturn(new RelationUser(43L, "snippetuser2", "用户二", null));
        when(followMapper.existsFollow(42L, 43L)).thenReturn(false);
        when(followMapper.insertFollow(42L, 43L)).thenReturn(1);

        FollowStatusResponse result = service().follow(42L, 43L);

        assertEquals(true, result.isFollowing());
        verify(followMapper).insertFollow(42L, 43L);
    }

    @Test
    void repeatedFollowIsIdempotent() {
        when(followMapper.selectActiveUserById(43L))
                .thenReturn(new RelationUser(43L, "snippetuser2", "用户二", null));
        when(followMapper.existsFollow(42L, 43L)).thenReturn(true);

        FollowStatusResponse result = service().follow(42L, 43L);

        assertEquals(true, result.isFollowing());
        verify(followMapper, never()).insertFollow(42L, 43L);
    }

    @Test
    void unfollowIsIdempotent() {
        when(followMapper.selectActiveUserById(43L))
                .thenReturn(new RelationUser(43L, "snippetuser2", "用户二", null));
        when(followMapper.deleteFollow(42L, 43L)).thenReturn(0);

        FollowStatusResponse result = service().unfollow(42L, 43L);

        assertEquals(false, result.isFollowing());
        verify(followMapper).deleteFollow(42L, 43L);
    }

    @Test
    void statusReadsOneWayRelationOnly() {
        when(followMapper.selectActiveUserById(43L))
                .thenReturn(new RelationUser(43L, "snippetuser2", "用户二", null));
        when(followMapper.existsFollow(42L, 43L)).thenReturn(false);

        FollowStatusResponse result = service().getFollowingStatus(42L, 43L);

        assertEquals(false, result.isFollowing());
        verify(followMapper).existsFollow(42L, 43L);
    }

    @Test
    void cannotFollowSelf() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().follow(42L, 42L)
        );

        assertEquals(400, exception.getCode());
        verify(followMapper, never()).insertFollow(42L, 42L);
    }

    private FollowServiceImpl service() {
        return new FollowServiceImpl(followMapper);
    }
}
