package com.snippet.asset.controller;

import com.snippet.asset.dto.AssetUploadResponse;
import com.snippet.asset.service.AssetService;
import com.snippet.common.api.CommonResult;
import com.snippet.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/assets")
@Tag(name = "资源接口", description = "图片资源上传相关接口")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            operationId = "uploadAsset",
            summary = "上传图片",
            description = "上传编辑器使用的图片资源，后端负责校验文件类型、大小和资源归属"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求已处理"),
            @ApiResponse(responseCode = "400", description = "文件不合法"),
            @ApiResponse(responseCode = "401", description = "未登录或登录已过期")
    })
    public CommonResult<AssetUploadResponse> upload(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "待上传的图片文件", required = true)
            @RequestPart("file") MultipartFile file) {
        return CommonResult.success(assetService.upload(currentUserId(jwt), file));
    }

    private Long currentUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或访问凭证无效");
        }

        try {
            long userId = Long.parseLong(jwt.getSubject());
            if (userId <= 0) {
                throw new NumberFormatException("用户 ID 必须为正数");
            }
            return userId;
        } catch (NumberFormatException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "访问凭证中的用户身份无效");
        }
    }
}
