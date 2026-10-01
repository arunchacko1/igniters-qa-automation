package org.igniters.qa.tests.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
 * registrations.event_id has no DB-level foreign key (see docs/decisions.md
 * #9), so this LEFT JOIN is the real referential-integrity check — nothing
 * else stops an orphan row from existing. Covers REQ-010 / seeded defect #3.
 */
@Tag("db")
class OrphanRegistrationTest {

    private static final String FIND_ORPHANS_SQL =
            "SELECT r.id FROM registrations r LEFT JOIN events e ON r.event_id = e.id WHERE e.id IS NULL AND r.id = ?";

    @Test
    @DisplayName("TC-041: deleting an event via the API (defects disabled) leaves no orphan registration")
    void delete_event_leaves_no_orphans() throws Exception {
        String adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        String memberToken = TestDataClient.login(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        long eventId = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("Orphan Check Event"), LocalDate.now().plusDays(15), "Hall", 5);
        TestDataClient.registerForEvent(memberToken, eventId);
        long registrationId = registrationIdFor(memberToken, eventId);

        TestDataClient.deleteEvent(adminToken, eventId);

        assertEquals(0, countOrphans(registrationId), "The cleanup in EventService#deleteEvent should have removed it");
    }

    @Test
    @DisplayName("The orphan-finding LEFT JOIN correctly flags a row inserted without its event (sanity check on the query itself)")
    void left_join_detects_a_manually_created_orphan() throws Exception {
        String adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        long eventId = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("Orphan Source Event"), LocalDate.now().plusDays(15), "Hall", 5);
        long memberId = userIdFor(SeededUsers.SECOND_MEMBER_EMAIL);

        long registrationId;
        try (Connection conn = DbSupport.getConnection()) {
            // Bypass the API entirely: delete the event directly, leaving a
            // registration row whose event_id now points at nothing — this
            // proves the LEFT JOIN query itself is correct, independent of
            // whether the application code remembers to clean up.
            registrationId = insertRegistrationDirect(conn, memberId, eventId);
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM events WHERE id = ?")) {
                del.setLong(1, eventId);
                del.executeUpdate();
            }
        }

        assertEquals(1, countOrphans(registrationId));

        // Clean up the orphan ourselves — nothing else will.
        try (Connection conn = DbSupport.getConnection();
                PreparedStatement del = conn.prepareStatement("DELETE FROM registrations WHERE id = ?")) {
            del.setLong(1, registrationId);
            del.executeUpdate();
        }
    }

    private int countOrphans(long registrationId) throws Exception {
        try (Connection conn = DbSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement(FIND_ORPHANS_SQL)) {
            stmt.setLong(1, registrationId);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                return count;
            }
        }
    }

    private long registrationIdFor(String memberToken, long eventId) {
        return io.restassured.RestAssured.given()
                .spec(org.igniters.qa.tests.support.Specs.authenticatedRequest(memberToken))
                .when().get("/me/registrations")
                .then().extract().jsonPath().getLong("find { it.eventId == " + eventId + " }.id");
    }

    private long userIdFor(String email) throws Exception {
        try (Connection conn = DbSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        }
    }

    private long insertRegistrationDirect(Connection conn, long userId, long eventId) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO registrations (user_id, event_id) VALUES (?, ?) RETURNING id")) {
            stmt.setLong(1, userId);
            stmt.setLong(2, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        }
    }
}
