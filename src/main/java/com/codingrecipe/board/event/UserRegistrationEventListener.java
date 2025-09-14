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
            log.error("[UserRegistrationEventListener] {}님 인증 메일 발송", event.getMember().getEmail());
            emailService.sendVerificationEmail(event.getMember());
        } catch (Exception e) {
            log.error("[UserRegistrationEventListener] {}님 인증 메일 발송 실패", event.getMember().getEmail(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭션 커밋 후 실행 (@Transactional 필요)
    @EventListener
    public void handleTempPasswordRequest(TempPasswordEvent event) {
        try {
            emailService.sendTempPasswordEmail(event.getMember().getEmail(), event.getTempPassword());
        } catch (Exception e) {
            log.error("[UserRegistrationEventListener] {}님 임시 비밀번호 메일 발송 실패", event.getMember(), e);
        }
    }
}

