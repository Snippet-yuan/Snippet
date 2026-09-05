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
public class PostComment {

    private Long id;

    private Long postId;

    private Long authorId;

    private String authorName;

    private Long authorAvatarAssetId;

    private String content;

    private LocalDateTime createdAt;
}
