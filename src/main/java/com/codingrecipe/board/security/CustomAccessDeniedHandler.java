package com.codingrecipe.board.security;

import com.codingrecipe.board.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 접근 권한이 없는 사용자가 보호된 리소스에 접근하려고 할 때 호출되는 핸들러 (USER가 ADMIN 권한이 필요한 API에 접근할 때 등)
 * - 403 Forbidden 응답을 반환
 */

@Slf4j
@RequiredArgsConstructor
@Component  // 컴포넌트로 빈 등록
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("No Authorities", accessDeniedException);
        log.error("Request Uri : {}", request.getRequestURI());

        String responseContent = objectMapper.writeValueAsString(
                ApiResponse.fail(403, "접근 권한이 없습니다.")
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(responseContent);
    }
}

