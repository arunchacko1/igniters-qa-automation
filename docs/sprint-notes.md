# Sprint Notes

A simple Agile log for this project — three one-week sprints. There's no
separate product owner here; "sprint review" means re-reading the test plan
and requirements against what actually got built.

## Sprint 1 — 2026-09-15 to 2026-09-19

**Goal:** Stand up the SUT and write the QA planning docs before any test
code, so "correct behavior" is defined before it's automated against.

**Stories done:**
- REQ-001, REQ-002, REQ-003 — login, role-based access, logout.
- REQ-004 through REQ-010 — event browsing, search, filter, and full admin
  CRUD.
- REQ-011, REQ-012, REQ-013 — member registration, cancellation, and the
  capacity/duplicate-registration rules.
- `docs/requirements.md`, `docs/test-plan.md`, `docs/test-cases.md`,
  `docs/traceability-matrix.md` written and reviewed before Sprint 2 started.

**Retro note:** Writing the 56 test cases before touching Selenium forced a
decision on every ambiguous rule (e.g., "is capacity exactly full" an edge
case or the normal case?) while it was still cheap to change. Doing that
after the UI tests existed would have meant rewriting assertions instead of
a spec.

## Sprint 2 — 2026-09-22 to 2026-09-26

**Goal:** Automate everything the test plan promised — UI, API, and DB — and
prove the suite can actually catch bugs, not just pass.

**Stories done:**
- 40 Selenium + JUnit 5 UI tests (Page Object Model, smoke/regression tags,
  3+ parameterized tests).
- 35 REST Assured API tests across all 8 endpoints, plus a Postman
  collection runnable with Newman.
- 10 JDBC database tests, including the orphan-row `LEFT JOIN` check.
- Verified all five `QA_DEFECTS_ENABLED` bugs are silent when the flag is
  off and reproduce when it's on.

**Retro note:** Running the Selenium suite three times in a row surfaced
three real race conditions (a native `<input type="date">` sendKeys
problem, a one-shot flash message read too late, and a click returning
before its redirect landed) that passed on a single run. "Run it three
times before calling it done" earned its place in the quality rules this
sprint, not just as a formality.

## Sprint 3 — 2026-09-29 to 2026-10-01

**Goal:** Add the second UI layer, wire up CI/CD, and document the defects
and the project itself well enough that someone else (or an interviewer)
could pick it up cold.

**Stories done:**
- 9 Playwright TypeScript tests with `storageState` reuse across the
  member/admin test projects.
- GitHub Actions workflow (5 jobs, parallel where safe) and a matching
  Jenkinsfile.
- 5 bug reports (one per seeded defect), GitHub issue templates, and this
  sprint log.
- README, interview notes, and resume bullets.

**Retro note:** The GitHub Actions run failed on its first push —
`./mvnw` had lost its executable bit somewhere along the way (Windows
doesn't track that bit the same way Git does), so the very first CI job
couldn't even run the build. A one-line `git update-index --chmod=+x`
fixed it. Worth remembering: a green build on a Windows dev machine says
nothing about whether the same script is executable on a Linux CI runner.
