package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.member.RegisterMember;

public class MemberFixture {

  private MemberFixture() {}

  public static RegisterMember registerMember() {
    return new RegisterMember("duk9741@gmail.com", "123asd!@#");
  }
}
