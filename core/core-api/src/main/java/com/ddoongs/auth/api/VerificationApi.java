package com.ddoongs.auth.api;

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
  public void issue(@RequestBody @Valid CreateVerificationRequest request) {
    verificationService.issue(request.toCommand());
  }
}
