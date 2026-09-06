package com.snippet.social.mapper;

import com.snippet.social.entity.FriendRelation;
import com.snippet.social.entity.FriendRequest;
import com.snippet.social.entity.RelationUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendRelationMapper {

    RelationUser selectActiveUserById(@Param("userId") Long userId);

    FriendRelation selectRelation(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId
    );

    List<FriendRequest> selectPendingRequestsByReceiverId(
            @Param("receiverId") Long receiverId,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    FriendRequest selectPendingRequestByIdAndReceiver(
            @Param("requestId") Long requestId,
            @Param("receiverId") Long receiverId
    );

    FriendRequest selectRequestById(@Param("requestId") Long requestId);

    int insertPendingRequest(
            @Param("requesterId") Long requesterId,
            @Param("receiverId") Long receiverId
    );

    int updateRejectedToPending(
            @Param("relationId") Long relationId,
            @Param("requesterId") Long requesterId,
            @Param("receiverId") Long receiverId
    );

    int updatePendingStatus(
            @Param("requestId") Long requestId,
            @Param("receiverId") Long receiverId,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus
    );

    int upsertAcceptedRelation(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId
    );
}
