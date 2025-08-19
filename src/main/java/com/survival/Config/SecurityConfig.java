package com.survival.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보안 비활성화 (테스트용)
                .csrf(csrf -> csrf.disable())

                // 모든 HTTP 요청에 대해 접근을 허용
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                );

//        // URL별로 접근 권한을 설정합니다.
//            .authorizeHttpRequests(authorize -> authorize
//                // 루트 URL, 로그인 관련 URL, OAuth2 관련 URL은 모두에게 허용
//                .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
//
//                // '/api/user/**' 패턴의 URL은 인증된 사용자만 접근 가능
//                .requestMatchers("/api/user/**").authenticated()
//
//                // '/api/admin/**' 패턴의 URL은 'ADMIN' 역할을 가진 사용자만 접근 가능
//                .requestMatchers("/api/admin/**").hasRole("ADMIN")
//
//                // 위에서 설정한 것 이외의 모든 요청은 인증이 필요함
//                .anyRequest().authenticated()
//        )
//
//                // OAuth2 로그인을 활성화합니다.
//                .oauth2Login(oauth2 -> oauth2
//                        // 여기에 로그인 성공/실패 시의 추가 설정을 할 수 있습니다.
//                        // .userInfoEndpoint(...)
//                        //

        return http.build();
    }
}