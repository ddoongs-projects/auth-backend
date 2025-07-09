package com.ddoongs.auth.domain;

import static java.util.Objects.requireNonNull;

import lombok.Getter;

@Getter
public class Member {

  private Long id;
  private Email email;
  private Password password;
  private DefaultDateTime defaultDateTime;

  private Member() {}

  public static Member register(RegisterMember registerMember, PasswordEncoder passwordEncoder) {
    Member member = new Member();

    member.email = new Email(requireNonNull(registerMember.email()));
    member.password = Password.of(requireNonNull(registerMember.password()), passwordEncoder);

    return member;
  }
}
