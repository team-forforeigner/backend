// 회원 인증, 임시 비밀번호 발송 등 이메일 전송 관련 비즈니스 로직을 처리하는 서비스
package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.security.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    @Value("${app.verification-base-url}")
    private String baseUrl; // 이메일 인증 링크의 기본 URL

    private static final String VERIFICATION_SUBJECT = "[ForForeigner] 이메일 인증을 완료해주세요";

    /**
     * 회원가입 후 인증 이메일을 발송
     */
    public void sendVerificationEmail(Member member) {
        // 이메일 인증용 JWT 토큰 생성
        String token = jwtUtil.generateToken(member.getEmail());
        String verificationLink = baseUrl + "?token=" + token;

        // 이메일 템플릿에 인증 링크와 사용자 이름을 채워 HTML 내용 생성
        String htmlContent = generateEmailTemplate(verificationLink, member.getNickname());
        sendEmail(member.getEmail(), VERIFICATION_SUBJECT, htmlContent);
    }

    /**
     * 임시 비밀번호를 이메일로 발송
     */
    public void sendTempPasswordEmail(String email, String tempPassword) {
        String subject = "[For-Foreigner] 임시 비밀번호 안내입니다";
        String htmlText = "<h1>임시 비밀번호 안내</h1>"
                + "<p>로그인 후, 반드시 비밀번호를 변경해주세요</p>"
                + "<p>임시 비밀번호: <strong>" + tempPassword + "</strong></p>";
        sendEmail(email, subject, htmlText);
    }

    /**
     * 6자리 랜덤 숫자 인증 코드를 생성 (현재는 임시 비밀번호 생성용으로 사용)
     */
    public String createCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(888888) + 111111);
    }

    /**
     * HTML 형식의 이메일을 실제로 발송하는 내부 메서드
     */
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true로 설정하여 HTML 형식으로 발송
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 전송에 실패했습니다", e);
        }
    }

    /**
     * resources/templates/verification-email.html 파일을 읽어와 내용을 채우는 메서드
     */
    private String generateEmailTemplate(String verificationLink, String username) {
        try {
            // HTML 템플릿 파일 로드
            ClassPathResource resource = new ClassPathResource("templates/verification-email.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("${username}", username)
                    .replace("${verificationLink}", verificationLink);
        } catch (IOException e) {
            throw new RuntimeException("인증 메일 템플릿을 불러오는 데 실패했습니다", e);
        }
    }
}
