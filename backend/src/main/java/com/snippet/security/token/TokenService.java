package com.snippet.security.token;

public interface TokenService {

    IssuedToken issueAccessToken(Long userId, String username);
}