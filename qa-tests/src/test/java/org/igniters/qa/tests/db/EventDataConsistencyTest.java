package org.igniters.qa.tests.db;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.response.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import org.igniters.qa.tests.support.DbSupport;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.Specs;
import org.igniters.qa.tests.support.TestDataClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Covers TC-026 (integration): the API's view of an event must agree with the row behind it. */
@Tag("db")
class EventDataConsistencyTest {

    @Test
    @DisplayName("GET /api/events/{id} returns exactly what the events table holds for that row")
    void api_response_matches_database_row() throws Exception {
        String adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        String title = TestDataClient.uniqueTitle("Consistency Check Event");
        LocalDate date = LocalDate.now().plusDays(12);
        long eventId = TestDataClient.createEvent(adminToken, title, date, "Consistency Hall", 11);

        try {
            Response apiResponse = given().spec(Specs.authenticatedRequest(adminToken))
                    .when().get("/events/{id}", eventId)
                    .then().statusCode(200)
                    .extract().response();

            try (Connection conn = DbSupport.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT title, location, capacity, event_date FROM events WHERE id = ?")) {
                stmt.setLong(1, eventId);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertEquals(true, rs.next());
                    assertEquals(rs.getString("title"), apiResponse.jsonPath().getString("title"));
                    assertEquals(rs.getString("location"), apiResponse.jsonPath().getString("location"));
                    assertEquals(rs.getInt("capacity"), apiResponse.jsonPath().getInt("capacity"));
                    assertEquals(
                            rs.getObject("event_date", LocalDate.class).toString(),
                            apiResponse.jsonPath().getString("eventDate"));
                }
            }
        } finally {
            TestDataClient.deleteEvent(adminToken, eventId);
        }
    }
}
