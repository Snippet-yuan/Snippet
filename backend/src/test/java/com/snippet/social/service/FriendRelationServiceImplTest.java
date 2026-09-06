package com.snippet.social.service;

import com.snippet.common.exception.BusinessException;
import com.snippet.social.dto.FriendRequestResponse;
import com.snippet.social.dto.SendFriendRequest;
import com.snippet.social.entity.FriendRelation;
import com.snippet.social.entity.FriendRequest;
import com.snippet.social.entity.RelationUser;
import com.snippet.social.mapper.FriendRelationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendRelationServiceImplTest {

    @Mock
    private FriendRelationMapper friendRelationMapper;

    @Test
    void incomingRequestsUseReceiverFromJwtAndPagination() {
        when(friendRelationMapper.selectPendingRequestsByReceiverId(42L, 10, 20))
                .thenReturn(List.of(request(501L, "PENDING")));

        List<FriendRequestResponse> result = service()
                .getIncomingRequests(42L, 10, 20);

        assertEquals(1, result.size());
        assertEquals(501L, result.get(0).getRequestId());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals(43L, result.get(0).getRequester().getId());
        verify(friendRelationMapper).selectPendingRequestsByReceiverId(42L, 10, 20);
    }

    @Test
    void sendingRequestDoesNotAcceptRequesterIdFromRequestBody() {
        SendFriendRequest request = new SendFriendRequest();
        request.setTargetUserId(43L);

        when(friendRelationMapper.selectActiveUserById(43L))
                .thenReturn(new RelationUser(43L, "snippetuser2", "用户二", null));
        when(friendRelationMapper.selectRelation(42L, 43L))
                .thenReturn(null, new FriendRelation(501L, 42L, 43L, "PENDING", null));
        when(friendRelationMapper.selectRelation(43L, 42L)).thenReturn(null);
        when(friendRelationMapper.insertPendingRequest(42L, 43L)).thenReturn(1);
        when(friendRelationMapper.selectRequestById(501L)).thenReturn(request(501L, "PENDING"));

        FriendRequestResponse result = service().sendFriendRequest(42L, request);

        assertEquals(501L, result.getRequestId());
        assertEquals(43L, result.getRequester().getId());
        verify(friendRelationMapper).insertPendingRequest(42L, 43L);
    }

    @Test
    void acceptingRequestUpdatesBothDirectionsInOneBusinessOperation() {
        when(friendRelationMapper.selectPendingRequestByIdAndReceiver(501L, 42L))
                .thenReturn(request(501L, "PENDING"));
        when(friendRelationMapper.updatePendingStatus(501L, 42L, "PENDING", "FRIEND"))
                .thenReturn(1);
        when(friendRelationMapper.upsertAcceptedRelation(42L, 43L)).thenReturn(1);
        when(friendRelationMapper.selectRequestById(501L)).thenReturn(request(501L, "FRIEND"));

        FriendRequestResponse result = service().acceptFriendRequest(42L, 501L);

        assertEquals("FRIEND", result.getStatus());
        verify(friendRelationMapper)
                .updatePendingStatus(501L, 42L, "PENDING", "FRIEND");
        verify(friendRelationMapper).upsertAcceptedRelation(42L, 43L);
    }

    @Test
    void rejectingRequestOnlyChangesPendingRequestOwnedByCurrentReceiver() {
        when(friendRelationMapper.selectPendingRequestByIdAndReceiver(501L, 42L))
                .thenReturn(request(501L, "PENDING"));
        when(friendRelationMapper.updatePendingStatus(501L, 42L, "PENDING", "REJECTED"))
                .thenReturn(1);
        when(friendRelationMapper.selectRequestById(501L)).thenReturn(request(501L, "REJECTED"));

        FriendRequestResponse result = service().rejectFriendRequest(42L, 501L);

        assertEquals("REJECTED", result.getStatus());
        verify(friendRelationMapper)
                .updatePendingStatus(501L, 42L, "PENDING", "REJECTED");
        verify(friendRelationMapper, never()).upsertAcceptedRelation(42L, 43L);
    }

    @Test
    void invalidUserDoesNotQueryDatabase() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().getIncomingRequests(null, 20, 0)
        );

        assertEquals(401, exception.getCode());
        verify(friendRelationMapper, never()).selectPendingRequestsByReceiverId(null, 20, 0);
    }

    private FriendRequest request(Long requestId, String status) {
        return new FriendRequest(
                requestId,
                status,
                43L,
                "snippetuser2",
                "用户二",
                11L,
                LocalDateTime.of(2026, 9, 6, 15, 30)
        );
    }

    private FriendRelationServiceImpl service() {
        return new FriendRelationServiceImpl(friendRelationMapper);
    }
}
