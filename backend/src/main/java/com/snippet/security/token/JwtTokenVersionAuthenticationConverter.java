package com.snippet.security.token;

import com.snippet.auth.entity.UserAccount;
import com.snippet.auth.mapper.UserAccountMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

/**
 * 在标准 JWT 签名、过期时间和 issuer 校验之后，再校验数据库中的令牌版本。
 */
@Component
public class JwtTokenVersionAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    public static final String TOKEN_VERSION_CLAIM = "token_version";

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String INVALID_TOKEN_MESSAGE = "访问凭证中的用户身份无效";

    private final UserAccountMapper userAccountMapper;
    private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();

    public JwtTokenVersionAuthenticationConverter(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long userId = parsePositiveLong(jwt.getSubject());
        Long tokenVersion = parseTokenVersion(jwt.getClaim(TOKEN_VERSION_CLAIM));
        if (userId == null || tokenVersion == null) {
            throw invalidToken();
        }

        UserAccount currentState;
        try {
            currentState = userAccountMapper.selectTokenStateById(userId);
        } catch (DataAccessException exception) {
            // 数据库校验失败时必须拒绝请求，不能把异常当成“校验通过”。
            throw invalidToken(exception);
        }

        if (currentState == null
                || !ACTIVE_STATUS.equals(currentState.getStatus())
                || currentState.getTokenVersion() != tokenVersion) {
            throw invalidToken();
        }

        AbstractAuthenticationToken authentication = delegate.convert(jwt);
        if (authentication == null) {
            throw invalidToken();
        }
        return authentication;
    }

    private Long parsePositiveLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long parseTokenVersion(Object value) {
        if (!(value instanceof Number number)) {
            return null;
        }
        long parsed = number.longValue();
        return parsed >= 0 ? parsed : null;
    }

    private BadCredentialsException invalidToken() {
        return new BadCredentialsException(INVALID_TOKEN_MESSAGE);
    }

    private BadCredentialsException invalidToken(Exception cause) {
        return new BadCredentialsException(INVALID_TOKEN_MESSAGE, cause);
    }
}
