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
import org.springframework.transaction.annotation.Transactional;

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

    private static final String VERIFICATION_SUBJECT = "[ForForeigner] 이메일 인증을 완료해주세요";

    /**
     * MemberService에서 이 메소드를 호출하면, 실제 메일 발송을 기다리지 않고 즉시 다음 코드로 진행됩니다.
     */
    @Async("threadPoolTaskExecutor")
    public void sendVerificationEmail(Member member) {
        try {
            String token = jwtUtil.generateToken(member);
            String verificationLink = baseUrl + "?token=" + token;
            String htmlContent = generateVerificationEmailTemplate(verificationLink, member.getNickname());
            sendEmail(member.getEmail(), VERIFICATION_SUBJECT, htmlContent);
        } catch (Exception e) {
            log.error("메일 발송 실패: {}", member.getEmail(), e);
        }
    }

    @Async("threadPoolTaskExecutor")
    public void sendTempPasswordEmail(String email, String tempPassword) {
        try {
            String subject = "[For-Foreigner] 임시 비밀번호 안내입니다";
            String username = email.split("@")[0]; // 이메일 앞부분을 username으로 사용
            String htmlText = generateTempPasswordEmailTemplate(username, tempPassword);
            sendEmail(email, subject, htmlText);
        } catch (Exception e) {
            log.error("메일 발송 실패: {}", email, e);
        }
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
            log.error("메일 전송에 실패했습니다. 받는 사람: {}", to, e);
            throw new RuntimeException("메일 전송에 실패했습니다", e);
        }
    }

    // 이메일 인증 템플릿
    private String generateVerificationEmailTemplate(String verificationLink, String username) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/verification-email.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("${username}", username)
                    .replace("${verificationLink}", verificationLink);
        } catch (IOException e) {
            throw new RuntimeException("인증 메일 템플릿을 불러오는 데 실패했습니다", e);
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
            throw new RuntimeException("임시 비밀번호 메일 템플릿 불러오기 실패", e);
        }
    }

}
