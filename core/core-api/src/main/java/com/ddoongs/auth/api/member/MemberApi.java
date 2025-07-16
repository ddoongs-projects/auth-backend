package com.ddoongs.auth.api.member;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberService;
import com.ddoongs.auth.domain.token.TokenPair;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberApi {

  private final MemberService memberService;

  @PostMapping("/members")
  public MemberRegisterResponse register(@RequestBody @Valid MemberRegisterRequest request) {
    Member member = memberService.register(request.toRegisterMember(), request.verificationId());
    return MemberRegisterResponse.of(member);
  }

  @PostMapping("/login")
  public TokenResponse login(@RequestBody @Valid MemberLoginRequest request) {
    TokenPair tokenPair = memberService.login(request.toLoginMember());
    return TokenResponse.of(tokenPair);
  }
}
