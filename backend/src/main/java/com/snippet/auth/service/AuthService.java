package com.snippet.auth.service;

import com.snippet.auth.dto.LoginRequest;
import com.snippet.auth.dto.LoginResponse;
import com.snippet.auth.dto.RegisterRequest;
import com.snippet.auth.vo.UserInfoVO;

/**
 * 用户认证相关业务。
 */
public interface AuthService {

    UserInfoVO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
