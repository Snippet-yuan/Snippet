package com.snippet.post.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
@Schema(description = "修改文章基本信息参数")
public class UpdatePostRequest {

    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Schema(description = "文章标题，不传则保持不变", example = "修改后的文章标题")
    private String title;

    @Size(max = 5000, message = "文章描述长度不能超过5000个字符")
    @Schema(description = "文章描述，不传则保持不变；传空白字符可清空", example = "这是文章简介")
    private String description;

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("不支持的请求字段: " + fieldName);
    }
}
