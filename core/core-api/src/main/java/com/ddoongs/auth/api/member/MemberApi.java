package com.ddoongs.auth.api.member;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

  @PostMapping("/members/reset-password")
  public void resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
    memberService.resetPassword(request.toEmail(), request.password(), request.verificationId());
  }

  @GetMapping("/members/me")
  public MemberResponse memberResponse(RequestMember requestMember) {
    return MemberResponse.of(memberService.find(requestMember.id()));
  }
}
