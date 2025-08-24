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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oauthUserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // [추가] Preflight 허용
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // ▼▼▼ [추가] 서바이벌 API에 대한 인증 규칙 추가 ▼▼▼
                .requestMatchers("/api/survival/**").authenticated()
                // ▲▲▲ [추가] 서바이벌 API에 대한 인증 규칙 추가 ▲▲▲
                .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/verify", "/api/auth/reset-password").permitAll()
                .requestMatchers("/login/oauth2/**", "/oauth-redirect").permitAll() // 인증/로그인 API: 모든 사용자가 접근 가능

                .requestMatchers(HttpMethod.GET, "/api/boards", "/api/boards/**").permitAll() // 게시판 조회 API(GET): 모든 사용자가 접근 가능
                .requestMatchers(HttpMethod.POST, "/api/analyze").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/banners").permitAll() // 배너 조회 API(GET): 모든 사용자가 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/tips").permitAll()
                .anyRequest().authenticated()); // 그 외 모든 API: 인증된 사용자만 접근 가능

        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
                .userInfoEndpoint(u -> u.userService(oauthUserService)));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // 개발 환경 (프론트엔드)
                "http://forforeigner.com.s3-website.ap-northeast-2.amazonaws.com",
                "https://forforeigner.site",
                "https://ai.navoodiai.site",

                "https://api.forforeigner.site",
                "http://api.forforeigner.site",

                // 변경된 서버 IP 주소
                "http://3.39.22.172"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
