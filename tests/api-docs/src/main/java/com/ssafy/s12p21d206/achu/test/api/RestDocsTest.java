package com.ssafy.s12p21d206.achu.test.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@Tag("restdocs")
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTest {

  protected MockMvcRequestSpecification mockMvc;

  private RestDocumentationContextProvider restDocumentation;

  @BeforeEach
  public void setUp(RestDocumentationContextProvider restDocumentation) {
    this.restDocumentation = restDocumentation;
  }

  protected MockMvcRequestSpecification given() {
    return mockMvc;
  }

  protected MockMvcRequestSpecification mockController(Object controller) {
    MockMvc mockMvc = createMockMvc(controller);
    return RestAssuredMockMvc.given().mockMvc(mockMvc);
  }

  private MockMvc createMockMvc(Object controller) {
    MappingJackson2HttpMessageConverter converter =
        new MappingJackson2HttpMessageConverter(objectMapper());

    // Request Preprocessors
    OperationPreprocessor requestUriModifier = Preprocessors.modifyUris()
        .scheme("https")
        .host("api.dev.achu.dukcode.org")
        .removePort();

    OperationPreprocessor requestHeaderModifier = Preprocessors.modifyHeaders()
        .remove("Content-Length")
        .remove("Content-Type")
        .remove("Host");

    OperationPreprocessor requestPrettyPrinter = Preprocessors.prettyPrint();

    OperationPreprocessor responseHeaderModifier = Preprocessors.modifyHeaders()
        .remove("Content-Length")
        .remove("Content-Type")
        .remove("X-Content-Type-Options")
        .remove("X-XSS-Protection")
        .remove("Cache-Control")
        .remove("Pragma")
        .remove("Expires")
        .remove("X-Frame-Options")
        .remove("Vary");

    OperationPreprocessor responsePrettyPrinter = Preprocessors.prettyPrint();

    return MockMvcBuilders.standaloneSetup(controller)
        .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
            .operationPreprocessors()
            .withRequestDefaults(requestUriModifier, requestHeaderModifier, requestPrettyPrinter)
            .withResponseDefaults(responseHeaderModifier, responsePrettyPrinter))
        .setMessageConverters(converter)
        .build();
  }

  private ObjectMapper objectMapper() {
    return Jackson2ObjectMapperBuilder.json()
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .featuresToDisable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
        .build();
  }
}
