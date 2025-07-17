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
    private String baseUrl;

    private static final String VERIFICATION_SUBJECT = "[ForForeigner] 이메일 인증을 완료해주세요.";

    public void sendVerificationEmail(Member member) {
        String token = jwtUtil.generateToken(member.getUserId());
        String verificationLink = baseUrl + "?token=" + token;
        String htmlContent = generateEmailTemplate(verificationLink, member.getName());
        sendEmail(member.getEmail(), VERIFICATION_SUBJECT, htmlContent);
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
            throw new RuntimeException("메일 전송에 실패했습니다.", e);
        }
    }

    private String generateEmailTemplate(String verificationLink, String username) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/verification-email.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("${username}", username)
                    .replace("${verificationLink}", verificationLink);
        } catch (IOException e) {
            throw new RuntimeException("인증 메일 템플릿을 불러오는 데 실패했습니다.", e);
        }
    }

    public void sendTempPasswordEmail(String email, String tempPassword) {
        String subject = "[For-Foreigner] 임시 비밀번호 안내입니다.";
        String htmlText = "<h1>임시 비밀번호 안내</h1>"
                + "<p>로그인 후, 반드시 비밀번호를 변경해주세요.</p>"
                + "<p>임시 비밀번호: <strong>" + tempPassword + "</strong></p>";
        sendEmail(email, subject, htmlText);
    }

    public String createCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(888888) + 111111);
    }
}