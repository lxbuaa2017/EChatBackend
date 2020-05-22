package com.example.echatbackend.service;

import com.example.echatbackend.entity.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.SecurityUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    //12小时后过期
    private final static int EXPIRE_HOURS = 24;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @NotNull
    @Contract(pure = true)
    private static String genRedisKeyForToken(@NotNull String uuid) {
        return "token:" + uuid;
    }

    @NotNull
    @Contract(pure = true)
    private static String genRedisKeyForUserId(int uuid) {
        return "userId:" + uuid;
    }

    public String createToken(int userId) {
        deleteToken(userId);
        String token;
        Boolean hasKey;
        do {
            token = RandomStringUtils.randomAlphanumeric(32);
            hasKey = stringRedisTemplate.hasKey(genRedisKeyForToken(token));
        } while (hasKey != null && hasKey);
        stringRedisTemplate.opsForValue().set(genRedisKeyForToken(token), String.valueOf(userId), EXPIRE_HOURS, TimeUnit.HOURS);
        stringRedisTemplate.opsForValue().set(genRedisKeyForUserId(userId), token, EXPIRE_HOURS, TimeUnit.HOURS);
        return token;
    }

    public Integer queryByToken(String token) {
        String userId = stringRedisTemplate.opsForValue().get(genRedisKeyForToken(token));
        return StringUtils.isEmpty(userId) ? null : Integer.parseInt(userId);
    }

    public void deleteToken(int userId) {
        String token = stringRedisTemplate.opsForValue().get(genRedisKeyForUserId(userId));
        stringRedisTemplate.delete(genRedisKeyForUserId(userId));
        if (token != null) {
            stringRedisTemplate.delete(genRedisKeyForToken(token));
        }
    }

    public void refreshToken(int userId, @NotNull String token) {
        stringRedisTemplate.expire(genRedisKeyForToken(token), EXPIRE_HOURS, TimeUnit.HOURS);
        stringRedisTemplate.expire(genRedisKeyForUserId(userId), EXPIRE_HOURS, TimeUnit.HOURS);
    }

    public User getCurrentUser() {
        return (User) SecurityUtils.getSubject().getPrincipal();
    }
}
