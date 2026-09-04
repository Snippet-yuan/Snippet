package com.snippet.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    private Long id;

    private String username;

    private String email;

    private String nickname;

    private Long avatarAssetId;

    private Long backgroundAssetId;

    private String status;
}
