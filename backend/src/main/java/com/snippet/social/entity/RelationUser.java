package com.snippet.social.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 关系列表中允许公开展示的用户字段。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationUser {

    private Long id;

    private String username;

    private String nickname;

    private Long avatarAssetId;
}
