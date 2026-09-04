package com.snippet.user.service;

import com.snippet.asset.mapper.AssetMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.user.dto.UpdateProfileRequest;
import com.snippet.user.dto.UserProfileResponse;
import com.snippet.user.entity.UserProfile;
import com.snippet.user.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 当前用户资料业务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final int USERNAME_MIN_LENGTH = 10;
    private static final int USERNAME_MAX_LENGTH = 20;
    private static final int EMAIL_MAX_LENGTH = 255;
    private static final int NICKNAME_MAX_LENGTH = 64;

    private final UserMapper userMapper;
    private final AssetMapper assetMapper;

    public UserServiceImpl(UserMapper userMapper, AssetMapper assetMapper) {
        this.userMapper = userMapper;
        this.assetMapper = assetMapper;
    }

    @Override
    public UserProfileResponse getCurrentUser(Long userId) {
        return toResponse(loadActiveProfile(userId));
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(
            Long userId,
            UpdateProfileRequest request) {
        UserProfile current = loadActiveProfile(userId);
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "资料参数不能为空");
        }

        String username = normalizeText(request.getUsername(), "用户名");
        String email = normalizeText(request.getEmail(), "邮箱");
        String nickname = normalizeText(request.getNickname(), "昵称");
        Long avatarAssetId = request.getAvatarAssetId();
        Long backgroundAssetId = request.getBackgroundAssetId();

        validateProfileFields(username, email, nickname, avatarAssetId, backgroundAssetId);
        if (username == null
                && email == null
                && nickname == null
                && avatarAssetId == null
                && backgroundAssetId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "至少修改一项资料");
        }

        if (username != null && !username.equals(current.getUsername())) {
            UserProfile existing = userMapper.selectByUsername(username);
            if (existing != null && !userId.equals(existing.getId())) {
                throw new BusinessException(HttpStatus.CONFLICT, "用户名已存在");
            }
        }
        if (email != null && !email.equals(current.getEmail())) {
            UserProfile existing = userMapper.selectByEmail(email);
            if (existing != null && !userId.equals(existing.getId())) {
                throw new BusinessException(HttpStatus.CONFLICT, "邮箱已存在");
            }
        }

        validateOwnedAsset(userId, avatarAssetId, "头像");
        validateOwnedAsset(userId, backgroundAssetId, "背景图");

        userMapper.updateProfile(
                userId,
                username,
                email,
                nickname,
                avatarAssetId,
                backgroundAssetId
        );
        return getCurrentUser(userId);
    }

    private UserProfile loadActiveProfile(Long userId) {
        validateUserId(userId);

        UserProfile profile = userMapper.selectById(userId);
        if (profile == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        if (!ACTIVE_STATUS.equals(profile.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "用户账号不可用");
        }
        return profile;
    }

    private void validateProfileFields(
            String username,
            String email,
            String nickname,
            Long avatarAssetId,
            Long backgroundAssetId) {
        if (username != null
                && (username.length() < USERNAME_MIN_LENGTH
                || username.length() > USERNAME_MAX_LENGTH)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名长度必须在10到20个字符之间");
        }
        if (email != null && email.length() > EMAIL_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "邮箱长度不能超过255个字符");
        }
        if (nickname != null && nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "昵称长度不能超过64个字符");
        }
        if (avatarAssetId != null && avatarAssetId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "头像资源 ID 无效");
        }
        if (backgroundAssetId != null && backgroundAssetId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "背景图资源 ID 无效");
        }
    }

    private void validateOwnedAsset(Long userId, Long assetId, String fieldName) {
        if (assetId == null) {
            return;
        }
        if (!assetMapper.existsOwnedReadyById(assetId, userId)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + "资源不存在、不属于当前用户或尚未准备完成"
            );
        }
    }

    private String normalizeText(String value, String fieldName) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + "不能为空");
        }
        return normalized;
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "访问凭证中的用户身份无效"
            );
        }
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getUsername(),
                profile.getEmail(),
                profile.getNickname(),
                profile.getAvatarAssetId(),
                profile.getBackgroundAssetId()
        );
    }
}
