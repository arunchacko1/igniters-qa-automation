package org.igniters.qa.tests.support;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Creates and tears down test data through the real API — not direct SQL
 * inserts — so UI and DB tests exercise the same code paths a real user
 * would, and each test stays independent (own event, own cleanup).
 */
public final class TestDataClient {

    private TestDataClient() {
    }

    public static String login(String email, String password) {
        return given().spec(Specs.jsonRequest())
                .body(Map.of("email", email, "password", password))
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");
    }

    /** A title no other test will collide with, even when suites run in parallel. */
    public static String uniqueTitle(String prefix) {
        return prefix + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    public static long createEvent(String adminToken, String title, LocalDate date, String location, int capacity) {
        return given().spec(Specs.authenticatedRequest(adminToken))
                .body(Map.of(
                        "title", title,
                        "description", "Created by automated tests.",
                        "eventDate", date.toString(),
                        "location", location,
                        "capacity", capacity))
                .when().post("/events")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public static void deleteEvent(String adminToken, long eventId) {
        // Best-effort cleanup — a test that already deleted the event itself
        // shouldn't fail teardown for every other test.
        given().spec(Specs.authenticatedRequest(adminToken)).when().delete("/events/{id}", eventId);
    }

    public static void registerForEvent(String memberToken, long eventId) {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().post("/events/{id}/registrations", eventId)
                .then().statusCode(201);
    }
}
