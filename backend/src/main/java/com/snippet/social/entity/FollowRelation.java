package com.snippet.social.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * follow 表中的单向关注关系。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowRelation {

    private Long id;

    private Long followerId;

    private Long followingId;

    private LocalDateTime createdAt;
}
