package com.ddoongs.auth.api.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

class RequestMemberArgumentResolverTest {

  private RequestMemberArgumentResolver resolver;
  private MethodParameter parameter;
  private ModelAndViewContainer mavContainer;
  private NativeWebRequest request;
  private WebDataBinderFactory binderFactory;

  @BeforeEach
  void setUp() {
    resolver = new RequestMemberArgumentResolver();
    parameter = mock(MethodParameter.class);
    mavContainer = mock(ModelAndViewContainer.class);
    request = mock(NativeWebRequest.class);
    binderFactory = mock(WebDataBinderFactory.class);
  }

  @Test
  @DisplayName("RequestMember 타입의 파라미터를 지원한다")
  void supportsParameter_whenRequestMemberType_returnsTrue() {
    given(parameter.getParameterType()).willReturn((Class) RequestMember.class);

    boolean result = resolver.supportsParameter(parameter);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("RequestMember 타입이 아닌 파라미터는 지원하지 않는다")
  void supportsParameter_whenNotRequestMemberType_returnsFalse() {
    given(parameter.getParameterType()).willReturn((Class) String.class);

    boolean result = resolver.supportsParameter(parameter);

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("헤더에서 멤버 정보를 추출하여 RequestMember 객체를 생성한다")
  void resolveArgument_withValidHeaders_returnsRequestMember() {
    given(request.getHeader("X-Member-Id")).willReturn("123");
    given(request.getHeader("X-Member-Email")).willReturn("test@example.com");

    RequestMember result =
        (RequestMember) resolver.resolveArgument(parameter, mavContainer, request, binderFactory);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(123L);
    assertThat(result.email()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("X-Member-Id 헤더가 없으면 예외가 발생한다")
  void resolveArgument_withoutMemberIdHeader_throwsException() {
    given(request.getHeader("X-Member-Id")).willReturn(null);
    given(request.getHeader("X-Member-Email")).willReturn("test@example.com");

    assertThatThrownBy(
            () -> resolver.resolveArgument(parameter, mavContainer, request, binderFactory))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("X-User not found in request header");
  }

  @Test
  @DisplayName("X-Member-Email 헤더가 없으면 예외가 발생한다")
  void resolveArgument_withoutMemberEmailHeader_throwsException() {
    given(request.getHeader("X-Member-Id")).willReturn("123");
    given(request.getHeader("X-Member-Email")).willReturn(null);

    assertThatThrownBy(
            () -> resolver.resolveArgument(parameter, mavContainer, request, binderFactory))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("X-User not found in request header");
  }

  @Test
  @DisplayName("X-Member-Id가 숫자가 아니면 예외가 발생한다")
  void resolveArgument_withInvalidMemberIdFormat_throwsException() {
    given(request.getHeader("X-Member-Id")).willReturn("invalid");
    given(request.getHeader("X-Member-Email")).willReturn("test@example.com");

    assertThatThrownBy(
            () -> resolver.resolveArgument(parameter, mavContainer, request, binderFactory))
        .isInstanceOf(NumberFormatException.class);
  }
}
