# Resume Bullets

Every number below is real for the finished state of this project — see
the README's test counts and the CI run history for the source.

- Designed and built a full-stack test automation portfolio (Spring Boot
  app + 5-layer test suite) covering 8 REST endpoints with 94 automated
  tests (40 Selenium/JUnit 5, 35 REST Assured, 10 JDBC, 9 Playwright/
  TypeScript) plus a 15-request Postman/Newman collection, achieving 100%
  pass rate across 3 consecutive stability runs.

- Implemented a feature-flagged bug-seeding system (5 deliberate defects
  behind one environment variable) to prove automated test coverage by
  demonstration rather than assertion — every suite passes with the flag
  off and fails at the exact seeded defect with it on.

- Built a GitHub Actions CI/CD pipeline (5 jobs, parallelized where safe)
  and a matching Jenkins declarative pipeline, cutting a full
  smoke-to-regression feedback loop to under 5 minutes end-to-end on a
  shared build artifact.

- Diagnosed and fixed 3 non-obvious UI test race conditions (native date
  input `sendKeys` unreliability, a one-shot flash-message read timing bug,
  and post-click navigation races) by running the suite repeatedly rather
  than accepting single-pass green as sufficient — codified into the
  project's own quality bar.

- Authored complete QA documentation ahead of implementation: a test plan,
  13 user stories with acceptance criteria, 56 traced test cases, a
  requirements-to-test traceability matrix, and 5 bug reports with
  root-cause analysis for the seeded defects.
