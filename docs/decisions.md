# Decisions Log

Simple decisions made while building this project, and why, so the reasoning
isn't lost. Follows the spec's instruction: "if a decision is unclear, choose
the simple option and write it down here."

| # | Decision | Why |
|---|---|---|
| 1 | Use Surefire HTML reports, not Allure. | Allure needs a separate CLI/server and a report-generation step; Surefire's HTML comes free with `mvn test` and needs zero extra tooling. For a portfolio project meant to run with one command on a clean machine, fewer moving parts wins. |
| 2 | Session-cookie auth for the Thymeleaf UI, bearer-token auth for the REST API. | These are two different clients (browser form login vs. API callers) and real systems usually separate them this way. It also lets the API test suite authenticate without a browser. |
| 3 | One Postgres schema, three tables (`users`, `events`, `registrations`), no soft deletes. | Keeps the DB tests simple to read and matches the spec exactly. Soft deletes would add a `deleted_at` column and extra WHERE clauses the spec never asked for. |
| 4 | `QA_DEFECTS_ENABLED` is read once at startup via a Spring `@ConfigurationProperties` bean, not re-checked per request. | Simpler to reason about; nothing in the spec requires toggling defects without a restart, and `docker compose` restarts are cheap. |
| 5 | Capacity is computed as `capacity - COUNT(registrations)` at read time, not stored as a mutable counter column. | Avoids a whole class of bugs where the counter and the actual row count drift apart. The DB tests in Phase 6 specifically check this computed value against the row count. |
| 6 | Dates are stored and compared as `DATE` (no time component). | The spec's "future date" rule only needs day-level granularity; adding time-of-day would require picking a timezone policy the spec never specifies. |
| 7 | The 5 seeded defects live behind `if (defectsEnabled)` branches directly in the normal service/controller code, not in a separate "buggy mode" module. | Keeps the diff between buggy and correct behavior small and readable — exactly what a QA engineer would want to point to in an interview. |
| 8 | Postman collection and REST Assured tests cover the same endpoints but are not generated from one another. | Having two independently-written test suites hitting the same API is realistic (QA engineers write Postman for manual exploration and REST Assured for CI) and demonstrates both tools honestly rather than faking coverage. |
| 9 | `registrations.event_id` has no DB-level foreign key (only `user_id` does). | A real DB-level FK would make seeded defect #3 (orphan rows after event delete) physically impossible to reproduce — Postgres would either cascade or block the delete. Skipping the FK on the churn-prone side of the relationship is also a realistic real-world mistake, which is why `EventService#deleteEvent` has to explicitly delete dependent registrations itself, and why `OrphanRegistrationTest`'s `LEFT JOIN` check is a meaningful regression test rather than a constraint restating what the DB already guarantees. |
