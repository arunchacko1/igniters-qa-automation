package org.igniters.qa.tests.api;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.Specs;
import org.igniters.qa.tests.support.TestDataClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Covers REQ-004..REQ-010 — see docs/test-cases.md TC-021..TC-041 (API subset). */
class EventsApiTest {

    private String adminToken;
    private String memberToken;
    private long eventId;
    private String eventTitle;

    @BeforeEach
    void setUp() {
        adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        memberToken = TestDataClient.login(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        eventTitle = TestDataClient.uniqueTitle("Api Fixture Event");
        eventId = TestDataClient.createEvent(adminToken, eventTitle, LocalDate.now().plusDays(10), "Api Hall", 5);
    }

    @AfterEach
    void tearDown() {
        TestDataClient.deleteEvent(adminToken, eventId);
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-021: GET /events returns 200 and a list matching the Event JSON schema")
    void list_events_returns_200_matching_schema() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().get("/events")
                .then().statusCode(200)
                .contentType("application/json")
                .body(matchesJsonSchemaInClasspath("schemas/event-list-schema.json"));
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-022: GET /events?q= returns only titles containing the query")
    void list_events_filtered_by_title() {
        List<String> titles = given().spec(Specs.authenticatedRequest(memberToken))
                .queryParam("q", eventTitle)
                .when().get("/events")
                .then().statusCode(200)
                .extract().jsonPath().getList("title", String.class);

        titles.forEach(title -> org.junit.jupiter.api.Assertions.assertTrue(title.contains(eventTitle)));
        org.junit.jupiter.api.Assertions.assertTrue(titles.contains(eventTitle));
    }

    @Test
    @Tag("regression")
    @DisplayName("Search is case-insensitive when QA_DEFECTS_ENABLED is false")
    void list_events_search_is_case_insensitive() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .queryParam("q", eventTitle.toUpperCase())
                .when().get("/events")
                .then().statusCode(200)
                .body("title", org.hamcrest.Matchers.hasItem(eventTitle));
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-023: GET /events?date= returns only events on that day")
    void list_events_filtered_by_date() {
        LocalDate date = LocalDate.now().plusDays(10);
        given().spec(Specs.authenticatedRequest(memberToken))
                .queryParam("date", date.toString())
                .when().get("/events")
                .then().statusCode(200)
                .body("eventDate", org.hamcrest.Matchers.everyItem(equalTo(date.toString())));
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-026: GET /events/{id} returns the event matching the schema")
    void get_event_by_id_returns_matching_event() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().get("/events/{id}", eventId)
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/event-schema.json"))
                .body("id", equalTo((int) eventId))
                .body("title", equalTo(eventTitle))
                .body("remainingCapacity", equalTo(5));
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-027: GET /events/{id} for a nonexistent id returns 404")
    void get_nonexistent_event_returns_404() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().get("/events/999999999")
                .then().statusCode(404)
                .body("code", equalTo("EVENT_NOT_FOUND"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-031: POST /events as admin returns 201, a Location header, and the new id")
    void create_event_returns_201_with_location() {
        String title = TestDataClient.uniqueTitle("Created Via Api");
        long createdId = given().spec(Specs.authenticatedRequest(adminToken))
                .body(Map.of(
                        "title", title,
                        "description", "d",
                        "eventDate", LocalDate.now().plusDays(5).toString(),
                        "location", "Hall",
                        "capacity", 10))
                .when().post("/events")
                .then().statusCode(201)
                .header("Location", notNullValue())
                .body("id", greaterThan(0))
                .extract().jsonPath().getLong("id");

        TestDataClient.deleteEvent(adminToken, createdId);
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-032: POST /events with a past date returns 400")
    void create_event_with_past_date_returns_400() {
        given().spec(Specs.authenticatedRequest(adminToken))
                .body(Map.of(
                        "title", "x", "description", "d", "eventDate", "2000-01-01", "location", "Hall", "capacity", 10))
                .when().post("/events")
                .then().statusCode(400)
                .body("code", equalTo("EVENT_DATE_IN_PAST"));
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /events with a blank title returns 400 validation error")
    void create_event_with_blank_title_returns_400() {
        given().spec(Specs.authenticatedRequest(adminToken))
                .body(Map.of(
                        "title", "",
                        "description", "d",
                        "eventDate", LocalDate.now().plusDays(5).toString(),
                        "location", "Hall",
                        "capacity", 10))
                .when().post("/events")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-033: POST /events with capacity 0 returns 400")
    void create_event_with_zero_capacity_returns_400() {
        given().spec(Specs.authenticatedRequest(adminToken))
                .body(Map.of(
                        "title", "x",
                        "description", "d",
                        "eventDate", LocalDate.now().plusDays(5).toString(),
                        "location", "Hall",
                        "capacity", 0))
                .when().post("/events")
                .then().statusCode(400);
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-011: POST /events as a member returns 403")
    void create_event_as_member_returns_403() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .body(Map.of(
                        "title", "x",
                        "description", "d",
                        "eventDate", LocalDate.now().plusDays(5).toString(),
                        "location", "Hall",
                        "capacity", 10))
                .when().post("/events")
                .then().statusCode(403);
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /events with no token returns 401")
    void create_event_without_token_returns_401() {
        given().spec(Specs.jsonRequest())
                .body(Map.of(
                        "title", "x",
                        "description", "d",
                        "eventDate", LocalDate.now().plusDays(5).toString(),
                        "location", "Hall",
                        "capacity", 10))
                .when().post("/events")
                .then().statusCode(401);
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-036: PUT /events/{id} as admin updates and returns the new fields")
    void update_event_returns_200_with_new_fields() {
        String newTitle = TestDataClient.uniqueTitle("Updated Title");
        given().spec(Specs.authenticatedRequest(adminToken))
                .body(Map.of(
                        "title", newTitle,
                        "description", "updated",
                        "eventDate", LocalDate.now().plusDays(20).toString(),
                        "location", "New Hall",
                        "capacity", 8))
                .when().put("/events/{id}", eventId)
                .then().statusCode(200)
                .body("title", equalTo(newTitle))
                .body("capacity", equalTo(8));
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-037: PUT /events/{id} for a nonexistent id returns 404 when defects are disabled")
    void update_nonexistent_event_returns_404() {
        given().spec(Specs.authenticatedRequest(adminToken))
                .body(Map.of(
                        "title", "x",
                        "description", "d",
                        "eventDate", LocalDate.now().plusDays(5).toString(),
                        "location", "Hall",
                        "capacity", 10))
                .when().put("/events/999999999")
                .then().statusCode(404);
    }

    @Test
    @Tag("regression")
    @DisplayName("PUT /events/{id} as a member returns 403")
    void update_event_as_member_returns_403() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .body(Map.of(
                        "title", "x",
                        "description", "d",
                        "eventDate", LocalDate.now().plusDays(5).toString(),
                        "location", "Hall",
                        "capacity", 10))
                .when().put("/events/{id}", eventId)
                .then().statusCode(403);
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-040: DELETE /events/{id} as admin returns 204")
    void delete_event_returns_204() {
        long idToDelete = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("To Delete"), LocalDate.now().plusDays(5), "Hall", 5);

        given().spec(Specs.authenticatedRequest(adminToken))
                .when().delete("/events/{id}", idToDelete)
                .then().statusCode(204);

        given().spec(Specs.authenticatedRequest(adminToken))
                .when().get("/events/{id}", idToDelete)
                .then().statusCode(404);
    }

    @Test
    @Tag("regression")
    @DisplayName("DELETE /events/{id} as a member returns 403")
    void delete_event_as_member_returns_403() {
        given().spec(Specs.authenticatedRequest(memberToken))
                .when().delete("/events/{id}", eventId)
                .then().statusCode(403);
    }

    @Test
    @Tag("regression")
    @DisplayName("DELETE /events/{id} for a nonexistent id returns 404")
    void delete_nonexistent_event_returns_404() {
        given().spec(Specs.authenticatedRequest(adminToken))
                .when().delete("/events/999999999")
                .then().statusCode(404);
    }
}
