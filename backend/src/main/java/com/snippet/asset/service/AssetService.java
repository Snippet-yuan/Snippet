package com.snippet.asset.service;

import com.snippet.asset.dto.AssetUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资源业务：校验、存储和资源登记都在这里完成。
 */
public interface AssetService {

    AssetUploadResponse upload(Long ownerId, MultipartFile file);
}
