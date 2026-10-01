package org.igniters.qa.tests.ui.pages;

import java.time.Duration;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        return this;
    }

    public void loginAs(String email, String password) {
        typeIntoTestId("login-email", email);
        typeIntoTestId("login-password", password);
        clickTestId("login-submit");
        // A click that submits the form returns once navigation *starts*,
        // not once it lands — waiting for either end-state here (instead of
        // letting callers read the URL/DOM immediately) is what makes every
        // caller's next step deterministic, success or failure. A timeout
        // means the browser's own "required" validation blocked the submit
        // client-side (e.g. empty fields) — there's no server round trip to wait for.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> isTestIdPresent("nav-bar") || isTestIdPresent("error-message"));
        } catch (TimeoutException ignored) {
            // Nothing was submitted — the caller is asserting against the unchanged login page.
        }
    }

    public boolean isErrorMessageDisplayed() {
        return waitForTestId("error-message");
    }

    public boolean isLoginFormDisplayed() {
        return isTestIdPresent("login-form");
    }

    public boolean isInfoMessageDisplayed() {
        return waitForTestId("info-message");
    }
}
