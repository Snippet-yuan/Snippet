package com.snippet.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "创建文章参数")
public class CreatePostRequest {

    @Size(max = 200)
    @Schema(description = "文章标题，可为空", example = "我的第一篇 Snippet")
    private String title;
}
