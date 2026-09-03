package com.snippet.asset.service;

import com.snippet.asset.dto.AssetUploadResponse;
import com.snippet.asset.entity.Asset;
import com.snippet.asset.mapper.AssetMapper;
import com.snippet.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AssetServiceImpl implements AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetServiceImpl.class);

    private static final String READY_STATUS = "READY";
    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif"
    );
    private static final Map<String, String> FORMAT_TO_MIME_TYPE = Map.of(
            "png", "image/png",
            "jpeg", "image/jpeg",
            "jpg", "image/jpeg",
            "gif", "image/gif"
    );
    private static final Map<String, String> MIME_TYPE_TO_EXTENSION = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/gif", ".gif"
    );

    private final AssetMapper assetMapper;
    private final Path storageRoot;
    private final String publicBaseUrl;
    private final long maxFileSizeBytes;

    public AssetServiceImpl(
            AssetMapper assetMapper,
            @Value("${snippet.asset.storage-path:./data/assets}") String storagePath,
            @Value("${snippet.asset.public-base-url:/api/v1/assets/files}") String publicBaseUrl,
            @Value("${snippet.asset.max-file-size:10MB}") DataSize maxFileSize) {
        this.assetMapper = assetMapper;
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
        String normalizedBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        while (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        this.publicBaseUrl = normalizedBaseUrl;
        this.maxFileSizeBytes = maxFileSize.toBytes();
        if (this.maxFileSizeBytes <= 0) {
            throw new IllegalArgumentException("资源文件大小限制必须大于 0");
        }
    }

    @Override
    @Transactional
    public AssetUploadResponse upload(Long ownerId, MultipartFile file) {
        validateOwnerId(ownerId);
        ValidatedFile validatedFile = validateRequest(file);

        Path temporaryFile = null;
        Path targetFile = null;
        try {
            Files.createDirectories(storageRoot);

            String objectKey = UUID.randomUUID() + MIME_TYPE_TO_EXTENSION.get(validatedFile.mimeType());
            targetFile = resolveStoragePath(objectKey);
            temporaryFile = Files.createTempFile(storageRoot, ".asset-", ".tmp");

            StoredFile storedFile = copyAndHash(file, temporaryFile);
            validateImageContent(temporaryFile, validatedFile.mimeType());
            moveIntoStorage(temporaryFile, targetFile);
            temporaryFile = null;

            Asset asset = new Asset();
            asset.setOwnerId(ownerId);
            asset.setObjectKey(objectKey);
            asset.setOriginalName(validatedFile.originalName());
            asset.setMimeType(validatedFile.mimeType());
            asset.setFileSize(storedFile.fileSize());
            asset.setSha256(storedFile.sha256());
            asset.setStatus(READY_STATUS);

            int insertedRows = assetMapper.insert(asset);
            if (insertedRows != 1 || asset.getId() == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "资源登记失败"
                );
            }

            return new AssetUploadResponse(
                    asset.getId(),
                    buildPublicUrl(objectKey),
                    asset.getOriginalName(),
                    asset.getMimeType(),
                    asset.getFileSize(),
                    asset.getSha256()
            );
        } catch (BusinessException exception) {
            cleanup(temporaryFile, targetFile);
            throw exception;
        } catch (DataAccessException exception) {
            cleanup(temporaryFile, targetFile);
            log.error("资源登记失败，ownerId={}", ownerId, exception);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "资源登记失败"
            );
        } catch (IOException | RuntimeException exception) {
            cleanup(temporaryFile, targetFile);
            log.error("资源存储失败，ownerId={}", ownerId, exception);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "资源存储失败"
            );
        }
    }

    private ValidatedFile validateRequest(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "文件不能为空");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "文件大小超过限制");
        }

        String mimeType = file.getContentType();
        if (!SUPPORTED_MIME_TYPES.contains(mimeType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅支持 PNG、JPEG、GIF 图片");
        }

        String originalName = safeOriginalName(file.getOriginalFilename());
        return new ValidatedFile(originalName, mimeType);
    }

    private void validateOwnerId(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "访问凭证中的用户身份无效");
        }
    }

    private StoredFile copyAndHash(MultipartFile file, Path temporaryFile) throws IOException {
        MessageDigest digest = sha256Digest();
        long fileSize = 0;

        try (InputStream input = file.getInputStream();
             OutputStream output = Files.newOutputStream(
                     temporaryFile,
                     StandardOpenOption.WRITE,
                     StandardOpenOption.TRUNCATE_EXISTING
             )) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                fileSize += bytesRead;
                if (fileSize > maxFileSizeBytes) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "文件大小超过限制");
                }
                digest.update(buffer, 0, bytesRead);
                output.write(buffer, 0, bytesRead);
            }
        }

        return new StoredFile(fileSize, HexFormat.of().formatHex(digest.digest()));
    }

    private void validateImageContent(Path temporaryFile, String declaredMimeType)
            throws IOException {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(temporaryFile.toFile())) {
            if (imageInput == null) {
                throw invalidImage();
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw invalidImage();
            }

            ImageReader reader = readers.next();
            try {
                String actualMimeType = FORMAT_TO_MIME_TYPE.get(
                        reader.getFormatName().toLowerCase(Locale.ROOT)
                );
                if (!declaredMimeType.equals(actualMimeType)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "文件类型与内容不匹配");
                }

                reader.setInput(imageInput, true, true);
                if (reader.getWidth(0) <= 0 || reader.getHeight(0) <= 0) {
                    throw invalidImage();
                }
            } finally {
                reader.dispose();
            }
        }
    }

    private Path resolveStoragePath(String objectKey) {
        Path resolvedPath = storageRoot.resolve(objectKey).normalize();
        if (!resolvedPath.startsWith(storageRoot)) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "资源存储路径无效");
        }
        return resolvedPath;
    }

    private void moveIntoStorage(Path temporaryFile, Path targetFile) throws IOException {
        try {
            Files.move(temporaryFile, targetFile, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, targetFile);
        }
    }

    private String buildPublicUrl(String objectKey) {
        return publicBaseUrl + "/" + objectKey;
    }

    private String safeOriginalName(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "unnamed";
        }

        String cleaned = StringUtils.cleanPath(originalFilename).replace('\\', '/');
        int lastSlash = cleaned.lastIndexOf('/');
        String basename = lastSlash >= 0 ? cleaned.substring(lastSlash + 1) : cleaned;
        if (!StringUtils.hasText(basename) || ".".equals(basename) || "..".equals(basename)) {
            return "unnamed";
        }
        return basename.length() > 255 ? basename.substring(0, 255) : basename;
    }

    private BusinessException invalidImage() {
        return new BusinessException(HttpStatus.BAD_REQUEST, "文件内容不是有效图片");
    }

    private MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }

    private void cleanup(Path temporaryFile, Path targetFile) {
        deleteIfExists(temporaryFile);
        deleteIfExists(targetFile);
    }

    private void deleteIfExists(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.error("清理资源文件失败，path={}", path, exception);
        }
    }

    private record ValidatedFile(String originalName, String mimeType) {
    }

    private record StoredFile(long fileSize, String sha256) {
    }
}
