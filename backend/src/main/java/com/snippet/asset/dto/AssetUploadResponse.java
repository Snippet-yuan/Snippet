package com.snippet.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "资源上传结果")
public class   AssetUploadResponse {

    @Schema(description = "资源id", example = "10")
    private Long id;

    @Schema(description = "资源访问地址", example = "https://cdn.example.com/assets/abc.png")
    private String url;

    @Schema(description = "原始文件名", example = "cover.png")
    private String originalName;

    @Schema(description = "媒体类型", example = "image/png")
    private String mimeType;

    @Schema(description = "文件大小，单位为字节", example = "204800")
    private Long fileSize;

    @Schema(description = "文件 SHA-256 摘要", example = "0123456789abcdef...")
    private String sha256;
}
