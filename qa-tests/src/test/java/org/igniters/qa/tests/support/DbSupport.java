package org.igniters.qa.tests.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** One JDBC connection per call — these tests run a handful of queries each, not enough to need a pool. */
public final class DbSupport {

    private DbSupport() {
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://" + TestConfig.dbHost() + ":" + TestConfig.dbPort() + "/" + TestConfig.dbName();
        return DriverManager.getConnection(url, TestConfig.dbUser(), TestConfig.dbPassword());
    }
}
