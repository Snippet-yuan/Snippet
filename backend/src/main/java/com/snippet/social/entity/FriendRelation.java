package com.snippet.social.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * friend 表中的一条有方向关系。
 * userId 是关系发起方，friendId 是关系目标方。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRelation {

    private Long id;

    private Long userId;

    private Long friendId;

    private String status;

    private LocalDateTime createdAt;
}
