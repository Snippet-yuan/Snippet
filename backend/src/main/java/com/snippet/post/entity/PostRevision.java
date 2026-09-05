package com.snippet.post.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRevision {

    private Long id;

    private Long postId;

    private Integer revisionNo;

    private String contentJson;

    private Integer schemaVersion;

    private Long createdBy;

    private LocalDateTime createdAt;
}
