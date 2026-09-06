package com.snippet.social.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * friend 表与申请人公开资料的查询投影。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest {

    private Long requestId;

    private String status;

    private Long requesterId;

    private String requesterUsername;

    private String requesterNickname;

    private Long requesterAvatarAssetId;

    private LocalDateTime createdAt;
}
