package com.ddoongs.auth.api.member;

import com.ddoongs.auth.domain.member.Member;

public record MemberRegisterResponse(Long memberId, String email) {

  public static MemberRegisterResponse of(Member member) {
    return new MemberRegisterResponse(member.getId(), member.getEmail().address());
  }
}
