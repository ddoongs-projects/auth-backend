package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.Password;
import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.member.RegisterMember;
import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import java.util.ArrayList;

public class MemberFixture {

  private MemberFixture() {}

  public static RegisterMember registerMember() {
    return new RegisterMember("duk9741@gmail.com", "123asd!@#");
  }

  public static RegisterMember registerMember(String email) {
    return new RegisterMember(email, "123asd!@#");
  }

  public static Member member(PasswordEncoder passwordEncoder) {
    return new Member(
        1L,
        new Email("duk9741@gmail.com"),
        Password.of("123asd!@#", passwordEncoder),
        new ArrayList<>(),
        DefaultDateTime.now());
  }

  public static Member member(String email, PasswordEncoder passwordEncoder) {
    return new Member(
        1L,
        new Email(email),
        Password.of("123asd!@#", passwordEncoder),
        new ArrayList<>(),
        DefaultDateTime.now());
  }
}
