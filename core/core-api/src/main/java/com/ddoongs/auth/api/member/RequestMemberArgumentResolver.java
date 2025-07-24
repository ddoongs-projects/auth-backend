package com.ddoongs.auth.api.member;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class RequestMemberArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(RequestMember.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest request,
      WebDataBinderFactory binderFactory) {
    String memberIdStr = request.getHeader("X-Member-Id");
    String memberEmail = request.getHeader("X-Member-Email");

    if (memberIdStr == null || memberEmail == null) {
      throw new IllegalArgumentException("X-User not found in request header");
    }

    Long memberId;
    try {
      memberId = Long.parseLong(memberIdStr);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Invalid userId type in request attributes", e);
    }

    return new RequestMember(memberId, memberEmail);
  }
}
