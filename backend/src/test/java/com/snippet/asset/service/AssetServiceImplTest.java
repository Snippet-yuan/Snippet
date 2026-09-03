package com.snippet.asset.service;

import com.snippet.asset.entity.Asset;
import com.snippet.asset.mapper.AssetMapper;
import com.snippet.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {

    @Mock
    private AssetMapper assetMapper;

    @Test
    void uploadStoresImageAndRegistersJwtOwnerData(@TempDir Path tempDirectory) throws Exception {
        byte[] imageBytes = pngBytes();
        doAnswer(invocation -> {
            Asset asset = invocation.getArgument(0);
            asset.setId(17L);
            return 1;
        }).when(assetMapper).insert(any(Asset.class));

        AssetServiceImpl service = newService(tempDirectory, DataSize.ofMegabytes(1));
        MultipartFile file = new MockMultipartFile(
                "file",
                "..//cover.png",
                "image/png",
                imageBytes
        );

        var response = service.upload(42L, file);

        assertEquals(17L, response.getId());
        assertEquals("cover.png", response.getOriginalName());
        assertEquals("image/png", response.getMimeType());
        assertEquals((long) imageBytes.length, response.getFileSize());
        assertEquals(sha256(imageBytes), response.getSha256());
        assertTrue(response.getUrl().startsWith("/assets/"));

        String objectKey = response.getUrl().substring(response.getUrl().lastIndexOf('/') + 1);
        Path storedFile = tempDirectory.resolve(objectKey);
        assertTrue(Files.exists(storedFile));
        assertArrayEquals(imageBytes, Files.readAllBytes(storedFile));

        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetMapper).insert(assetCaptor.capture());
        Asset persistedAsset = assetCaptor.getValue();
        assertEquals(42L, persistedAsset.getOwnerId());
        assertEquals(objectKey, persistedAsset.getObjectKey());
        assertEquals("READY", persistedAsset.getStatus());
        assertEquals(response.getSha256(), persistedAsset.getSha256());
    }

    @Test
    void invalidTypeIsRejectedBeforeStorage(@TempDir Path tempDirectory) {
        AssetServiceImpl service = newService(tempDirectory, DataSize.ofMegabytes(1));
        MultipartFile file = new MockMultipartFile(
                "file",
                "cover.txt",
                "text/plain",
                "not an image".getBytes()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.upload(42L, file)
        );

        assertEquals(400, exception.getCode());
        assertEquals("仅支持 PNG、JPEG、GIF 图片", exception.getMessage());
        verifyNoInteractions(assetMapper);
        assertDirectoryIsEmpty(tempDirectory);
    }

    @Test
    void invalidImageContentIsRejectedEvenWhenMimeTypeLooksValid(@TempDir Path tempDirectory) {
        AssetServiceImpl service = newService(tempDirectory, DataSize.ofMegabytes(1));
        MultipartFile file = new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                "not an image".getBytes()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.upload(42L, file)
        );

        assertEquals(400, exception.getCode());
        assertEquals("文件内容不是有效图片", exception.getMessage());
        verifyNoInteractions(assetMapper);
        assertDirectoryIsEmpty(tempDirectory);
    }

    @Test
    void databaseFailureCleansStoredFile(@TempDir Path tempDirectory) throws Exception {
        doThrow(new RuntimeException("database unavailable"))
                .when(assetMapper).insert(any(Asset.class));

        AssetServiceImpl service = newService(tempDirectory, DataSize.ofMegabytes(1));

        assertThrows(
                BusinessException.class,
                () -> service.upload(
                        42L,
                        new MockMultipartFile(
                                "file",
                                "cover.png",
                                "image/png",
                                pngBytes()
                        )
                )
        );

        assertDirectoryIsEmpty(tempDirectory);
    }

    private AssetServiceImpl newService(Path tempDirectory, DataSize maxFileSize) {
        return new AssetServiceImpl(
                assetMapper,
                tempDirectory.toString(),
                "/assets/",
                maxFileSize
        );
    }

    private byte[] pngBytes() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private String sha256(byte[] content) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(content)
        );
    }

    private void assertDirectoryIsEmpty(Path directory) {
        try (var files = Files.list(directory)) {
            assertEquals(List.of(), files.toList());
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
