package com.codingrecipe.board.event;

import com.codingrecipe.board.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegistrationEventListener {

    private final EmailService emailService;

    // spring에 의해 자동으로 호출됨
    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistration(UserRegistrationEvent event) {
        try {
            emailService.sendVerificationEmail(event.getMember());
        } catch (Exception e) {
            log.error("{}님 인증 메일 발송 실패", event.getMember().getEmail(), e);
        }
    }

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTempPasswordRequest(TempPasswordEvent event) {
        try {
            emailService.sendTempPasswordEmail(event.getMember().getEmail(), event.getTempPassword());
        } catch (Exception e) {
            log.error("{}님 임시 비밀번호 메일 발송 실패", event.getMember(), e);
        }
    }
}

