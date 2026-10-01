package org.igniters.qa.tests.support;

/** Mirrors the accounts Flyway's V2__seed_data.sql inserts — see sut-app for the source of truth. */
public final class SeededUsers {

    private SeededUsers() {
    }

    public static final String PASSWORD = "Passw0rd!";

    public static final String ADMIN_EMAIL = "avery.admin@igniters.org";
    public static final String SECOND_ADMIN_EMAIL = "riley.admin@igniters.org";

    public static final String MEMBER_EMAIL = "jordan.member@igniters.org";
    public static final String SECOND_MEMBER_EMAIL = "casey.member@igniters.org";
    public static final String THIRD_MEMBER_EMAIL = "drew.member@igniters.org";
}
