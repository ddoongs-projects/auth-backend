package com.ddoongs.auth.redis.config;

import com.ddoongs.auth.domain.token.RefreshToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory();
  }

  @Bean // RefreshToken 전용 RedisTemplate Bean 등록
  public RedisTemplate<String, RefreshToken> redisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, RefreshToken> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory); // Redis 연결 팩토리 설정

    // KeySerializer: Redis 키를 문자열(JTI)로 직렬화
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    // ValueSerializer: RefreshToken 객체를 JSON으로 직렬화
    // ObjectMapper 커스터마이징: Instant 지원 및 ISO-8601 포맷 사용
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    Jackson2JsonRedisSerializer<RefreshToken> serializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, RefreshToken.class);

    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);

    template.afterPropertiesSet(); // 프로퍼티 주입 후 초기화
    return template; // 완성된 RedisTemplate 반환
  }
}
