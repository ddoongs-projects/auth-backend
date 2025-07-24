package com.ddoongs.auth.restdocs;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

@Tag("restdocs")
@Import(RestdocsConfiguration.class)
@ImportAutoConfiguration(
    exclude = {
      SecurityAutoConfiguration.class,
      OAuth2ClientAutoConfiguration.class,
    })
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public abstract class RestdocsTest {}
