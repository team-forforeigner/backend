//// JWT 토큰을 사용한 로그아웃 처리를 담당하는 서비스
//package com.codingrecipe.board.service;
//
//import lombok.RequiredArgsConstructor;
////import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.TimeUnit;
//
//@Service
//@RequiredArgsConstructor
//public class LogoutService {
//
////    private final RedisTemplate<String, String> redisTemplate;
//
//    /**
//     * 로그아웃 요청 시 전달된 토큰을 블랙리스트에 추가
//     */
//    public void logout(String token, long expiration) {
//        // Redis에 토큰을 키로, "logout"을 값으로 저장
//        // 토큰의 남은 유효 시간만큼만 Redis에 저장하여 메모리 낭비를 방지
//        redisTemplate.opsForValue().set(token, "logout", expiration, TimeUnit.MILLISECONDS);
//    }
//}
