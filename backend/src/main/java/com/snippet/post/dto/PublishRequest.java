package com.snippet.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "发布文章参数")
public class PublishRequest {

    @NotNull
    @Min(0)
    @Schema(description = "准备发布的草稿版本", example = "3")
    private Integer expectedVersion;
}
