package com.ddoongs.auth.api.docs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockOAuth2Controller {

  @GetMapping("/oauth2/authorization/{provider}")
  public ResponseEntity<Void> oauth2Authorization(@PathVariable String provider) {
    // OAuth2 제공자 인증 페이지로 리다이렉트하는 것을 시뮬레이션
    // 실제 프로덕션에서는 Spring Security OAuth2가 이 역할을 수행
    String redirectUrl =
        String.format("https://%s.com/oauth2/authorize?client_id=xxx&redirect_uri=xxx", provider);

    return ResponseEntity.status(302).header("Location", redirectUrl).build();
  }
}
