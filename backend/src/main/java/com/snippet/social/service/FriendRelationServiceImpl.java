package com.snippet.social.service;

import com.snippet.common.exception.BusinessException;
import com.snippet.social.dto.FriendRequestResponse;
import com.snippet.social.dto.SendFriendRequest;
import com.snippet.social.dto.UserSummaryResponse;
import com.snippet.social.entity.FriendRelation;
import com.snippet.social.entity.FriendRequest;
import com.snippet.social.entity.RelationUser;
import com.snippet.social.mapper.FriendRelationMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户关系业务。
 *
 * <p>当前数据库只有 V2 的 friend 表，因此 accepted friend 关系作为当前
 * following 查询的兼容数据源；独立的单向 follow 关系需要后续独立表支持。</p>
 */
@Service
public class FriendRelationServiceImpl implements FriendRelationService {

    private static final String PENDING_STATUS = "PENDING";
    private static final String FRIEND_STATUS = "FRIEND";
    private static final String REJECTED_STATUS = "REJECTED";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_OFFSET = 10000;

    private final FriendRelationMapper friendRelationMapper;

    public FriendRelationServiceImpl(FriendRelationMapper friendRelationMapper) {
        this.friendRelationMapper = friendRelationMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getIncomingRequests(
            Long receiverId,
            Integer limit,
            Integer offset) {
        validateUserId(receiverId);
        int normalizedLimit = normalizeLimit(limit);
        int normalizedOffset = normalizeOffset(offset);

        try {
            return friendRelationMapper.selectPendingRequestsByReceiverId(
                            receiverId,
                            normalizedLimit,
                            normalizedOffset
                    )
                    .stream()
                    .map(this::toRequestResponse)
                    .toList();
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "好友申请列表读取失败"
            );
        }
    }

    @Override
    @Transactional
    public FriendRequestResponse sendFriendRequest(
            Long requesterId,
            SendFriendRequest request) {
        validateUserId(requesterId);
        if (request == null || request.getTargetUserId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "目标用户不能为空");
        }

        Long receiverId = request.getTargetUserId();
        validateUserId(receiverId);
        if (requesterId.equals(receiverId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能向自己发送好友申请");
        }

        try {
            RelationUser receiver = friendRelationMapper.selectActiveUserById(receiverId);
            if (receiver == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "目标用户不存在");
            }

            FriendRelation existing = friendRelationMapper.selectRelation(
                    requesterId,
                    receiverId
            );
            FriendRelation reverse = friendRelationMapper.selectRelation(
                    receiverId,
                    requesterId
            );
            validateExistingRelations(existing, reverse);

            int affectedRows;
            if (existing == null) {
                affectedRows = friendRelationMapper.insertPendingRequest(
                        requesterId,
                        receiverId
                );
            } else {
                affectedRows = friendRelationMapper.updateRejectedToPending(
                        existing.getId(),
                        requesterId,
                        receiverId
                );
            }

            if (affectedRows != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, "好友申请状态已发生变化，请重试");
            }

            FriendRequest saved = friendRelationMapper.selectRequestById(
                    findRequestId(requesterId, receiverId)
            );
            if (saved == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "好友申请创建结果读取失败"
                );
            }
            return toRequestResponse(saved);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "好友申请已存在");
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "好友申请创建失败"
            );
        }
    }

    @Override
    @Transactional
    public FriendRequestResponse acceptFriendRequest(Long receiverId, Long requestId) {
        validateUserId(receiverId);
        validateRequestId(requestId);

        try {
            FriendRequest request = friendRelationMapper.selectPendingRequestByIdAndReceiver(
                    requestId,
                    receiverId
            );
            if (request == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "好友申请不存在或已处理");
            }

            int updatedRows = friendRelationMapper.updatePendingStatus(
                    requestId,
                    receiverId,
                    PENDING_STATUS,
                    FRIEND_STATUS
            );
            if (updatedRows != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, "好友申请状态已发生变化，请重试");
            }

            int reciprocalRows = friendRelationMapper.upsertAcceptedRelation(
                    receiverId,
                    request.getRequesterId()
            );
            if (reciprocalRows < 1) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "好友关系建立失败");
            }

            FriendRequest accepted = friendRelationMapper.selectRequestById(requestId);
            if (accepted == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "好友申请处理结果读取失败"
                );
            }
            return toRequestResponse(accepted);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "好友申请接受失败"
            );
        }
    }

    @Override
    @Transactional
    public FriendRequestResponse rejectFriendRequest(Long receiverId, Long requestId) {
        validateUserId(receiverId);
        validateRequestId(requestId);

        try {
            FriendRequest request = friendRelationMapper.selectPendingRequestByIdAndReceiver(
                    requestId,
                    receiverId
            );
            if (request == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "好友申请不存在或已处理");
            }

            int updatedRows = friendRelationMapper.updatePendingStatus(
                    requestId,
                    receiverId,
                    PENDING_STATUS,
                    REJECTED_STATUS
            );
            if (updatedRows != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, "好友申请状态已发生变化，请重试");
            }

            FriendRequest rejected = friendRelationMapper.selectRequestById(requestId);
            if (rejected == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "好友申请处理结果读取失败"
                );
            }
            return toRequestResponse(rejected);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "好友申请拒绝失败"
            );
        }
    }

    private void validateExistingRelations(
            FriendRelation existing,
            FriendRelation reverse) {
        if (existing != null) {
            if (PENDING_STATUS.equals(existing.getStatus())) {
                throw new BusinessException(HttpStatus.CONFLICT, "好友申请已发送");
            }
            if (FRIEND_STATUS.equals(existing.getStatus())) {
                throw new BusinessException(HttpStatus.CONFLICT, "你们已经是好友");
            }
            if (!REJECTED_STATUS.equals(existing.getStatus())) {
                throw new BusinessException(HttpStatus.CONFLICT, "关系状态不可处理");
            }
        }

        if (reverse != null) {
            if (PENDING_STATUS.equals(reverse.getStatus())) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "对方已向你发送好友申请，请先处理该申请"
                );
            }
            if (FRIEND_STATUS.equals(reverse.getStatus())) {
                throw new BusinessException(HttpStatus.CONFLICT, "你们已经是好友");
            }
            if (!REJECTED_STATUS.equals(reverse.getStatus())) {
                throw new BusinessException(HttpStatus.CONFLICT, "关系状态不可处理");
            }
        }
    }

    /**
     * friend 表使用唯一 user_id/friend_id，所以写入后可通过这组键重新取回申请记录。
     */
    private Long findRequestId(Long requesterId, Long receiverId) {
        FriendRelation relation = friendRelationMapper.selectRelation(requesterId, receiverId);
        if (relation == null || relation.getId() == null) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "好友申请记录读取失败"
            );
        }
        return relation.getId();
    }

    private FriendRequestResponse toRequestResponse(FriendRequest request) {
        return new FriendRequestResponse(
                request.getRequestId(),
                request.getStatus(),
                new UserSummaryResponse(
                        request.getRequesterId(),
                        request.getRequesterUsername(),
                        request.getRequesterNickname(),
                        request.getRequesterAvatarAssetId()
                ),
                request.getCreatedAt()
        );
    }

    private int normalizeLimit(Integer limit) {
        int value = limit == null ? DEFAULT_PAGE_SIZE : limit;
        if (value <= 0 || value > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "limit 必须在1到100之间");
        }
        return value;
    }

    private int normalizeOffset(Integer offset) {
        int value = offset == null ? 0 : offset;
        if (value < 0 || value > MAX_OFFSET) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "offset 必须在0到10000之间");
        }
        return value;
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "访问凭证中的用户身份无效");
        }
    }

    private void validateRequestId(Long requestId) {
        if (requestId == null || requestId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "申请记录 ID 无效");
        }
    }
}
