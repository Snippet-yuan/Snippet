package com.snippet.security.token;

import java.time.Instant;

public record IssuedToken(
        String value,
        Instant expiresAt,
        long expiresInSeconds
) {
}
