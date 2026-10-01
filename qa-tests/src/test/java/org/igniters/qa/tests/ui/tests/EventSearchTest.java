package org.igniters.qa.tests.ui.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Covers REQ-005, REQ-006 — see docs/test-cases.md TC-015..TC-020. */
class EventSearchTest extends BaseUiTest {

    private String adminToken;
    private final List<Long> createdEventIds = new ArrayList<>();
    private String kickoffTitle;
    private LocalDate datedEventDate;
    private String datedEventTitle;

    @BeforeEach
    void createFixtureEventsAndLogIn() {
        adminToken = TestDataClient.login(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);

        kickoffTitle = TestDataClient.uniqueTitle("Summer Camp Kickoff");
        String driveTitle = TestDataClient.uniqueTitle("Food Drive Service Day");
        String ridgeTitle = TestDataClient.uniqueTitle("Hiking Trip Eagle Ridge");
        String fundraiserTitle = TestDataClient.uniqueTitle("Car Wash Fundraiser");
        String cleanupTitle = TestDataClient.uniqueTitle("Beach Cleanup Day");
        datedEventDate = LocalDate.now().plusDays(30);
        datedEventTitle = TestDataClient.uniqueTitle("Dated Fixture Event");

        createdEventIds.add(TestDataClient.createEvent(adminToken, kickoffTitle, LocalDate.now().plusDays(5), "Hall", 10));
        createdEventIds.add(TestDataClient.createEvent(adminToken, driveTitle, LocalDate.now().plusDays(6), "Hall", 10));
        createdEventIds.add(TestDataClient.createEvent(adminToken, ridgeTitle, LocalDate.now().plusDays(7), "Hall", 10));
        createdEventIds.add(TestDataClient.createEvent(adminToken, fundraiserTitle, LocalDate.now().plusDays(8), "Hall", 10));
        createdEventIds.add(TestDataClient.createEvent(adminToken, cleanupTitle, LocalDate.now().plusDays(9), "Hall", 10));
        createdEventIds.add(TestDataClient.createEvent(adminToken, datedEventTitle, datedEventDate, "Hall", 10));

        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
    }

    @AfterEach
    void deleteFixtureEvents() {
        createdEventIds.forEach(id -> TestDataClient.deleteEvent(adminToken, id));
    }

    // The second column is the keyword exactly as it appears (case-wise) in
    // the fixture title created in @BeforeEach — e.g. "Summer Camp Kickoff ...".
    // Matching on that substring, rather than the whole random-suffixed
    // title, keeps this test readable without extra placeholder-resolution code.
    @ParameterizedTest(name = "searching \"{0}\" still finds the event titled with \"{1}\"")
    @Tag("regression")
    @DisplayName("TC-016/TC-020: search is case-insensitive")
    @CsvSource({
            "kickoff, Kickoff",
            "KICKOFF, Kickoff",
            "drive, Drive",
            "Ridge, Ridge",
            "fundraiser, Fundraiser",
            "CLEANUP, Cleanup"
    })
    void search_is_case_insensitive(String query, String titleKeyword) {
        EventListPage listPage = new EventListPage(driver).open(baseUrl);
        listPage.searchByTitle(query);

        boolean found = listPage.getVisibleEventTitles().stream().anyMatch(title -> title.contains(titleKeyword));
        assertTrue(found, "Expected a result containing \"" + titleKeyword + "\" when searching \"" + query + "\"");
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-017: searching for nonsense shows the empty state, not an error")
    void search_with_no_matches_shows_empty_state() {
        EventListPage listPage = new EventListPage(driver).open(baseUrl);
        listPage.searchByTitle("zzz-no-such-event-zzz");

        assertTrue(listPage.isEmptyStateShown());
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-018: filtering by a date with events shows only that day's events")
    void filter_by_date_shows_matching_events() {
        EventListPage listPage = new EventListPage(driver).open(baseUrl);
        listPage.filterByDate(datedEventDate);

        assertTrue(listPage.getVisibleEventTitles().contains(datedEventTitle));
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-019: filtering by a date with no events shows the empty state")
    void filter_by_date_with_no_events_shows_empty_state() {
        EventListPage listPage = new EventListPage(driver).open(baseUrl);
        listPage.filterByDate(LocalDate.now().plusYears(5));

        assertTrue(listPage.isEmptyStateShown());
    }

    @Test
    @Tag("regression")
    @DisplayName("Clearing filters after a search restores the full event list")
    void clearing_filters_restores_full_list() {
        EventListPage listPage = new EventListPage(driver).open(baseUrl);
        listPage.searchByTitle("zzz-no-such-event-zzz");
        assertTrue(listPage.isEmptyStateShown());

        listPage.clearFilters();

        assertTrue(listPage.getVisibleEventTitles().contains(kickoffTitle));
    }
}
