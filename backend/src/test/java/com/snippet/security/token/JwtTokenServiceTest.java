package com.snippet.security.token;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenServiceTest {

    @Test
    void issuedTokenCanBeDecodedAndTamperingIsRejected() {
        String issuer = "snippet-backend";
        String encodedSecret = Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef"
                        .getBytes(StandardCharsets.UTF_8)
        );

        JwtTokenConfig config = new JwtTokenConfig();
        SecretKey secretKey = config.jwtSecretKey(encodedSecret);
        JwtEncoder encoder = config.jwtEncoder(secretKey);
        JwtDecoder decoder = config.jwtDecoder(secretKey, issuer);
        JwtTokenService tokenService = new JwtTokenService(
                encoder,
                issuer,
                Duration.ofMinutes(30)
        );

        IssuedToken issuedToken = tokenService.issueAccessToken(42L, "snippet-user");
        Jwt jwt = decoder.decode(issuedToken.value());

        assertEquals("42", jwt.getSubject());
        assertEquals("snippet-user", jwt.getClaimAsString("username"));
        assertEquals(issuer, jwt.getClaimAsString("iss"));
        assertEquals(1800L, issuedToken.expiresInSeconds());

        String tamperedToken = tamperPayload(issuedToken.value());
        assertThrows(JwtException.class, () -> decoder.decode(tamperedToken));
    }

    private String tamperPayload(String token) {
        int payloadStart = token.indexOf('.') + 1;
        char current = token.charAt(payloadStart);
        char replacement = current == 'a' ? 'b' : 'a';
        return token.substring(0, payloadStart)
                + replacement
                + token.substring(payloadStart + 1);
    }
}
