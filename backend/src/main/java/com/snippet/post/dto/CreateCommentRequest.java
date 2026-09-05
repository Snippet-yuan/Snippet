package com.snippet.post.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
@Schema(description = "创建评论参数")
public class CreateCommentRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容长度不能超过2000个字符")
    @Schema(description = "评论纯文本内容", example = "这篇文章很有帮助")
    private String content;

    /**
     * 评论作者从 JWT 获取，不允许客户端通过请求体伪造 authorId 或 userId。
     */
    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("不支持的请求字段: " + fieldName);
    }
}
