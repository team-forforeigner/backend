package com.codingrecipe.board.event;

import com.codingrecipe.board.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegistrationEventListener {

    private final EmailService emailService;

    @Async("threadPoolTaskExecutor")
    @EventListener
    public void handleUserRegistration(UserRegistrationEvent event) {
        try {
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            log.error("[{}}] {}님 인증 메일 전송", methodName, event.getMember().getEmail());
            emailService.sendVerificationEmail(event.getMember());
        } catch (Exception e) {
            log.error("{}님 임시 비밀번호 메일 전송 실패", event.getMember(), e);
        }
    }

    @Async("threadPoolTaskExecutor")
    @EventListener
    public void handleTempPasswordRequest(TempPasswordEvent event) {
        try {
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            log.error("[{}}] {}님 인증 메일 전송", methodName, event.getMember().getEmail());
            emailService.sendTempPasswordEmail(event.getMember().getEmail(), event.getTempPassword());
        } catch (Exception e) {
            log.error("{}님 임시 비밀번호 메일 전송 실패", event.getMember(), e);
        }
    }
}

