package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCreatedEvent;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class FakeVerificationSender {

  private final List<Email> sentMessages = new ArrayList<>();

  @EventListener
  public void handleVerificationCreatedEvent(VerificationCreatedEvent event) {
    Verification verification = event.verification();
    log.info(
        "Fake Mail Sender: Pretending to send email to {}",
        verification.getEmail().address());
    sentMessages.add(verification.getEmail());
  }

  public void clear() {
    sentMessages.clear();
  }
}
