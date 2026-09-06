package com.snippet.social.service;

import com.snippet.social.dto.FollowStatusResponse;
import com.snippet.social.dto.UserSummaryResponse;

import java.util.List;

public interface FollowService {

    List<UserSummaryResponse> getFollowing(Long followerId, Integer limit, Integer offset);

    FollowStatusResponse follow(Long followerId, Long followingId);

    FollowStatusResponse unfollow(Long followerId, Long followingId);

    FollowStatusResponse getFollowingStatus(Long followerId, Long followingId);
}
