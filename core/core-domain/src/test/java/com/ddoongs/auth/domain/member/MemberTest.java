package com.ddoongs.auth.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.domain.support.MemberFixture;
import com.ddoongs.auth.domain.support.TestFixture;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class MemberTest {

  private PasswordEncoder passwordEncoder;

  static Stream<Arguments> provideNullForRegister() {
    return Stream.of(
        Arguments.of(new RegisterMember(null, "123asd!@#")),
        Arguments.of(new RegisterMember("duk9741@gmail.com", null)),
        Arguments.of(new RegisterMember(null, null)));
  }

  @BeforeEach
  void setup() {
    passwordEncoder = TestFixture.passwordEncoder();
  }

  @Test
  void register() {
    RegisterMember registerMember = MemberFixture.registerMember();
    Member member = Member.register(registerMember, passwordEncoder);

    assertThat(member.getEmail().address()).isEqualTo(registerMember.email());
    assertThat(member.getPassword().getPasswordHash()).isNotEqualTo(registerMember.password());
  }

  @ParameterizedTest
  @MethodSource("provideNullForRegister")
  void registerFail(RegisterMember registerMember) {
    assertThatThrownBy(() -> Member.register(registerMember, passwordEncoder))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @CsvSource({
    "duk9741, 123asd!@#",
    "@gmail.com, 123asd!@#",
    "duk9741@gmail.com, 123",
    "gmail.com, 123"
  })
  void registerFailInvalidValue(String email, String password) {
    assertThatThrownBy(() -> Member.register(new RegisterMember(email, password), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validatePassword() {
    Member member =
        Member.register(new RegisterMember("test@test.com", "123qwe!@#"), passwordEncoder);

    assertThatCode(() -> member.validatePassword("123qwe!@#", passwordEncoder))
        .doesNotThrowAnyException();

    assertThatThrownBy(() -> member.validatePassword("456rty$%^", passwordEncoder))
        .isInstanceOf(PasswordMismatchException.class);
  }

  @Test
  void changePassword() {
    String oldPassword = "123qwe!@#";
    Member member =
        Member.register(new RegisterMember("test@test.com", oldPassword), passwordEncoder);

    String newPassword = "456rty$%^";
    member.changePassword(newPassword, passwordEncoder);

    assertThatThrownBy(() -> member.validatePassword(oldPassword, passwordEncoder))
        .isInstanceOf(PasswordMismatchException.class);
    assertThatCode(() -> member.validatePassword(newPassword, passwordEncoder))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("OAuth2 계정을 연결할 수 있다")
  void connectOAuth2() {
    Member member =
        Member.register(new RegisterMember("test@test.com", "password123"), passwordEncoder);

    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "test@test.com");

    member.connectOAuth2(providerDetail);

    assertThat(member.getProviderDetails()).hasSize(1);
    assertThat(member.getProviderDetails().get(0).getProvider()).isEqualTo(Provider.GOOGLE);
    assertThat(member.getProviderDetails().get(0).getProviderId()).isEqualTo("google123");
  }

  @Test
  @DisplayName("여러 OAuth2 제공자를 연결할 수 있다")
  void connectMultipleOAuth2Providers() {
    Member member =
        Member.register(new RegisterMember("test@test.com", "password123"), passwordEncoder);

    AppendProviderDetail googleDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "test@test.com");
    AppendProviderDetail kakaoDetail =
        new AppendProviderDetail(Provider.KAKAO, "kakao456", "test@test.com");

    member.connectOAuth2(googleDetail);
    member.connectOAuth2(kakaoDetail);

    assertThat(member.getProviderDetails()).hasSize(2);
    assertThat(member.getProviderDetails())
        .extracting(ProviderDetail::getProvider)
        .containsExactlyInAnyOrder(Provider.GOOGLE, Provider.KAKAO);
  }

  @Test
  @DisplayName("이미 연결된 OAuth2 정보로 다시 연결하면 예외가 발생한다")
  void connectOAuth2_duplicateProvider() {
    Member member =
        Member.register(new RegisterMember("test@test.com", "password123"), passwordEncoder);

    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "test@test.com");

    member.connectOAuth2(providerDetail);

    assertThatThrownBy(() -> member.connectOAuth2(providerDetail))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("이미 해당 정보로 가입했습니다.");
  }

  @Test
  @DisplayName("OAuth2로 등록한 회원도 OAuth2 계정을 추가로 연결할 수 있다")
  void connectOAuth2_toOAuth2RegisteredMember() {
    AppendProviderDetail googleDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "oauth@test.com");
    Member member = Member.registerOAuth2(googleDetail, passwordEncoder);

    AppendProviderDetail kakaoDetail =
        new AppendProviderDetail(Provider.KAKAO, "kakao456", "oauth@test.com");

    member.connectOAuth2(kakaoDetail);

    assertThat(member.getProviderDetails()).hasSize(2);
    assertThat(member.getProviderDetails())
        .extracting(ProviderDetail::getProvider)
        .containsExactlyInAnyOrder(Provider.GOOGLE, Provider.KAKAO);
  }

  @Test
  @DisplayName("OAuth2로 회원을 등록할 수 있다")
  void registerOAuth2() {
    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "oauth@test.com");

    Member member = Member.registerOAuth2(providerDetail, passwordEncoder);

    assertThat(member.getEmail().address()).isEqualTo("oauth@test.com");
    assertThat(member.getProviderDetails()).hasSize(1);
    assertThat(member.getProviderDetails().get(0).getProvider()).isEqualTo(Provider.GOOGLE);
    assertThat(member.getProviderDetails().get(0).getProviderId()).isEqualTo("google123");
    assertThat(member.getPassword()).isNotNull();
  }
}
