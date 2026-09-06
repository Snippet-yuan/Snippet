package com.snippet.social.service;

import com.snippet.social.dto.FriendRequestResponse;
import com.snippet.social.dto.SendFriendRequest;
import java.util.List;

public interface FriendRelationService {

    List<FriendRequestResponse> getIncomingRequests(
            Long receiverId,
            Integer limit,
            Integer offset
    );

    FriendRequestResponse sendFriendRequest(
            Long requesterId,
            SendFriendRequest request
    );

    FriendRequestResponse acceptFriendRequest(Long receiverId, Long requestId);

    FriendRequestResponse rejectFriendRequest(Long receiverId, Long requestId);
}
