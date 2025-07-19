package com.ddoongs.auth.api.docs;

import static com.ddoongs.auth.domain.shared.CoreErrorCode.DUPLICATED_EMAIL;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.MEMBER_NOT_FOUND;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.VERIFICATION_MISMATCH;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.VERIFICATION_NOT_COMPLETED;
import static com.ddoongs.auth.restdocs.RestdocsUtils.errorCodesWithCause;
import static com.ddoongs.auth.restdocs.RestdocsUtils.errorWithCause;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.ddoongs.auth.api.member.MemberApi;
import com.ddoongs.auth.api.member.MemberRegisterRequest;
import com.ddoongs.auth.api.member.ResetPasswordRequest;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberService;
import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.support.MemberFixture;
import com.ddoongs.auth.domain.support.TestFixture;
import com.ddoongs.auth.restdocs.RestdocsTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(MemberApi.class)
class MemberApiDocsTest extends RestdocsTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  private PasswordEncoder passwordEncoder;

  @MockitoBean
  private MemberService memberService;

  @BeforeEach
  void setup() {
    passwordEncoder = TestFixture.passwordEncoder();
  }

  @DisplayName("회원등록 API 문서 생성")
  @Test
  void register() throws Exception {
    final var request = new MemberRegisterRequest("test@email.com", "123asd!@#", UUID.randomUUID());
    final Member member = MemberFixture.member(passwordEncoder);

    given(memberService.register(any(), any())).willReturn(member);

    assertThat(mvc.post()
            .uri("/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .exchange())
        .hasStatusOk()
        .apply(document(
            "member-register",
            requestFields(
                fieldWithPath("email").description("이메일"),
                fieldWithPath("password").description("비밀번호"),
                fieldWithPath("verificationId").description("인증이 완료된 인증 식별자")),
            responseFields(
                fieldWithPath("memberId").description("회원 식별자"),
                fieldWithPath("email").description("회원 이메일")),
            errorCodesWithCause(
                errorWithCause(DUPLICATED_EMAIL, "이메일 중복 시 발생"),
                errorWithCause(VERIFICATION_NOT_COMPLETED, "인증이 완료되지 않았을 떄 발생"),
                errorWithCause(VERIFICATION_MISMATCH, "인증 정보(email, purpose)가 일치하지 않을 때 발생"))));
  }

  @DisplayName("비밀번호 초기화 API 문서 생성")
  @Test
  void resetPassword() throws Exception {
    final var request = new ResetPasswordRequest("test@email.com", "123asd!@#", UUID.randomUUID());
    final Member member = MemberFixture.member(passwordEncoder);

    given(memberService.resetPassword(any(), any(), any())).willReturn(member);

    assertThat(mvc.post()
            .uri("/members/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .exchange())
        .hasStatusOk()
        .apply(document(
            "member-password-reset",
            requestFields(
                fieldWithPath("email").description("이메일"),
                fieldWithPath("password").description("비밀번호"),
                fieldWithPath("verificationId").description("인증이 완료된 인증 식별자")),
            errorCodesWithCause(
                errorWithCause(MEMBER_NOT_FOUND, "회원이 아닐 시 발생"),
                errorWithCause(VERIFICATION_NOT_COMPLETED, "인증이 완료되지 않았을 떄 발생"),
                errorWithCause(VERIFICATION_MISMATCH, "인증 정보(email, purpose)가 일치하지 않을 때 발생"))));
  }
}
