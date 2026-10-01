package org.igniters.qa.tests.ui;

import java.time.Duration;
import org.igniters.qa.tests.support.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Every Selenium test gets a fresh browser — no shared state between tests, no flakiness from leftover sessions. */
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseUiTest {

    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    void setUpDriver() {
        driver = DriverFactory.createChromeDriver();
        baseUrl = TestConfig.baseUrl();
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    WebDriver getDriver() {
        return driver;
    }

    /**
     * The logout button lives in the nav fragment on every page, so it has
     * no dedicated page object. Waiting for clickability here (rather than
     * an unconditional {@code findElement(...).click()}) is what the other
     * page objects do too — skipping it was the cause of occasional flakiness.
     */
    protected void logout() {
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='logout-button']")))
                .click();
        waitForUrlToContain("/login");
    }

    /**
     * A click that triggers navigation returns once the browser *starts*
     * navigating, not necessarily once a redirect chain has fully landed —
     * so reading {@code driver.getCurrentUrl()} immediately after a click
     * is a real source of flakiness. Call this first wherever a test
     * asserts on the URL a click was expected to lead to.
     */
    protected void waitForUrlToContain(String fragment) {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.urlContains(fragment));
    }
}
