package org.igniters.qa.tests.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import org.igniters.qa.tests.support.DbSupport;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.TestDataClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Plain JDBC + raw SQL on purpose — no JPA/Hibernate — so these tests see
 * exactly what's on disk, independent of whatever the application layer
 * claims happened. Covers REQ-008, REQ-009, REQ-010 at the data layer.
 */
@Tag("db")
class EventCrudDbTest {

    @Test
    @DisplayName("TC-034: creating an event via the API inserts exactly one matching row")
    void create_event_via_api_creates_matching_row() throws Exception {
        String adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        String title = TestDataClient.uniqueTitle("DB CRUD Event");
        LocalDate date = LocalDate.now().plusDays(15);
        long eventId = TestDataClient.createEvent(adminToken, title, date, "DB Hall", 7);

        try (Connection conn = DbSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT title, location, capacity, event_date FROM events WHERE id = ?")) {
            stmt.setLong(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Expected exactly one row for the created event");
                assertEquals(title, rs.getString("title"));
                assertEquals("DB Hall", rs.getString("location"));
                assertEquals(7, rs.getInt("capacity"));
                assertEquals(date, rs.getObject("event_date", LocalDate.class));
                assertFalse(rs.next(), "Expected no second row");
            }
        } finally {
            TestDataClient.deleteEvent(adminToken, eventId);
        }
    }

    @Test
    @DisplayName("TC-038: editing an event's capacity via the API updates the row without touching registrations")
    void update_event_preserves_registrations() throws Exception {
        String adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        String memberToken = TestDataClient.login(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        long eventId = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("DB Update Event"), LocalDate.now().plusDays(15), "Hall", 5);
        TestDataClient.registerForEvent(memberToken, eventId);

        io.restassured.RestAssured.given()
                .spec(org.igniters.qa.tests.support.Specs.authenticatedRequest(adminToken))
                .body(java.util.Map.of(
                        "title", "Updated Title",
                        "description", "Updated",
                        "eventDate", LocalDate.now().plusDays(20).toString(),
                        "location", "New Hall",
                        "capacity", 9))
                .when().put("/events/{id}", eventId)
                .then().statusCode(200);

        try (Connection conn = DbSupport.getConnection()) {
            try (PreparedStatement eventStmt = conn.prepareStatement("SELECT capacity FROM events WHERE id = ?")) {
                eventStmt.setLong(1, eventId);
                try (ResultSet rs = eventStmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(9, rs.getInt("capacity"));
                }
            }
            try (PreparedStatement regStmt =
                    conn.prepareStatement("SELECT count(*) FROM registrations WHERE event_id = ?")) {
                regStmt.setLong(1, eventId);
                try (ResultSet rs = regStmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt(1), "The registration made before the edit should still be there");
                }
            }
        } finally {
            TestDataClient.deleteEvent(adminToken, eventId);
        }
    }

    @Test
    @DisplayName("TC-041 (baseline): deleting an event via the API removes its row entirely")
    void delete_event_removes_row() throws Exception {
        String adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        long eventId = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("DB Delete Event"), LocalDate.now().plusDays(15), "Hall", 5);

        TestDataClient.deleteEvent(adminToken, eventId);

        try (Connection conn = DbSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM events WHERE id = ?")) {
            stmt.setLong(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertFalse(rs.next(), "Event row should be gone after delete");
            }
        }
    }
}
