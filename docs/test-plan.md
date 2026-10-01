# Test Plan — Igniters Event Registration App

## 1. Objective

Verify that the event registration app lets members browse and register for
events, lets admins manage events, and enforces its business rules (capacity,
duplicate registration, future-dated events) — across the UI, the REST API,
and the database.

## 2. Scope

**In scope**
- Login/logout and role-based access (ADMIN vs MEMBER).
- Event browsing, search, and date filtering.
- Admin create/edit/delete of events.
- Member register/cancel for events, including the capacity and
  duplicate-registration business rules.
- REST API contract: status codes, response bodies, error codes.
- Database integrity: foreign keys, unique constraints, orphan-row checks.
- CI pipeline execution of all of the above.

**Out of scope**
- Load/performance testing (noted as future work in the README).
- Email/SMS notifications — the app has none.
- Payment processing — events are free to register for.
- Mobile native apps — this is a desktop web app only.
- Accessibility (WCAG) audit — not required for this portfolio scope.

## 3. Test Types

| Type | Purpose |
|---|---|
| Smoke | Fast, critical-path checks (login, see events, register) run on every push. |
| Functional | Verify each feature works per its acceptance criteria. |
| Regression | Full suite, run on PRs and nightly, to catch breakage from new changes. |
| Integration | Verify the UI/API layer and the database agree (e.g., API create → DB row exists). |

## 4. Test Levels

- **UI** — Selenium (Java) as primary, Playwright (TypeScript) as a secondary
  cross-check on the same critical flows.
- **API** — REST Assured (Java, in-CI) and a Postman collection (manual
  exploration + Newman in CI) hitting the same endpoints.
- **Database** — JDBC + raw SQL, bypassing the ORM, to validate data on disk
  independently of what the application code claims happened.

## 5. Tools

| Tool | Role |
|---|---|
| Java 21 / Maven | Build and test runner for the SUT and `qa-tests`. |
| JUnit 5 | Test framework for UI, API, and DB tests. |
| Selenium 4 | Browser automation (Page Object Model). |
| Playwright + TypeScript | Secondary browser automation. |
| REST Assured | API test client and assertions. |
| Postman + Newman | Manual API exploration and CI-runnable collection. |
| PostgreSQL 16 (Docker) | System database. |
| JDBC | Direct SQL assertions against PostgreSQL. |
| GitHub Actions / Jenkins | CI/CD execution of all suites. |
| Surefire HTML reports | Test result reporting (see `docs/decisions.md` for why). |

## 6. Environments

| Name | Purpose | How it's started |
|---|---|---|
| Local | Developer's machine | `docker compose up` + `./scripts/run-all.sh` |
| CI | GitHub Actions / Jenkins | Same `docker-compose.yml`, headless browsers |
| Staging (future) | Cloud smoke checks | See `docs/cloud-notes.md` |

All test layers read `BASE_URL` / `API_BASE_URL` from environment variables, so
the same test code runs against local, CI, or a deployed staging URL without
code changes.

## 7. Entry Criteria

- `docker compose up` brings up PostgreSQL and the SUT successfully.
- Flyway migrations run clean with no errors.
- The seed data (2 admins, 5 members, 10 events) is present.
- `QA_DEFECTS_ENABLED=false` (the default) for all "must pass" runs.

## 8. Exit Criteria

- 100% of smoke tests pass on every push.
- 100% of regression tests pass before a PR merges.
- No open P1/P2 defect is left unresolved at release.
- All five seeded defects are reproduced by at least one automated test when
  `QA_DEFECTS_ENABLED=true` (proves the suite has real detection power).

## 9. Risks

| Risk | Mitigation |
|---|---|
| Flaky UI tests from timing issues | Explicit waits only, no `Thread.sleep`; retry disabled so flakiness is visible, not hidden. |
| Shared test data causes cross-test interference | Each test creates its own data via the API/DB and cleans up after itself. |
| CI browser version drift | Pin Selenium/Playwright browser versions in CI config. |
| Database state leaking between test runs | Each suite run uses Flyway's clean slate from Docker Compose; DB tests use unique titles/emails per test. |

## 10. Defect Workflow

1. A failing test or manual exploration finds a bug.
2. File a bug report in `docs/bug-reports/` using the standard template
   (ID, severity, priority, repro steps, expected vs. actual, evidence, linked test case).
   A matching GitHub Issue is opened from the "Bug report" issue template.
3. Severity/priority set using:
   - **Severity** = how bad the impact is (Critical/High/Medium/Low).
   - **Priority** = how soon it must be fixed (P1/P2/P3), which can differ from severity
     (e.g., a cosmetic bug on the login page is low severity but could be high priority
     before a demo).
4. Fix is made on a feature branch, linked to the issue, and merged via PR once
   the originally-failing test (and the rest of regression) passes.
5. Bug report is updated with the fixing commit/PR link and closed.

## 11. Deliverables

- Automated UI (Selenium + Playwright), API (REST Assured + Postman), and DB
  (JDBC) test suites.
- CI pipelines (GitHub Actions + Jenkins) running all of the above.
- This test plan, requirements doc, test cases, traceability matrix, and bug reports.
