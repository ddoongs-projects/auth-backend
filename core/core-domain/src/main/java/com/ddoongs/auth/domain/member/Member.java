package com.ddoongs.auth.domain.member;

import static java.util.Objects.requireNonNull;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.Assert;

@AllArgsConstructor
@Getter
public class Member {

  private Long id;
  private Email email;
  private Password password;
  private List<ProviderDetail> providerDetails = new ArrayList<>();
  private DefaultDateTime defaultDateTime;

  private Member() {}

  public static Member register(RegisterMember registerMember, PasswordEncoder passwordEncoder) {
    Member member = new Member();

    member.email = new Email(requireNonNull(registerMember.email()));
    member.password = Password.of(requireNonNull(registerMember.password()), passwordEncoder);
    return member;
  }

  public static Member registerOAuth2(
      AppendProviderDetail providerUser, PasswordEncoder passwordEncoder) {
    Member member = new Member();

    member.email = new Email(providerUser.email());
    member.password = Password.ofRandom(passwordEncoder);
    member.providerDetails.add(providerUser.toProviderDetail());
    return member;
  }

  public void validatePassword(String password, PasswordEncoder passwordEncoder) {
    if (!this.password.matches(password, passwordEncoder)) {
      throw new PasswordMismatchException();
    }
  }

  public void changePassword(String password, PasswordEncoder passwordEncoder) {
    this.password = Password.of(password, passwordEncoder);
  }

  public void connectOAuth2(AppendProviderDetail providerUser) {
    ProviderDetail providerDetail = providerUser.toProviderDetail();

    Assert.isTrue(!this.providerDetails.contains(providerDetail), "이미 해당 정보로 가입했습니다.");

    this.providerDetails.add(providerDetail);
  }
}
