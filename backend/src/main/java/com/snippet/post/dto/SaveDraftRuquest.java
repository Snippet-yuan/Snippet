package com.snippet.post.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "SaveDraftRequest", description = "保存文章草稿参数")
public class SaveDraftRuquest {

    @NotNull
    @Schema(
            description = "编辑器结构化内容，只允许约定的节点和样式字段",
            example = "{\"type\":\"doc\",\"children\":[]}"
    )
    private JsonNode content;

    @NotNull
    @Min(1)
    @Schema(description = "内容结构版本", example = "1")
    private Integer schemaVersion;

    @NotNull
    @Min(0)
    @Schema(description = "客户端最近一次已知的草稿版本，用于避免覆盖较新的内容", example = "0")
    private Integer version;
}
