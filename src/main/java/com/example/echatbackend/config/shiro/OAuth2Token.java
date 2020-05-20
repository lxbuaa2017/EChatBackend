package com.example.echatbackend.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.jetbrains.annotations.Contract;

public class OAuth2Token implements AuthenticationToken {
    private String token;

    @Contract(pure = true)
    public OAuth2Token(String token) {
        this.token = token;
    }

    @Override
    public String getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
