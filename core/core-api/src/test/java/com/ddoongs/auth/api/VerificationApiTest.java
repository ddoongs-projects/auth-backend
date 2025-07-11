package com.ddoongs.auth.api;

import static com.ddoongs.auth.domain.util.AssertThatUtils.notNull;
import static org.assertj.core.api.Assertions.assertThat;

import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.support.FakeVerificationSender;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationRepository;
import com.ddoongs.auth.domain.verification.VerificationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@Import(AuthTestConfiguration.class)
@SpringBootTest
class VerificationApiTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FakeVerificationSender fakeVerificationSender;

  @Autowired
  private VerificationRepository verificationRepository;

  @BeforeEach
  void setUp() {
    fakeVerificationSender.clear();
  }

  @DisplayName("인증번호를 발급한다.")
  @Test
  void issue() throws Exception {
    final var request =
        new CreateVerificationRequest("test@email.com", VerificationPurpose.REGISTER);

    final var result = mvc.post()
        .uri("/verifications")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result).hasStatusOk().bodyJson().hasPathSatisfying("$.verificationId", notNull());

    VerificationIdResponse response = objectMapper.readValue(
        result.getResponse().getContentAsString(), VerificationIdResponse.class);

    Verification verification =
        verificationRepository.findById(response.verificationId()).orElseThrow();

    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.PENDING);
    assertThat(verification.getEmail().address()).isEqualTo("test@email.com");
    assertThat(verification.getPurpose()).isEqualTo(VerificationPurpose.REGISTER);

    assertThat(fakeVerificationSender.getSentMessages()).hasSize(1);
  }
}
