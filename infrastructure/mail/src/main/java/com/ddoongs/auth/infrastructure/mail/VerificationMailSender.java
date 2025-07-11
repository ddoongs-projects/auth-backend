package com.ddoongs.auth.infrastructure.mail;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCreatedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class VerificationMailSender {

  private static final String SUBJECT_FORMAT = "[DDOONGS] %s 인증 메일 입니다.";

  private final JavaMailSender mailSender;
  private final EmailVerificationCodeHtmlLoader htmlLoader;

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  public void handleVerificationCreatedEvent(VerificationCreatedEvent event) {
    Verification verification = event.verification();
    MimeMessage mimeMessage = mailSender.createMimeMessage();

    MimeMessageHelper helper = null;
    try {
      helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setTo(verification.getEmail().address());
      helper.setSubject(SUBJECT_FORMAT.formatted(verification.getPurpose().getDescription()));
      helper.setText(htmlLoader.loadWith(verification.getCode(), verification.getPurpose()), true);
    } catch (MessagingException e) {
      log.error(e.getMessage(), e);
    }

    mailSender.send(mimeMessage);
  }
}
