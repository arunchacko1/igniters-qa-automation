package org.igniters.qa.tests.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.igniters.qa.tests.support.DbSupport;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.TestDataClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Covers REQ-011, REQ-012 at the data layer — see docs/test-cases.md TC-052..TC-054. */
@Tag("db")
class RegistrationDbTest {

    private String adminToken;
    private long eventId;
    private long memberId;

    @BeforeEach
    void createEventAndResolveMemberId() throws Exception {
        adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        eventId = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("DB Registration Event"), LocalDate.now().plusDays(15), "Hall", 3);

        try (Connection conn = DbSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            stmt.setString(1, SeededUsers.MEMBER_EMAIL);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                memberId = rs.getLong("id");
            }
        }
    }

    @AfterEach
    void deleteEvent() {
        TestDataClient.deleteEvent(adminToken, eventId);
    }

    @Test
    @DisplayName("TC-052: registering via the API inserts a matching registrations row")
    void register_via_api_creates_row() throws Exception {
        String memberToken = TestDataClient.login(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        TestDataClient.registerForEvent(memberToken, eventId);

        try (Connection conn = DbSupport.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement("SELECT 1 FROM registrations WHERE user_id = ? AND event_id = ?")) {
            stmt.setLong(1, memberId);
            stmt.setLong(2, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a registrations row for this member and event");
            }
        }
    }

    @Test
    @DisplayName("Remaining capacity (capacity - registration count) matches what the DB actually holds")
    void remaining_capacity_matches_registration_count() throws Exception {
        String memberToken = TestDataClient.login(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        TestDataClient.registerForEvent(memberToken, eventId);

        try (Connection conn = DbSupport.getConnection()) {
            int capacity = queryInt(conn, "SELECT capacity FROM events WHERE id = ?", eventId);
            int registrationCount = queryInt(conn, "SELECT count(*) FROM registrations WHERE event_id = ?", eventId);
            assertEquals(2, capacity - registrationCount, "3 capacity - 1 registration should leave 2 seats");
        }
    }

    @Test
    @DisplayName("TC-053: cancelling via the API deletes the registrations row")
    void cancel_via_api_removes_row() throws Exception {
        String memberToken = TestDataClient.login(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        TestDataClient.registerForEvent(memberToken, eventId);

        io.restassured.RestAssured.given()
                .spec(org.igniters.qa.tests.support.Specs.authenticatedRequest(memberToken))
                .when().delete("/events/{id}/registrations", eventId)
                .then().statusCode(204);

        try (Connection conn = DbSupport.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement("SELECT 1 FROM registrations WHERE user_id = ? AND event_id = ?")) {
            stmt.setLong(1, memberId);
            stmt.setLong(2, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertFalse(rs.next(), "Registration row should be gone after cancel");
            }
        }
    }

    @Test
    @DisplayName("TC-054: the unique constraint on (user_id, event_id) blocks a direct duplicate insert")
    void unique_constraint_blocks_duplicate_registration() throws Exception {
        try (Connection conn = DbSupport.getConnection()) {
            insertRegistration(conn, memberId, eventId);

            SQLException duplicateInsertError =
                    org.junit.jupiter.api.Assertions.assertThrows(
                            SQLException.class, () -> insertRegistration(conn, memberId, eventId));
            // Postgres' unique_violation SQLSTATE — confirms it's the constraint
            // doing the rejecting, not some unrelated failure.
            assertEquals("23505", duplicateInsertError.getSQLState());
        }
    }

    private void insertRegistration(Connection conn, long userId, long eventId) throws SQLException {
        try (PreparedStatement stmt =
                conn.prepareStatement("INSERT INTO registrations (user_id, event_id) VALUES (?, ?)")) {
            stmt.setLong(1, userId);
            stmt.setLong(2, eventId);
            stmt.executeUpdate();
        }
    }

    private int queryInt(Connection conn, String sql, long param) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
