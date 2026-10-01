package org.igniters.qa.tests.ui.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.TestDataClient;
import org.igniters.qa.tests.ui.BaseUiTest;
import org.igniters.qa.tests.ui.pages.EventDetailPage;
import org.igniters.qa.tests.ui.pages.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Covers REQ-007 — see docs/test-cases.md TC-024, TC-025. */
class EventDetailTest extends BaseUiTest {

    private String adminToken;
    private long eventId;
    private String eventTitle;

    @BeforeEach
    void createTestEvent() {
        adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        eventTitle = TestDataClient.uniqueTitle("Detail Page Event");
        eventId = TestDataClient.createEvent(adminToken, eventTitle, LocalDate.now().plusDays(14), "Detail Hall", 20);
    }

    @AfterEach
    void deleteTestEvent() {
        TestDataClient.deleteEvent(adminToken, eventId);
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-024: the detail page shows the event's own title and remaining capacity")
    void detail_page_shows_event_fields() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        EventDetailPage detailPage = new EventDetailPage(driver).open(baseUrl, eventId);

        assertEquals(eventTitle, detailPage.getTitle());
        assertTrue(detailPage.getRemainingSeatsText().contains("20"));
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-025: visiting a nonexistent event shows a 404 page, not an error stack trace")
    void nonexistent_event_shows_not_found_page() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);

        assertTrue(new EventDetailPage(driver).open(baseUrl, 999_999_999L).isNotFoundPage());
    }

    @Test
    @Tag("functional")
    @DisplayName("An admin sees Edit/Delete controls on the detail page; a member does not")
    void only_admin_sees_edit_and_delete_controls() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        assertFalse(new EventDetailPage(driver).open(baseUrl, eventId).isEditLinkVisible());

        logout();
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        assertTrue(new EventDetailPage(driver).open(baseUrl, eventId).isEditLinkVisible());
    }
}
