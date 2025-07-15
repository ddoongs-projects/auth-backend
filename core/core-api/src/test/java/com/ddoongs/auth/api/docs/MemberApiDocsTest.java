package com.ddoongs.auth.api.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.ddoongs.auth.api.member.MemberApi;
import com.ddoongs.auth.api.member.MemberRegisterRequest;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberService;
import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.support.MemberFixture;
import com.ddoongs.auth.domain.support.TestFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@AutoConfigureRestDocs
@WebMvcTest(MemberApi.class)
@Tag("restdocs")
class MemberApiDocsTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MemberService memberService;

  private PasswordEncoder passwordEncoder;

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
                fieldWithPath("email").description("회원 이메일"))));
  }
}
