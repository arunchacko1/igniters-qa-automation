package org.igniters.qa.tests.ui.pages;

import java.time.LocalDate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EventFormPage extends BasePage {

    public EventFormPage(WebDriver driver) {
        super(driver);
    }

    public EventFormPage openNew(String baseUrl) {
        driver.get(baseUrl + "/admin/events/new");
        return this;
    }

    public EventFormPage openEdit(String baseUrl, long eventId) {
        driver.get(baseUrl + "/admin/events/" + eventId + "/edit");
        return this;
    }

    /** Pass null for any field to leave it blank — used by the required-field tests. */
    public void fillForm(String title, String description, LocalDate date, String location, Integer capacity) {
        setValue("event-title-input", title);
        setValue("event-description-input", description);
        setDateValue("event-date-input", date == null ? null : date.toString());
        setValue("event-location-input", location);
        setValue("event-capacity-input", capacity == null ? null : String.valueOf(capacity));
    }

    private void setValue(String testId, String value) {
        WebElement element = findTestId(testId);
        element.clear();
        if (value != null) {
            element.sendKeys(value);
        }
    }

    public void submit() {
        clickTestId("event-form-submit");
    }

    public boolean hasErrorFor(String fieldErrorTestId) {
        return waitForTestId(fieldErrorTestId);
    }

    public boolean hasGeneralError() {
        return waitForTestId("error-message");
    }
}
