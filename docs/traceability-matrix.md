# Traceability Matrix

Links every requirement to its test cases (`docs/test-cases.md`) and to the
automated test class(es) that implement it. Use this to answer "if I change
feature X, what tests should I run?" and "is every requirement covered?"

| Requirement | Test Cases | Automated Test Class(es) |
|---|---|---|
| REQ-001 Member login | TC-001, TC-002, TC-003, TC-004 | `ui.tests.LoginTest`, `playwright-tests/tests/login.spec.ts` |
| REQ-002 Role-based access | TC-009, TC-010, TC-011, TC-012, TC-013 | `ui.tests.RoleAccessTest`, `api.AuthApiTest`, `api.EventsApiTest` |
| REQ-003 Logout | TC-005, TC-006 | `ui.tests.LoginTest` |
| REQ-004 Browse events | TC-014, TC-021 | `ui.tests.EventListTest`, `api.EventsApiTest`, `playwright-tests/tests/browse-events.spec.ts` |
| REQ-005 Search by title | TC-015, TC-016, TC-017, TC-020, TC-022 | `ui.tests.EventSearchTest`, `api.EventsApiTest` |
| REQ-006 Filter by date | TC-018, TC-019, TC-023 | `ui.tests.EventSearchTest`, `api.EventsApiTest` |
| REQ-007 Event detail | TC-024, TC-025, TC-026, TC-027 | `ui.tests.EventDetailTest`, `api.EventsApiTest`, `db.EventDataConsistencyTest`, `playwright-tests/tests/event-detail.spec.ts` |
| REQ-008 Admin create event | TC-028, TC-029, TC-030, TC-031, TC-032, TC-033, TC-034 | `ui.tests.AdminEventCrudTest`, `api.EventsApiTest`, `db.EventCrudDbTest`, `playwright-tests/tests/admin-create-event.spec.ts` |
| REQ-009 Admin edit event | TC-035, TC-036, TC-037, TC-038 | `ui.tests.AdminEventCrudTest`, `api.EventsApiTest`, `db.EventCrudDbTest` |
| REQ-010 Admin delete event | TC-039, TC-040, TC-041 | `ui.tests.AdminEventCrudTest`, `api.EventsApiTest`, `db.OrphanRegistrationTest` |
| REQ-011 Member registers | TC-042, TC-043, TC-044, TC-047, TC-048, TC-049, TC-052, TC-054 | `ui.tests.RegistrationTest`, `api.RegistrationApiTest`, `db.RegistrationDbTest`, `playwright-tests/tests/register-cancel.spec.ts` |
| REQ-012 Member cancels | TC-045, TC-046, TC-053 | `ui.tests.RegistrationTest`, `api.RegistrationApiTest`, `db.RegistrationDbTest`, `playwright-tests/tests/register-cancel.spec.ts` |
| REQ-013 My registrations | TC-050, TC-051 | `api.RegistrationApiTest` |

## Coverage summary

- 13 of 13 requirements have at least one automated test.
- 54 of 56 test cases are automated (the 2 exploratory cases are intentionally manual — see `docs/test-plan.md` §2 Out of Scope rationale).
- Every P1 requirement (REQ-001, 002, 004, 007, 008, 010, 011, 012) has smoke-level coverage.
