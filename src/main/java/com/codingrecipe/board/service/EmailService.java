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

// 동기문제로 인해 발생한 email 발송 씹힘 현상을 해결 == 비동기로 변경 후 리스너를 추가하여 메일 전송이 확실하게 일어나도록 함

import com.codingrecipe.board.event.UserRegistrationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


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
     * --- @Async 어노테이션 추가 ---
     * MemberService에서 이 메소드를 호출하면, 실제 메일 발송을 기다리지 않고 즉시 다음 코드로 진행됩니다.
     */
//    @Async("threadPoolTaskExecutor")
//    public void sendVerificationEmail(Member member) {
//        log.info("{}님에게 인증 메일을 비동기로 발송합니다. 스레드: {}", member.getEmail(), Thread.currentThread().getName());
//        String token = jwtUtil.generateToken(member.getEmail(), member.getRole().name());
//        String verificationLink = baseUrl + "?token=" + token;
//
//        String htmlContent = generateEmailTemplate(verificationLink, member.getNickname());
//        sendEmail(member.getEmail(), VERIFICATION_SUBJECT, htmlContent);
//        log.info("{}님에게 인증 메일 비동기 발송 완료.", member.getEmail());
//    } // => 아래 추가된 handleUserRegistration으로 대신합니다. ...

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistration(UserRegistrationEvent event) {
        Member member = event.getMember();
        log.info("{}님에게 인증 메일을 비동기로 발송합니다. (이벤트 리스너) 스레드: {}", member.getEmail(), Thread.currentThread().getName());

        String token = jwtUtil.generateToken(member.getEmail(), member.getRole().name());
        String verificationLink = baseUrl + "?token=" + token;
        String htmlContent = generateEmailTemplate(verificationLink, member.getNickname());

        sendEmail(member.getEmail(), VERIFICATION_SUBJECT, htmlContent);
        log.info("{}님에게 인증 메일 비동기 발송 완료. (이벤트 리스너)", member.getEmail());
    }

    /**
     * --- @Async 어노테이션 추가 ---
     */
    @Async("threadPoolTaskExecutor")
    public void sendTempPasswordEmail(String email, String tempPassword) {
        log.info("{}님에게 임시 비밀번호를 비동기로 발송합니다. 스레드: {}", email, Thread.currentThread().getName());
        String subject = "[For-Foreigner] 임시 비밀번호 안내입니다";
        String htmlText = "<h1>임시 비밀번호 안내</h1>"
                + "<p>로그인 후, 반드시 비밀번호를 변경해주세요</p>"
                + "<p>임시 비밀번호: <strong>" + tempPassword + "</strong></p>";
        sendEmail(email, subject, htmlText);
        log.info("{}님에게 임시 비밀번호 비동기 발송 완료.", email);
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

    private String generateEmailTemplate(String verificationLink, String username) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/verification-email.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("${username}", username)
                    .replace("${verificationLink}", verificationLink);
        } catch (IOException e) {
            throw new RuntimeException("인증 메일 템플릿을 불러오는 데 실패했습니다", e);
        }
    }
}
