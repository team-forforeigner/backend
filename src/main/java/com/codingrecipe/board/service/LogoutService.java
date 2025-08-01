package com.codingrecipe.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RedisTemplate<String, String> redisTemplate;

    public void logout(String token, long expiration) {
        // 블랙리스트에 토큰을 추가하고, 남은 유효시간만큼만 저장
        redisTemplate.opsForValue().set(token, "logout", expiration, TimeUnit.MILLISECONDS);
    }
}