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

/** Covers REQ-001 (login), REQ-003 (logout) — see docs/test-cases.md TC-001..TC-006. */
class LoginTest extends BaseUiTest {

    @Test
    @Tag("smoke")
    @DisplayName("TC-001: valid credentials log the member in and show the event list")
    void member_can_log_in_with_valid_credentials() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);

        assertTrue(driver.getCurrentUrl().endsWith("/events"));
        assertFalse(new EventListPage(driver).isCreateEventLinkVisible());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-002: wrong password shows an error and does not log in")
    void wrong_password_shows_error_and_stays_on_login_page() {
        LoginPage loginPage = new LoginPage(driver).open(baseUrl);
        loginPage.loginAs(SeededUsers.MEMBER_EMAIL, "not-the-password");

        assertTrue(loginPage.isErrorMessageDisplayed());
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-003: unknown email shows the same error, not a stack trace")
    void unknown_email_shows_error_and_stays_on_login_page() {
        LoginPage loginPage = new LoginPage(driver).open(baseUrl);
        loginPage.loginAs("nobody@igniters.org", "whatever");

        assertTrue(loginPage.isErrorMessageDisplayed());
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-004: submitting the login form empty never leaves the login page")
    void empty_login_form_does_not_submit() {
        LoginPage loginPage = new LoginPage(driver).open(baseUrl);
        loginPage.loginAs("", "");

        // Browser-native "required" validation blocks the POST entirely.
        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertTrue(loginPage.isLoginFormDisplayed());
    }

    @Test
    @Tag("smoke")
    @DisplayName("TC-005: logout ends the session and returns to the login page")
    void logout_clears_session_and_returns_to_login() {
        new LoginPage(driver).open(baseUrl).loginAs(SeededUsers.MEMBER_EMAIL, SeededUsers.PASSWORD);
        logout();

        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertTrue(new LoginPage(driver).isInfoMessageDisplayed());
    }

    @Test
    @Tag("regression")
    @DisplayName("TC-006: visiting a protected page while logged out redirects to login")
    void visiting_events_while_logged_out_redirects_to_login() {
        driver.get(baseUrl + "/events");

        assertFalse(driver.getCurrentUrl().endsWith("/events"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }
}
