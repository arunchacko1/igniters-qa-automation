package org.igniters.qa.tests.ui.pages;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class EventListPage extends BasePage {

    public EventListPage(WebDriver driver) {
        super(driver);
    }

    public EventListPage open(String baseUrl) {
        driver.get(baseUrl + "/events");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='nav-bar']")));
        return this;
    }

    public void searchByTitle(String query) {
        typeIntoTestId("search-input", query);
        clickTestId("search-submit");
        // Without this, a caller reading the list right after the click can
        // see the PREVIOUS (pre-search) page: any event-title-link already
        // present satisfies findAllTestId's presence wait, so it doesn't by
        // itself prove the search's navigation has landed yet.
        wait.until(ExpectedConditions.urlContains("q="));
    }

    public void filterByDate(LocalDate date) {
        setDateValue("date-filter-input", date.toString());
        clickTestId("search-submit");
        wait.until(ExpectedConditions.urlContains("date="));
    }

    public void clearFilters() {
        clickTestId("clear-filters-link");
        wait.until(d -> !d.getCurrentUrl().contains("q=") && !d.getCurrentUrl().contains("date="));
    }

    public List<String> getVisibleEventTitles() {
        return findAllTestId("event-title-link").stream().map(e -> e.getText()).toList();
    }

    public void clickEventTitled(String title) {
        findAllTestId("event-title-link").stream()
                .filter(e -> e.getText().equals(title))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No event titled '" + title + "' is visible"))
                .click();
    }

    public boolean isEmptyStateShown() {
        return waitForTestId("empty-state");
    }

    public boolean isCreateEventLinkVisible() {
        return isTestIdPresent("nav-create-event-link");
    }
}
