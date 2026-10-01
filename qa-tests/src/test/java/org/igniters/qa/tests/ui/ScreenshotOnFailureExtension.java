package org.igniters.qa.tests.ui;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Saves a screenshot next to the Surefire report whenever a UI test fails, so a CI failure is debuggable without rerunning it. */
public class ScreenshotOnFailureExtension implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        Object testInstance = context.getRequiredTestInstance();
        if (!(testInstance instanceof BaseUiTest baseUiTest)) {
            return;
        }
        WebDriver driver = baseUiTest.getDriver();
        if (!(driver instanceof TakesScreenshot takesScreenshot)) {
            return;
        }
        try {
            Path targetDir = Path.of("target", "screenshots");
            Files.createDirectories(targetDir);
            File screenshot = takesScreenshot.getScreenshotAs(OutputType.FILE);
            String fileName = context.getRequiredTestClass().getSimpleName()
                    + "_" + context.getRequiredTestMethod().getName() + ".png";
            Files.copy(screenshot.toPath(), targetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
