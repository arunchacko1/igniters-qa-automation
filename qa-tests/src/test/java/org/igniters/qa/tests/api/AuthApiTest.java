package org.igniters.qa.tests.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.Specs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Covers REQ-001, REQ-002 — see docs/test-cases.md TC-007, TC-008, TC-012, TC-013. */
class AuthApiTest {

    @Test
    @Tag("smoke")
    @DisplayName("TC-007: valid admin credentials return a token and the ADMIN role")
    void login_with_valid_admin_credentials_returns_token_and_role() {
        given().spec(Specs.jsonRequest())
                .body(Map.of("email", SeededUsers.ADMIN_EMAIL, "password", SeededUsers.PASSWORD))
                .when().post("/auth/login")
                .then().statusCode(200)
                .body("token", notNullValue())
                .body("role", equalTo("ADMIN"));
    }

    @Test
    @Tag("functional")
    @DisplayName("Valid member credentials return the MEMBER role")
    void login_with_valid_member_credentials_returns_member_role() {
        given().spec(Specs.jsonRequest())
                .body(Map.of("email", SeededUsers.MEMBER_EMAIL, "password", SeededUsers.PASSWORD))
                .when().post("/auth/login")
                .then().statusCode(200)
                .body("role", equalTo("MEMBER"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-008: wrong password returns 401 with a JSON error body")
    void login_with_wrong_password_returns_401() {
        given().spec(Specs.jsonRequest())
                .body(Map.of("email", SeededUsers.ADMIN_EMAIL, "password", "wrong"))
                .when().post("/auth/login")
                .then().statusCode(401)
                .body("code", equalTo("INVALID_CREDENTIALS"))
                .body("message", notNullValue());
    }

    @Test
    @Tag("regression")
    @DisplayName("An unknown email returns the same 401 error as a wrong password")
    void login_with_unknown_email_returns_401() {
        given().spec(Specs.jsonRequest())
                .body(Map.of("email", "nobody@igniters.org", "password", "whatever"))
                .when().post("/auth/login")
                .then().statusCode(401)
                .body("code", equalTo("INVALID_CREDENTIALS"));
    }

    @Test
    @Tag("regression")
    @DisplayName("A blank email is rejected by validation before hitting the database")
    void login_with_blank_email_returns_400() {
        given().spec(Specs.jsonRequest())
                .body(Map.of("email", "", "password", "whatever"))
                .when().post("/auth/login")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-012: a protected endpoint with no Authorization header returns 401")
    void protected_endpoint_without_token_returns_401() {
        given().spec(Specs.jsonRequest())
                .when().get("/me/registrations")
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHORIZED"));
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-013: a protected endpoint with a garbage token returns 401")
    void protected_endpoint_with_garbage_token_returns_401() {
        given().spec(Specs.authenticatedRequest("not-a-real-token"))
                .when().get("/me/registrations")
                .then().statusCode(401);
    }
}
