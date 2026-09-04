package com.snippet.security.token;

import com.snippet.auth.entity.UserAccount;
import com.snippet.auth.mapper.UserAccountMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtTokenVersionAuthenticationConverterTest {

    private final UserAccountMapper userAccountMapper = mock(UserAccountMapper.class);
    private final JwtTokenVersionAuthenticationConverter converter =
            new JwtTokenVersionAuthenticationConverter(userAccountMapper);

    @Test
    void acceptsActiveUserWithCurrentTokenVersion() {
        when(userAccountMapper.selectTokenStateById(42L))
                .thenReturn(tokenState("ACTIVE", 3L));

        AbstractAuthenticationToken authentication = converter.convert(jwt(42L, 3L));

        assertEquals("42", authentication.getName());
        verify(userAccountMapper).selectTokenStateById(42L);
    }

    @Test
    void rejectsStaleTokenVersion() {
        when(userAccountMapper.selectTokenStateById(42L))
                .thenReturn(tokenState("ACTIVE", 4L));

        assertThrows(
                BadCredentialsException.class,
                () -> converter.convert(jwt(42L, 3L))
        );
    }

    @Test
    void rejectsInactiveUserEvenWhenTokenVersionMatches() {
        when(userAccountMapper.selectTokenStateById(42L))
                .thenReturn(tokenState("DISABLED", 3L));

        assertThrows(
                BadCredentialsException.class,
                () -> converter.convert(jwt(42L, 3L))
        );
    }

    private UserAccount tokenState(String status, long tokenVersion) {
        UserAccount user = new UserAccount();
        user.setStatus(status);
        user.setTokenVersion(tokenVersion);
        return user;
    }

    private Jwt jwt(long userId, long tokenVersion) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(Long.toString(userId))
                .claim("username", "test-user")
                .claim(JwtTokenVersionAuthenticationConverter.TOKEN_VERSION_CLAIM, tokenVersion)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
