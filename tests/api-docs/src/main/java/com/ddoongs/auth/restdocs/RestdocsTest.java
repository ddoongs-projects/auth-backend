package com.ddoongs.auth.restdocs;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.context.annotation.Import;

@Tag("restdocs")
@Import(RestdocsConfiguration.class)
@AutoConfigureRestDocs
public abstract class RestdocsTest {}
