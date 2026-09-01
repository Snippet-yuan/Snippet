package com.snippet.auth.service.impl;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

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
            throw new BusinessException("用户已存在");
        }

        String password = passwordEncoder.encode(request.getPassword());

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(password);
        user.setStatus("ACTIVE");

        userAccountMapper.insert(user);
        return new UserInfoVO(user.getId(), user.getUsername());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserAccount user = userAccountMapper.selectByUsername(request.getUsername());
        if (user == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("账号不可用");
        }

        IssuedToken issuedToken = tokenService.issueAccessToken(
                user.getId(),
                user.getUsername()
        );

        return new LoginResponse(
                issuedToken.value(),
                "Bearer",
                issuedToken.expiresInSeconds(),
                new UserInfoVO(user.getId(), user.getUsername())
        );
    }
}
