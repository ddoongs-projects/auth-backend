package com.ddoongs.auth.domain;

public class MemberFixture {

  private MemberFixture() {}

  public static RegisterMember registerMember() {
    return new RegisterMember("duk9741@gmail.com", "123asd!@#");
  }
}
