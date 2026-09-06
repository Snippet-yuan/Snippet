package com.snippet.social.service;

import com.snippet.common.exception.BusinessException;
import com.snippet.social.dto.FollowStatusResponse;
import com.snippet.social.dto.UserSummaryResponse;
import com.snippet.social.entity.RelationUser;
import com.snippet.social.mapper.FollowMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 单向关注业务。
 */
@Service
public class FollowServiceImpl implements FollowService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_OFFSET = 10000;

    private final FollowMapper followMapper;

    public FollowServiceImpl(FollowMapper followMapper) {
        this.followMapper = followMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getFollowing(
            Long followerId,
            Integer limit,
            Integer offset) {
        validateUserId(followerId);
        int normalizedLimit = normalizeLimit(limit);
        int normalizedOffset = normalizeOffset(offset);

        try {
            return followMapper.selectFollowingUsers(
                            followerId,
                            normalizedLimit,
                            normalizedOffset
                    )
                    .stream()
                    .map(this::toUserSummary)
                    .toList();
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "关注列表读取失败"
            );
        }
    }

    @Override
    @Transactional
    public FollowStatusResponse follow(Long followerId, Long followingId) {
        validateUserId(followerId);
        validateUserId(followingId);
        if (followerId.equals(followingId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能关注自己");
        }

        try {
            ensureActiveTarget(followingId);
            if (followMapper.existsFollow(followerId, followingId)) {
                return new FollowStatusResponse(true);
            }

            followMapper.insertFollow(followerId, followingId);
            return new FollowStatusResponse(true);
        } catch (DuplicateKeyException exception) {
            // 并发重复关注时，数据库唯一键保证最终只有一条关系。
            return new FollowStatusResponse(true);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "关注操作失败"
            );
        }
    }

    @Override
    @Transactional
    public FollowStatusResponse unfollow(Long followerId, Long followingId) {
        validateUserId(followerId);
        validateUserId(followingId);
        if (followerId.equals(followingId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能取消对自己的关注");
        }

        try {
            ensureActiveTarget(followingId);
            followMapper.deleteFollow(followerId, followingId);
            return new FollowStatusResponse(false);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "取消关注操作失败"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FollowStatusResponse getFollowingStatus(Long followerId, Long followingId) {
        validateUserId(followerId);
        validateUserId(followingId);
        if (followerId.equals(followingId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能查询自己对自己的关注状态");
        }

        try {
            ensureActiveTarget(followingId);
            return new FollowStatusResponse(
                    followMapper.existsFollow(followerId, followingId)
            );
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "关注状态读取失败"
            );
        }
    }

    private void ensureActiveTarget(Long followingId) {
        if (followMapper.selectActiveUserById(followingId) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "目标用户不存在");
        }
    }

    private UserSummaryResponse toUserSummary(RelationUser user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarAssetId()
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
}
