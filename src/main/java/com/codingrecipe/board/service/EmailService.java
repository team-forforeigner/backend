package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.security.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    @Value("${app.verification-base-url}")
    private String baseUrl;

    private static final String VERIFICATION_SUBJECT = "[ForForeigner] 회원가입 이메일 인증을 완료해주세요";
    private static final String TEMP_PASSWORD_SUBJECT = "[For-Foreigner] 임시 비밀번호 안내입니다";

    // 회원가입 인증 메일 발송
    @Async("threadPoolTaskExecutor")
    public void sendVerificationEmail(Member member) {
        log.info("{}님에게 인증 메일을 비동기로 발송합니다. 스레드: {}", member.getEmail(), Thread.currentThread().getName());

        // [수정] 로그인용 토큰 대신, 이메일 인증 전용 토큰을 생성하도록 변경
        String token = jwtUtil.generateVerificationToken(member.getEmail());

        String verificationLink = baseUrl + "?token=" + token;

        String htmlContent = generateVerificationEmailTemplate(verificationLink, member.getNickname());
        sendEmail(member.getEmail(), VERIFICATION_SUBJECT, htmlContent);
        log.info("{}님에게 인증 메일 비동기 발송 완료.", member.getEmail());
    }

    // 임시 비밀번호 메일 발송
    @Async("threadPoolTaskExecutor")
    public void sendTempPasswordEmail(String email, String tempPassword) {
        String htmlContent = generateTempPasswordEmailTemplate(email, tempPassword);
        sendEmail(email, TEMP_PASSWORD_SUBJECT, htmlContent);
    }

    public String createCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(888888) + 111111);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            log.error("[{}}] 메일 전송 실패: {}", methodName,to, e);
            throw new RuntimeException("메일 전송에 실패했습니다", e);
        }
    }

    // 회원가입 인증 템플릿
    private String generateVerificationEmailTemplate(String verificationLink, String email) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/verification-email.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("${email}", email)
                    .replace("${verificationLink}", verificationLink);
        } catch (IOException e) {
            throw new RuntimeException("회원가입 인증 메일 템플릿 불러오기 실패: ", e);
        }
    }

    // 임시 비밀번호 템플릿
    public String generateTempPasswordEmailTemplate(String username, String tempPassword) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/temp-password-email.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("${username}", username)
                    .replace("${tempPassword}", tempPassword);
        } catch (IOException e) {
            throw new RuntimeException("임시 비밀번호 메일 템플릿 불러오기 실패: ", e);
        }
    }

}
