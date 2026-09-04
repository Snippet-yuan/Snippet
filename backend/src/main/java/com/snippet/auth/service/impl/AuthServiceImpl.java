package com.snippet.auth.service.impl;

import com.snippet.auth.dto.ChangePasswordRequest;
import com.snippet.auth.dto.LoginRequest;
import com.snippet.auth.dto.LoginResponse;
import com.snippet.auth.dto.RegisterRequest;
import com.snippet.auth.entity.UserAccount;
import com.snippet.auth.mapper.UserAccountMapper;
import com.snippet.auth.service.AuthService;
import com.snippet.auth.vo.UserInfoVO;
import com.snippet.common.exception.BusinessException;
import com.snippet.security.token.IssuedToken;
import com.snippet.security.token.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int PASSWORD_MIN_LENGTH = 10;
    private static final int PASSWORD_MAX_LENGTH = 20;
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthServiceImpl(UserAccountMapper userAccountMapper,
                           PasswordEncoder passwordEncoder,
                           TokenService tokenService) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public UserInfoVO register(RegisterRequest request) {
        String username = request.getUsername();

        UserAccount oldUser = userAccountMapper.selectByUsername(username);
        if (oldUser != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户已存在");
        }

        String password = passwordEncoder.encode(request.getPassword());

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(password);
        user.setStatus(ACTIVE_STATUS);

        userAccountMapper.insert(user);
        return new UserInfoVO(user.getId(), user.getUsername());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserAccount user = userAccountMapper.selectByUsername(request.getUsername());
        if (user == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        if (!ACTIVE_STATUS.equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "账号不可用");
        }

        IssuedToken issuedToken = tokenService.issueAccessToken(
                user.getId(),
                user.getUsername(),
                user.getTokenVersion()
        );

        return new LoginResponse(
                issuedToken.value(),
                "Bearer",
                issuedToken.expiresInSeconds(),
                new UserInfoVO(user.getId(), user.getUsername())
        );
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        validateUserId(userId);
        validatePasswordRequest(request);

        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        if (!ACTIVE_STATUS.equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "用户账号不可用");
        }
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前密码错误");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "新密码不能与当前密码相同");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "两次新密码不一致");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        int updatedRows = userAccountMapper.updatePassword(userId, newPasswordHash);
        if (updatedRows != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "密码更新失败"
            );
        }
    }

    private void validatePasswordRequest(ChangePasswordRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCurrentPassword())
                || !StringUtils.hasText(request.getNewPassword())
                || !StringUtils.hasText(request.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码参数不能为空");
        }
        int newPasswordLength = request.getNewPassword().length();
        if (newPasswordLength < PASSWORD_MIN_LENGTH
                || newPasswordLength > PASSWORD_MAX_LENGTH) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "新密码长度必须在10到20个字符之间"
            );
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "访问凭证中的用户身份无效"
            );
        }
    }
}
