package org.igniters.qa.tests.ui.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.TestDataClient;
import org.igniters.qa.tests.ui.BaseUiTest;
import org.igniters.qa.tests.ui.pages.EventListPage;
import org.igniters.qa.tests.ui.pages.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Covers REQ-004 — see docs/test-cases.md TC-014, TC-021. */
class EventListTest extends BaseUiTest {

    private String adminToken;
    private long eventId;
    private String eventTitle;

    @BeforeEach
    void createTestEvent() {
        adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        eventTitle = TestDataClient.uniqueTitle("List Visibility Event");
        eventId = TestDataClient.createEvent(adminToken, eventTitle, LocalDate.now().plusDays(5), "Test Hall", 10);
    }

    @AfterEach
    void deleteTestEvent() {
        TestDataClient.deleteEvent(adminToken, eventId);
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-014: a newly created event appears in the event list")
    void event_list_shows_newly_created_event() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);

        assertTrue(new EventListPage(driver).open(baseUrl).getVisibleEventTitles().contains(eventTitle));
    }

    @Test
    @Tag("functional")
    @DisplayName("A fresh event with capacity 10 and no registrations shows 10 seats left")
    void event_list_shows_full_remaining_capacity_for_new_event() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        new EventListPage(driver).open(baseUrl).clickEventTitled(eventTitle);

        assertTrue(driver.findElement(org.openqa.selenium.By.cssSelector("[data-testid='event-detail-remaining']"))
                .getText().contains("10"));
    }
}
