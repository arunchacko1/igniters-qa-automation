package org.igniters.qa.tests.support;

/**
 * Single place every test layer reads its target environment from. Values
 * come from system properties (wired to env vars in qa-tests/pom.xml) with
 * localhost defaults, so `mvn test` works out of the box after
 * `docker compose up`, and CI/staging just override the properties.
 */
public final class TestConfig {

    private TestConfig() {
    }

    public static String baseUrl() {
        return System.getProperty("base.url", "http://localhost:8080");
    }

    public static String apiBaseUrl() {
        return System.getProperty("api.base.url", "http://localhost:8080/api");
    }

    public static String dbHost() {
        return System.getProperty("db.host", "localhost");
    }

    public static String dbPort() {
        return System.getProperty("db.port", "5432");
    }

    public static String dbName() {
        return System.getProperty("db.name", "igniters_qa");
    }

    public static String dbUser() {
        return System.getProperty("db.user", "igniters");
    }

    public static String dbPassword() {
        return System.getProperty("db.password", "change-me-locally");
    }

    public static boolean seleniumHeadless() {
        // Default headless so CI never needs a display; set
        // SELENIUM_HEADLESS=false locally to watch the browser run.
        return !"false".equalsIgnoreCase(System.getProperty("selenium.headless", "true"));
    }
}
