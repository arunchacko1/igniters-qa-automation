# Interview Notes

Fifteen questions an interviewer is likely to ask about this project, with
short answers. Know these well enough to explain them without re-reading —
that's the actual goal of keeping this list.

### 1. Walk me through this project in two minutes.
It's a QA automation portfolio: a small Spring Boot event-registration app
(the "SUT") plus four independent test layers around it — Selenium,
Playwright, REST Assured + Postman, and JDBC — wired into CI with GitHub
Actions and Jenkins. The app has a feature flag, `QA_DEFECTS_ENABLED`, that
switches on five deliberate bugs so the test suites have something real to
catch, which is how I proved the automation actually works rather than just
asserting trivially-true things.

### 2. Why did you write the SUT yourself instead of testing an existing app?
Full control over the test surface. I needed specific, deliberately-placed
bugs to demonstrate detection, specific `data-testid` hooks for stable
locators, and an API/DB/UI combination that all agree with each other — all
much easier to guarantee by building the thing than by finding an existing
app that happens to fit.

### 3. How do the UI and API authenticate differently, and why?
The browser pages use Spring Security's session-based `formLogin` (a cookie).
The REST API uses a bearer token issued by `POST /api/auth/login` and
checked by a custom `OncePerRequestFilter`. Two separate Spring Security
filter chains (`@Order(1)` for `/api/**`, stateless; `@Order(2)` for
everything else, session-based) handle this — real systems usually separate
browser and API auth this way, and it also means the API test suite never
needs a browser.

### 4. What's a real bug you found while building this, not one you seeded?
Spring Security's `authorizeHttpRequests` matches rules in order and stops
at the first match. I had `DELETE /api/events/**` (ADMIN-only) declared
before the more specific `DELETE /api/events/{id}/registrations`
(MEMBER-only) rule, so the broad admin rule shadowed the specific one and
members got 403 trying to cancel their own registration. Found it by
actually curling the endpoint during manual verification, not by a test —
which is itself a point worth making: exploratory testing still catches
things automated suites written against your own mental model won't.

### 5. Why does `registrations.event_id` have no database foreign key?
So seeded defect #3 (orphaned registrations after an event delete) can
actually happen. A real FK would force Postgres to either cascade the
delete or block it outright — there'd be no way to reproduce "the event is
gone but the registration row is still there." It's also a realistic bug
shape: teams often add a FK on the stable side of a relationship and skip it
on the side that churns, which is exactly why the bug is worth testing for.

### 6. How do you keep UI tests from being flaky?
Explicit waits only, never `Thread.sleep`. But the bigger lesson from this
project: I ran the 40-test Selenium suite three times before calling it
done, per my own quality bar, and it wasn't stable on the first pass. I
found and fixed three real race conditions — a native date input that
doesn't accept `sendKeys()` reliably, a one-shot flash message read on a
second navigation that had already consumed it, and clicks that return
before their redirect fully lands. All three needed an actual fix, not a
longer timeout.

### 7. Selenium vs. Playwright — what's the real difference, from using both?
Playwright's locators auto-wait for actionability before every action,
which absorbs most of the timing issues Selenium makes you handle
explicitly with `WebDriverWait`. That's not automatically "better" for
learning purposes, though — writing the Selenium suite forced me to
understand *why* each wait is needed, which the auto-waiting in Playwright
would have let me skip past.

### 8. How do capacity and duplicate-registration rules work?
`RegistrationService.register()` checks for an existing
`(user_id, event_id)` registration first (→ 409 `ALREADY_REGISTERED`), then
compares the current registration count against capacity (→ 409
`EVENT_FULL`). The database backs up the duplicate check with a real unique
constraint on `(user_id, event_id)` — `RegistrationDbTest` proves that by
inserting a duplicate directly via JDBC and asserting on SQLSTATE `23505`.

### 9. Why plain JDBC for the DB tests instead of the same JPA repositories the app uses?
Testing through the app's own ORM layer only proves the ORM agrees with
itself. Raw SQL over a direct JDBC connection sees exactly what's on disk,
independent of whatever the application code claims happened — which is the
whole point of a separate DB-layer test.

### 10. Why Surefire HTML instead of Allure?
Allure needs a separate CLI/server step to generate its report; Surefire's
HTML (via `maven-surefire-report-plugin`) comes from a single
`mvn surefire-report:report-only` with zero extra tooling. For a project
meant to run end-to-end with one script on a clean machine, fewer moving
parts wins. Logged in `docs/decisions.md` along with the other
simple-option calls.

### 11. How does CI avoid wasting time rebuilding the SUT for every job?
One `build` job packages the jar and uploads it as an artifact; the four
downstream jobs (smoke, regression, playwright, newman) download it and
just run `java -jar`. Regression only runs on PRs and the nightly cron, not
every push — smoke is the fast everyday gate, regression is the thorough
periodic one.

### 12. What would break your test suite the most if the app changed?
The `data-testid` attributes — every Selenium/Playwright locator depends on
them being stable, which is exactly why they exist as a separate attribute
instead of testing against CSS classes or text content that's likely to
change for cosmetic reasons.

### 13. How do you decide what's a smoke test vs. a regression test?
Smoke tests are the critical path only — can a user log in, see events, and
register? Fast enough to run on every single push without slowing anyone
down. Regression is everything else: edge cases, negative paths, role
checks — thorough, not fast, so it only runs where that cost is justified
(PRs, nightly).

### 14. How would you extend this to test a staging/cloud environment?
Nothing in the test code is hardcoded to localhost — every layer reads its
target from an environment variable (`BASE_URL`, `API_BASE_URL`, the
Postman environment file). Pointing the suite at staging is just setting
that variable differently; see `docs/cloud-notes.md` for the full mapping
from this local setup to a real cloud deployment.

### 15. What's the weakest part of this project, if you're honest about it?
No performance or load testing — everything here checks correctness, not
behavior under concurrency or load. Also no contract testing between the
API and any consumer, since there isn't a second real client to contract
against yet. Both are called out explicitly in the README's "What I would
add next," rather than left unstated.
