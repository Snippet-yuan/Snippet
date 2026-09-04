package com.snippet.auth.entity;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private Long id;

    private String username;

    private String passwordHash;

    private Long avatarAssetId;

    private String status;

    private long tokenVersion;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
