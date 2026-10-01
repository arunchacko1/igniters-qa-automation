package org.igniters.qa.tests.ui.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.igniters.qa.tests.support.SeededUsers;
import org.igniters.qa.tests.ui.BaseUiTest;
import org.igniters.qa.tests.ui.pages.EventListPage;
import org.igniters.qa.tests.ui.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Covers REQ-002 — see docs/test-cases.md TC-009, TC-010. */
class RoleAccessTest extends BaseUiTest {

    @Test
    @Tag("functional")
    @DisplayName("TC-009: a member never sees the Create Event control")
    void member_does_not_see_create_event_link() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);

        assertFalse(new EventListPage(driver).open(baseUrl).isCreateEventLinkVisible());
    }

    @Test
    @Tag("functional")
    @DisplayName("TC-010: an admin sees the Create Event control")
    void admin_sees_create_event_link() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.ADMIN_EMAIL, SeededUsers.PASSWORD);

        assertTrue(new EventListPage(driver).open(baseUrl).isCreateEventLinkVisible());
    }

    @Test
    @Tag("regression")
    @DisplayName("A member is blocked (403) from the admin create-event page by URL")
    void member_cannot_reach_admin_create_page_directly() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        driver.get(baseUrl + "/admin/events/new");

        // Spring Security's default Whitelabel error page reports the status in its body;
        // Selenium has no direct way to read the HTTP status code itself.
        assertTrue(driver.getPageSource().contains("403"));
    }
}
