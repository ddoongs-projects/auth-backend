package com.ddoongs.auth.domain.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;
import org.assertj.core.api.AssertProvider;
import org.springframework.test.json.JsonPathValueAssert;

public class AssertThatUtils {

  private AssertThatUtils() {}

  public static Consumer<AssertProvider<JsonPathValueAssert>> notNull() {
    return value -> assertThat(value).isNotNull();
  }

  public static <T> Consumer<AssertProvider<JsonPathValueAssert>> equalsTo(T compare) {
    return value -> assertThat(value).isEqualTo(compare);
  }
}
