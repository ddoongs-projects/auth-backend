package com.ddoongs.auth.api.member;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.Provider;
import com.ddoongs.auth.domain.member.ProviderDetail;
import java.util.List;

public record MemberResponse(Long id, String email, List<Provider> oauth2Providers) {

  public static MemberResponse of(Member member) {
    return new MemberResponse(
        member.getId(),
        member.getEmail().address(),
        member.getProviderDetails().stream().map(ProviderDetail::getProvider).toList());
  }
}
