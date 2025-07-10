package com.ddoongs.auth.infrastructure.mail;

import com.ddoongs.auth.domain.verification.VerificationCode;
import java.util.HashMap;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
public class EmailVerificationCodeHtmlThymeleafLoader implements EmailVerificationCodeHtmlLoader {

  private static final String TEMPLATE = "email-verification-code-template";

  private final SpringTemplateEngine templateEngine;

  public EmailVerificationCodeHtmlThymeleafLoader(SpringTemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  @Override
  public String loadWith(VerificationCode code) {
    HashMap<String, String> emailValues = new HashMap<>();
    emailValues.put("emailVerificationCode", code.code());

    Context context = new Context();
    emailValues.forEach(context::setVariable);

    return templateEngine.process(TEMPLATE, context);
  }
}
