package com.snippet.user.service;

import com.snippet.user.dto.UpdateProfileRequest;
import com.snippet.user.dto.UserProfileResponse;

/**
 * 用户资料业务。
 */
public interface UserService {

    UserProfileResponse getCurrentUser(Long userId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
}
