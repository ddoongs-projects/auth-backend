package com.ddoongs.auth.api.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;
  private final OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;
  private final AuthenticationSuccessHandler authenticationSuccessHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.httpBasic(AbstractHttpConfigurer::disable);
    http.formLogin(AbstractHttpConfigurer::disable);
    http.sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.csrf(AbstractHttpConfigurer::disable);
    http.logout(AbstractHttpConfigurer::disable);
    http.cors(AbstractHttpConfigurer::disable);
    http.headers(AbstractHttpConfigurer::disable);
    http.requestCache(AbstractHttpConfigurer::disable);

    http.authorizeHttpRequests(authRequest -> authRequest.anyRequest().permitAll());

    http.oauth2Login(oauth2 -> oauth2
        .userInfoEndpoint(
            userInfo -> userInfo.userService(oauth2UserService).oidcUserService(oidcUserService))
        .successHandler(authenticationSuccessHandler));

    return http.build();
  }

  @Bean
  @ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
  public WebSecurityCustomizer configureH2ConsoleEnable() {
    return web -> web.ignoring().requestMatchers(PathRequest.toH2Console());
  }
}
