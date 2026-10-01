package org.igniters.qa.tests.ui.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Reusable element-finding actions for every page object. Everything waits
 * explicitly for a condition rather than sleeping a fixed amount of time —
 * that's what keeps these tests from being flaky under CI load.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        // Generous on purpose: running 40 sequential browser sessions against
        // a locally dockerized SUT means occasional requests queue up behind
        // others, not that the app itself is slow — a short timeout would
        // turn system load into test flakiness.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    private By testId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }

    protected WebElement findTestId(String testId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(testId(testId)));
    }

    protected List<WebElement> findAllTestId(String testId) {
        wait.until(ExpectedConditions.presenceOfElementLocated(testId(testId)));
        return driver.findElements(testId(testId));
    }

    protected void clickTestId(String testId) {
        wait.until(ExpectedConditions.elementToBeClickable(testId(testId))).click();
    }

    protected void typeIntoTestId(String testId, String text) {
        WebElement element = findTestId(testId);
        element.clear();
        element.sendKeys(text);
    }

    /** Instant check — use for asserting something is NOT there, on a page that's already fully loaded. */
    protected boolean isTestIdPresent(String testId) {
        return !driver.findElements(testId(testId)).isEmpty();
    }

    /**
     * Use instead of {@link #isTestIdPresent} right after an action (a
     * submit, a redirect) whose result hasn't necessarily rendered yet.
     * Waits up to the normal timeout before concluding "not there".
     */
    protected boolean waitForTestId(String testId) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(testId(testId)));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public String getText(String testId) {
        return findTestId(testId).getText();
    }

    /**
     * Native {@code <input type="date">} widgets are segmented (day/month/year
     * sub-fields), so {@code sendKeys("2026-10-10")} types into whichever
     * segment has focus one character at a time and rarely produces the
     * intended date. Setting the value via JS and firing input/change
     * events is the standard, reliable workaround.
     */
    protected void setDateValue(String testId, String isoDate) {
        WebElement element = findTestId(testId);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                        + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                element,
                isoDate == null ? "" : isoDate);
    }
}
