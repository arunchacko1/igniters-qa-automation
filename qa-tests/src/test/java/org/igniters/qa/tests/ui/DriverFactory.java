package org.igniters.qa.tests.ui;

import org.igniters.qa.tests.support.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Selenium Manager (built into Selenium 4.6+) resolves a matching
 * chromedriver automatically — no WebDriverManager dependency needed.
 */
final class DriverFactory {

    private DriverFactory() {
    }

    static WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        if (TestConfig.seleniumHeadless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        return new ChromeDriver(options);
    }
}
