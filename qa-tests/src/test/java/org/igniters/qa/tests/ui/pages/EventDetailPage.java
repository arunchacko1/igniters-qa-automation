package org.igniters.qa.tests.ui.pages;

import org.openqa.selenium.WebDriver;

public class EventDetailPage extends BasePage {

    public EventDetailPage(WebDriver driver) {
        super(driver);
    }

    public EventDetailPage open(String baseUrl, long eventId) {
        driver.get(baseUrl + "/events/" + eventId);
        return this;
    }

    public String getTitle() {
        return getText("event-detail-title");
    }

    public String getRemainingSeatsText() {
        return getText("event-detail-remaining");
    }

    public void clickRegister() {
        clickTestId("register-button");
    }

    public void clickCancelRegistration() {
        clickTestId("cancel-registration-button");
    }

    public boolean isRegisterButtonVisible() {
        return isTestIdPresent("register-button");
    }

    public boolean isCancelButtonVisible() {
        return isTestIdPresent("cancel-registration-button");
    }

    public boolean isErrorMessageDisplayed() {
        return waitForTestId("error-message");
    }

    public String getErrorMessage() {
        return getText("error-message");
    }

    public void clickEdit() {
        clickTestId("edit-event-link");
    }

    public void clickDelete() {
        clickTestId("delete-event-button");
    }

    public boolean isEditLinkVisible() {
        return isTestIdPresent("edit-event-link");
    }

    public boolean isNotFoundPage() {
        return isTestIdPresent("not-found-heading");
    }
}
