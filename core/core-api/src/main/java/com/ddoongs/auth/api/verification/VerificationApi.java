package com.ddoongs.auth.api.verification;

import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class VerificationApi {

  private final VerificationService verificationService;

  @PostMapping("/verifications")
  public VerificationIdResponse issue(@RequestBody @Valid CreateVerificationRequest request) {
    Verification verification = verificationService.issue(request.toCommand());
    return new VerificationIdResponse(verification.getId());
  }

  @PostMapping("/verifications/verify")
  public void issue(@RequestBody @Valid VerifyVerificationRequest request) {
    verificationService.verify(request.verificationId(), request.toCode());
  }
}
