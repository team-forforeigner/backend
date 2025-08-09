// Spring Security, JWT, OAuth2, CORS 관련 보안 설정 관리
package com.codingrecipe.board.config;

import com.codingrecipe.board.security.CustomOAuth2UserService;
import com.codingrecipe.board.security.JwtFilter;
import com.codingrecipe.board.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oauthUserService; // 소셜 로그인 사용자 정보 처리 서비스
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // OAuth2 로그인 성공 핸들러
    private final JwtFilter jwtFilter; // JWT 인증 필터
    private final AuthenticationEntryPoint authenticationEntryPoint; // 인증되지 않은 사용자의 접근 시 처리 핸들러
    private final AccessDeniedHandler accessDeniedHandler; // 접근 거부 핸들러

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 요청에 대한 보안 필터 체인을 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 서버 기본 인증 및 세션 정책 설정
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .formLogin(form -> form.disable()) // 기본 폼 로그인 비활성화
                .httpBasic(basic -> basic.disable()) // HTTP Basic 인증 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않는 stateless 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource())); // CORS 설정 적용

        // API 경로별 접근 권한 규칙 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // 관리자 API: '/api/admin/**' 경로는 'ADMIN' 역할을 가진 사용자만 접근 가능
                .requestMatchers("/api/auth/**", "/login/oauth2/**", "/oauth-redirect").permitAll() // 인증/로그인 API: 모든 사용자가 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/boards", "/api/boards/**").permitAll() // 게시판 조회 API(GET): 모든 사용자가 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/tips").permitAll() // 팁 API(GET): 모든 사용자가 접근 가능
                .requestMatchers(HttpMethod.POST, "/api/analyze").permitAll() // AI API(POST): 모든 사용자가 접근 가능
                .anyRequest().authenticated()); // 그 외 모든 API: 인증된 사용자만 접근 가능

        // OAuth2 로그인 관련 설정
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
                .userInfoEndpoint(u -> u.userService(oauthUserService)));

        // 모든 요청 처리 이전에 JWT 필터를 먼저 실행하도록 설정
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // 인증 실패 시 응답 처리 설정
        // (*웹 브라우저로 접속했을 때 구글 로그인이 떠야 되는데, 이걸 적용하고 나서 안 떠서 주석 처리함)
        /*http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint) // 인증되지 않은 사용자가 접근할 경우
                .accessDeniedHandler(accessDeniedHandler) // 접근이 거부된 경우 (예: 권한 부족)
        );*/

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 프론트엔드 출처(도메인) 설정
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://forforeigner.com.s3-website.ap-northeast-2.amazonaws.com",
                "https://forforeigner.site",
                "https://ai.navoodiai.site"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(Arrays.asList("*")); // 허용할 HTTP 헤더
        configuration.setAllowCredentials(true); // 쿠키 등 허용
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 CORS 설정을 적용
        return source;
    }
}
