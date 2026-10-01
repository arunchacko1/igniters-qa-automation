package org.igniters.qa.tests.ui.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.support.TestDataClient;
import org.igniters.qa.tests.ui.BaseUiTest;
import org.igniters.qa.tests.ui.pages.EventDetailPage;
import org.igniters.qa.tests.ui.pages.EventFormPage;
import org.igniters.qa.tests.ui.pages.EventListPage;
import org.igniters.qa.tests.ui.pages.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Covers REQ-008, REQ-009, REQ-010 — see docs/test-cases.md TC-028..TC-039. */
class AdminEventCrudTest extends BaseUiTest {

    private String adminToken;
    private final List<Long> cleanupIds = new ArrayList<>();

    @BeforeEach
    void logInAsAdmin() {
        adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);
    }

    @AfterEach
    void deleteAnyCreatedEvents() {
        cleanupIds.forEach(id -> TestDataClient.deleteEvent(adminToken, id));
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-028: creating an event with valid data shows it on the event list immediately")
    void create_event_with_valid_data_appears_in_list() {
        String title = TestDataClient.uniqueTitle("New Admin Event");
        EventFormPage form = new EventFormPage(driver).openNew(baseUrl);
        form.fillForm(title, "A valid description.", LocalDate.now().plusDays(10), "New Hall", 15);
        form.submit();
        waitForUrlToContain("/events/");

        // A successful create redirects to /events/{id} — grab the id for cleanup.
        cleanupIds.add(idFromDetailUrl());
        assertTrue(new EventListPage(driver).open(baseUrl).getVisibleEventTitles().contains(title));
    }

    @ParameterizedTest(name = "a past date ({0} days ago) is rejected with a validation error")
    @Tag("regression")
    @DisplayName("TC-029: the create form rejects a past event date")
    @ValueSource(ints = {1, 30, 365})
    void create_with_past_date_is_rejected(int daysAgo) {
        EventFormPage form = new EventFormPage(driver).openNew(baseUrl);
        form.fillForm(
                TestDataClient.uniqueTitle("Should Not Be Created"),
                "desc",
                LocalDate.now().minusDays(daysAgo),
                "Hall",
                10);
        form.submit();

        assertTrue(form.hasGeneralError());
    }

    @ParameterizedTest(name = "leaving \"{0}\" blank shows its inline required-field error")
    @Tag("regression")
    @DisplayName("TC-030: each required field shows its own inline error when left blank")
    @CsvSource({
            "title, title-error",
            "description, description-error",
            "eventDate, date-error",
            "location, location-error",
            "capacity, capacity-error"
    })
    void create_with_blank_required_field_shows_inline_error(String blankField, String expectedErrorTestId) {
        EventFormPage form = new EventFormPage(driver).openNew(baseUrl);
        form.fillForm(
                blankField.equals("title") ? null : "Valid Title",
                blankField.equals("description") ? null : "Valid description.",
                blankField.equals("eventDate") ? null : LocalDate.now().plusDays(10),
                blankField.equals("location") ? null : "Valid Location",
                blankField.equals("capacity") ? null : 10);
        form.submit();

        assertTrue(form.hasErrorFor(expectedErrorTestId));
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-035: editing an event's title updates it on the detail page")
    void edit_event_updates_title() {
        long eventId = TestDataClient.createEvent(
                adminToken, TestDataClient.uniqueTitle("Before Edit"), LocalDate.now().plusDays(10), "Hall", 10);
        cleanupIds.add(eventId);
        String newTitle = TestDataClient.uniqueTitle("After Edit");

        EventFormPage form = new EventFormPage(driver).openEdit(baseUrl, eventId);
        form.fillForm(newTitle, "Updated description.", LocalDate.now().plusDays(11), "New Location", 12);
        form.submit();

        assertTrue(newTitle.equals(new EventDetailPage(driver).open(baseUrl, eventId).getTitle()));
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-037 (web): editing a nonexistent event shows a 404 page")
    void edit_nonexistent_event_shows_not_found() {
        driver.get(baseUrl + "/admin/events/999999999/edit");

        assertTrue(new EventDetailPage(driver).isNotFoundPage());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-039: deleting an event removes it from the event list immediately")
    void delete_event_removes_it_from_list() {
        String title = TestDataClient.uniqueTitle("To Be Deleted");
        long eventId = TestDataClient.createEvent(adminToken, title, LocalDate.now().plusDays(10), "Hall", 10);

        new EventDetailPage(driver).open(baseUrl, eventId).clickDelete();

        assertFalse(new EventListPage(driver).open(baseUrl).getVisibleEventTitles().contains(title));
    }

    /** Pulls the numeric id off the end of the current URL, e.g. ".../events/42" -> 42. */
    private long idFromDetailUrl() {
        String url = driver.getCurrentUrl();
        String idPart = url.substring(url.lastIndexOf('/') + 1);
        return Long.parseLong(idPart);
    }
}
