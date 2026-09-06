package com.snippet.post.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PostFavorite {

    private Long id;

    private Long postId;

    private Long userId;

    private LocalDateTime createdAt;

    /** 查询收藏列表时由 post 表投影出的公开字段。 */
    private String title;

    private String description;

    private String slug;

    private LocalDateTime publishedAt;
}
