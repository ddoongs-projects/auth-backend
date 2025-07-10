package com.ddoongs.auth.infrastructure.mail;

import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class VerificationMailSender implements VerificationSender {

  private static final String SUBJECT = "[BizKit] 회원 가입을 위해 메일을 인증해 주세요.";

  private final JavaMailSender mailSender;
  private final EmailVerificationCodeHtmlLoader htmlLoader;

  @Async
  @Override
  public void send(Verification verification) {
    MimeMessage mimeMessage = mailSender.createMimeMessage();

    MimeMessageHelper helper = null;
    try {
      helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setTo(verification.getEmail().address());
      helper.setSubject(SUBJECT);
      helper.setText(htmlLoader.loadWith(verification.getCode()), true);
    } catch (MessagingException e) {
      log.error(e.getMessage(), e);
    }

    mailSender.send(mimeMessage);
  }
}
