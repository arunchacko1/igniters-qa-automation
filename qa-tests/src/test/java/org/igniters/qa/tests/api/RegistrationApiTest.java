package org.igniters.qa.tests.api;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDate;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.Specs;
import org.igniters.qa.tests.support.TestDataClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Covers REQ-011, REQ-012, REQ-013 — see docs/test-cases.md TC-046..TC-054 (API subset). */
class RegistrationApiTest {

    private String adminToken;
    private String memberToken;
    private String secondMemberToken;
    private long eventId;

    @BeforeEach
    void setUp() {
        adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        memberToken = TestDataClient.login(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        secondMemberToken = TestDataClient.login(SeededUsers.SECOND_MEMBER_EMAIL, SeededUsers.PASSWORD);
        eventId = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("Registration Api Event"), LocalDate.now().plusDays(10), "Hall", 1);
    }

    @AfterEach
    void tearDown() {
        TestDataClient.deleteEvent(adminToken, eventId);
    }

    @Test
    @Tag("smoke")
    @DisplayName("A member can register for an open event and gets 201 matching the schema")
    void member_can_register_for_open_event() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/registration-schema.json"))
                .body("eventId", equalTo((int) eventId));
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-047: registering for a full event returns 409 EVENT_FULL")
    void registering_for_full_event_returns_409() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(201);

        given().spec(Specs.authenticatedRequest(secondMemberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(409)
                .body("code", equalTo("EVENT_FULL"));
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-048: registering twice for the same event returns 409 ALREADY_REGISTERED")
    void registering_twice_returns_409() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(201);

        given().spec(Specs.authenticatedRequest(memberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(409)
                .body("code", equalTo("ALREADY_REGISTERED"));
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-049: with capacity 1, a second different member is rejected while the first succeeds")
    void only_one_of_two_concurrent_members_can_take_the_last_seat() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(201);
        given().spec(Specs.authenticatedRequest(secondMemberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(409);
    }

    @Test
    @Tag("functional")
    @DisplayName("An admin cannot register for an event (registration is a MEMBER-only action)")
    void admin_cannot_register_for_event() {
        given().spec(Specs.authenticatedRequest(adminToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(403);
    }

    @Test
    @Tag("smoke")
    @DisplayName("Cancelling an existing registration returns 204")
    void cancel_registration_returns_204() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(201);

        given().spec(Specs.authenticatedRequest(memberToken))
                .when().delete("/events/{id}/registrations", eventId)
                .then().statusCode(204);
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-046: cancelling a registration that doesn't exist returns 404")
    void cancel_nonexistent_registration_returns_404() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().delete("/events/{id}/registrations", eventId)
                .then().statusCode(404)
                .body("code", equalTo("REGISTRATION_NOT_FOUND"));
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-050: GET /me/registrations returns only the current user's registrations")
    void my_registrations_returns_only_current_users() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(201);

        given().spec(Specs.authenticatedRequest(memberToken))
                .when().get("/me/registrations")
                .then().statusCode(200)
                .body("eventId", org.hamcrest.Matchers.hasItem((int) eventId));

        given().spec(Specs.authenticatedRequest(secondMemberToken))
                .when().get("/me/registrations")
                .then().statusCode(200)
                .body("eventId", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem((int) eventId)));
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-051: GET /me/registrations returns an empty array, not an error, for a member with none")
    void my_registrations_is_empty_array_when_none() {
        given().spec(Specs.authenticatedRequest(secondMemberToken))
                .when().get("/me/registrations")
                .then().statusCode(200)
                .body("findAll { it.eventId == " + eventId + " }", org.hamcrest.Matchers.empty());
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /events/{id}/registrations with no token returns 401")
    void register_without_token_returns_401() {
        given().spec(Specs.jsonRequest())
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(401);
    }
}
