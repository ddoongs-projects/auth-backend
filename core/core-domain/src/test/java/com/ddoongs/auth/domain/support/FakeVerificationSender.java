package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationSender;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeVerificationSender implements VerificationSender {

  private final List<Email> sentMessages = new ArrayList<>();
  Logger log = LoggerFactory.getLogger(FakeVerificationSender.class);

  @Override
  public void send(Verification verification) {

    log.info(
        "Fake Mail Sender: Pretending to send email to {}",
        verification.getEmail().address());
    sentMessages.add(verification.getEmail());
  }

  /**
   * 테스트 간의 독립성을 보장하기 위해 저장된 메시지 기록을 모두 삭제합니다. JUnit의 @AfterEach 등에서 호출하면 유용합니다.
   */
  public void clear() {
    sentMessages.clear();
  }

  public List<Email> getSentMessages() {
    return sentMessages;
  }
}
