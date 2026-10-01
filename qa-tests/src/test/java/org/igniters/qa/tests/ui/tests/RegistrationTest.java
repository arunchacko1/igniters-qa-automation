package org.igniters.qa.tests.ui.tests;

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

/** Covers REQ-011, REQ-012 — see docs/test-cases.md TC-042..TC-045. */
class RegistrationTest extends BaseUiTest {

    private String adminToken;
    private long eventId;

    @BeforeEach
    void createOpenEvent() {
        adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        eventId = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("Registration Flow Event"), LocalDate.now().plusDays(10), "Hall", 1);
    }

    @AfterEach
    void deleteEvent() {
        TestDataClient.deleteEvent(adminToken, eventId);
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-042: registering decreases the remaining-seats count by one")
    void registering_decreases_remaining_seats() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.SECOND_MEMBER_EMAIL, SeededUsers.PASSWORD);
        EventDetailPage detail = new EventDetailPage(driver).open(baseUrl, eventId);

        detail.clickRegister();

        detail = new EventDetailPage(driver).open(baseUrl, eventId);
        assertTrue(detail.getRemainingSeatsText().contains("0"));
        assertTrue(detail.isCancelButtonVisible());
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-043: registering for a full event shows an error and doesn't create a row")
    void registering_for_full_event_shows_error() {
        // Capacity is 1; fill it with a different member first.
        String otherMemberToken = TestDataClient.login(SeededUsers.THIRD_MEMBER_EMAIL, SeededUsers.PASSWORD);
        TestDataClient.registerForEvent(otherMemberToken, eventId);

        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.SECOND_MEMBER_EMAIL, SeededUsers.PASSWORD);
        EventDetailPage detail = new EventDetailPage(driver).open(baseUrl, eventId);
        detail.clickRegister();

        // The error is a one-shot flash attribute shown on the page the
        // register redirect lands on — read it there, don't navigate again
        // (a second GET would have already consumed/cleared it).
        assertTrue(detail.isErrorMessageDisplayed());
        assertTrue(detail.isRegisterButtonVisible());
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-044: once registered, the page offers Cancel instead of a second Register")
    void registering_twice_is_not_offered_by_the_ui() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.SECOND_MEMBER_EMAIL, SeededUsers.PASSWORD);
        EventDetailPage detail = new EventDetailPage(driver).open(baseUrl, eventId);
        detail.clickRegister();

        // The UI's own duplicate-prevention is that the Register button is
        // replaced by Cancel — there's no button left to click a second time.
        // The server-side rule (ALREADY_REGISTERED, 409) is covered directly
        // by RegistrationApiTest; re-asserting it here would just duplicate that.
        detail = new EventDetailPage(driver).open(baseUrl, eventId);
        assertTrue(detail.isCancelButtonVisible());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-045: cancelling increases the remaining-seats count by one")
    void cancelling_increases_remaining_seats() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.SECOND_MEMBER_EMAIL, SeededUsers.PASSWORD);
        EventDetailPage detail = new EventDetailPage(driver).open(baseUrl, eventId);
        detail.clickRegister();

        detail = new EventDetailPage(driver).open(baseUrl, eventId);
        detail.clickCancelRegistration();

        detail = new EventDetailPage(driver).open(baseUrl, eventId);
        assertTrue(detail.getRemainingSeatsText().contains("1"));
        assertTrue(detail.isRegisterButtonVisible());
    }
}
