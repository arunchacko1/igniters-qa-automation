package org.igniters.qa.tests.support;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/** Shared REST Assured request specs so every API test isn't rebuilding baseUri/headers by hand. */
public final class Specs {

    private Specs() {
    }

    public static RequestSpecification jsonRequest() {
        return new RequestSpecBuilder()
                .setBaseUri(TestConfig.apiBaseUrl())
                .setContentType(ContentType.JSON)
                .build();
    }

    public static RequestSpecification authenticatedRequest(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(jsonRequest())
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }
}
