package com.snippet.asset.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 资源表 asset 的持久化实体。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    private Long id;

    private Long ownerId;

    private String objectKey;

    private String originalName;

    private String mimeType;

    private Long fileSize;

    private String sha256;

    private String status;

    private LocalDateTime createdAt;
}
