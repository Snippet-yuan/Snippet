package com.snippet.asset.controller;

import com.snippet.asset.dto.AssetUploadResponse;
import com.snippet.asset.service.AssetService;
import com.snippet.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AssetControllerTest {

    @Test
    void uploadPassesJwtSubjectAsOwnerId() {
        AssetService assetService = Mockito.mock(AssetService.class);
        AssetController controller = new AssetController(assetService);
        var file = new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                new byte[]{1}
        );
        var expected = new AssetUploadResponse(
                10L,
                "/assets/object.png",
                "cover.png",
                "image/png",
                1L,
                "sha256"
        );
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        when(assetService.upload(42L, file)).thenReturn(expected);

        var result = controller.upload(jwt, file);

        assertEquals(expected, result.getData());
        verify(assetService).upload(42L, file);
    }

    @Test
    void malformedJwtSubjectIsRejectedAndIsNotPassedToService() {
        AssetService assetService = Mockito.mock(AssetService.class);
        AssetController controller = new AssetController(assetService);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("not-a-user-id")
                .build();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.upload(jwt, new MockMultipartFile("file", new byte[]{1}))
        );

        assertEquals(HttpStatus.UNAUTHORIZED.value(), exception.getCode());
        verifyNoInteractions(assetService);
    }
}
