package org.igniters.qa.tests.support;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

/** Shared REST Assured response specs — one place to assert "this is a well-formed error body". */
public final class ResponseSpecs {

    private ResponseSpecs() {
    }

    public static ResponseSpecification errorWithCode(int status, String errorCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .expectBody("code", equalTo(errorCode))
                .build();
    }
}
