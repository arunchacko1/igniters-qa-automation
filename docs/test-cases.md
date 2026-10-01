# Test Cases — Igniters Event Registration App

56 test cases covering login, access control, event browsing, admin event
management, member registration rules, and validation. "Automation" names the
layer(s) that automate the case; "Not automated" cases are manual/exploratory
by design (e.g., one-off visual checks).

Legend — Type: **Smoke** / **Functional** / **Regression** / **Integration**.

## Login & Session (REQ-001, REQ-002, REQ-003)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-001 | REQ-001 | Seeded member exists | Enter correct email/password, submit | Redirected to event list, session cookie set | Smoke | Selenium `LoginTest`, Playwright `login.spec.ts` |
| TC-002 | REQ-001 | Seeded member exists | Enter wrong password, submit | Error message shown, stays on login page | Smoke | Selenium `LoginTest` |
| TC-003 | REQ-001 | None | Enter unknown email, submit | Error message shown, no session created | Functional | Selenium `LoginTest` |
| TC-004 | REQ-001 | None | Submit empty form | Inline validation errors, no request sent | Functional | Selenium `LoginTest` |
| TC-005 | REQ-003 | Logged in as member | Click logout | Session ends, redirected to login page | Smoke | Selenium `LoginTest` |
| TC-006 | REQ-003 | Logged out | Visit `/events` directly | Redirected to login page | Functional | Selenium `LoginTest` |
| TC-007 | REQ-002 | None | `POST /api/auth/login` with valid admin creds | `200 OK`, body has token and role `ADMIN` | Smoke | REST Assured `AuthApiTest` |
| TC-008 | REQ-002 | None | `POST /api/auth/login` with wrong password | `401 Unauthorized`, JSON error body with `code`/`message` | Functional | REST Assured `AuthApiTest` |

## Role-Based Access (REQ-002)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-009 | REQ-002 | Logged in as member | Open event list page | No "Create Event" button visible | Functional | Selenium `RoleAccessTest` |
| TC-010 | REQ-002 | Logged in as admin | Open event list page | "Create Event" button visible | Functional | Selenium `RoleAccessTest` |
| TC-011 | REQ-002 | Member token | `POST /api/events` with member token | `403 Forbidden` | Regression | REST Assured `EventsApiTest` |
| TC-012 | REQ-002 | No token | `GET /api/me/registrations` with no Authorization header | `401 Unauthorized` | Regression | REST Assured `AuthApiTest` |
| TC-013 | REQ-002 | None | Any protected endpoint with a malformed/garbage token | `401 Unauthorized` | Regression | REST Assured `AuthApiTest` |

## Event Browsing, Search, Filter (REQ-004, REQ-005, REQ-006)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-014 | REQ-004 | Logged in, 10 seeded events | Open event list | All upcoming events shown with title/date/location/capacity | Smoke | Selenium `EventListTest`, Playwright |
| TC-015 | REQ-005 | Logged in | Search `"camp"` | Only titles containing "camp" shown | Functional | Selenium `EventSearchTest` |
| TC-016 | REQ-005 | Logged in | Search `"CAMP"` (uppercase) | Same results as lowercase search (case-insensitive) | Regression | Selenium `EventSearchTest`, REST Assured |
| TC-017 | REQ-005 | Logged in | Search for nonsense string | Empty-state message, no error | Functional | Selenium `EventSearchTest` |
| TC-018 | REQ-006 | Logged in | Filter by a date that has events | Only events on that date shown | Functional | Selenium `EventSearchTest` |
| TC-019 | REQ-006 | Logged in | Filter by a date with no events | Empty-state message shown | Functional | Selenium `EventSearchTest` |
| TC-020 | REQ-005 | Logged in | `@ParameterizedTest`: search `"Camp"`, `"HIKE"`, `"serv"` | Each returns the matching seeded events | Regression | Selenium `EventSearchTest#searchIsCaseInsensitive` |
| TC-021 | REQ-004 | None | `GET /api/events` with no filters | `200 OK`, array of all upcoming events | Smoke | REST Assured `EventsApiTest` |
| TC-022 | REQ-005 | None | `GET /api/events?q=camp` | `200 OK`, only matching events, case-insensitive | Regression | REST Assured `EventsApiTest` |
| TC-023 | REQ-006 | None | `GET /api/events?date=YYYY-MM-DD` | `200 OK`, only events on that date | Regression | REST Assured `EventsApiTest` |

## Event Detail (REQ-007)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-024 | REQ-007 | Logged in, event exists | Click an event title | Detail page shows description, date, location, remaining seats | Smoke | Selenium `EventDetailTest`, Playwright |
| TC-025 | REQ-007 | Logged in | Navigate to `/events/999999` (nonexistent) | 404 page shown | Functional | Selenium `EventDetailTest` |
| TC-026 | REQ-007 | None | `GET /api/events/{id}` for a real event | `200 OK`, body matches DB row | Integration | REST Assured + JDBC `EventDataConsistencyTest` |
| TC-027 | REQ-007 | None | `GET /api/events/999999` | `404 Not Found`, JSON error body | Functional | REST Assured `EventsApiTest` |

## Admin — Create Event (REQ-008)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-028 | REQ-008 | Logged in as admin | Fill valid form, submit | Event created, appears in list immediately | Smoke | Selenium `AdminEventCrudTest`, Playwright |
| TC-029 | REQ-008 | Logged in as admin | Submit with a past date | Validation error, event not created | Functional | Selenium `AdminEventCrudTest` |
| TC-030 | REQ-008 | Logged in as admin | Submit with title blank | Inline required-field error | Functional | Selenium `AdminEventCrudTest` |
| TC-031 | REQ-008 | Admin token | `POST /api/events` with valid body | `201 Created`, `Location` header, body has new ID | Smoke | REST Assured `EventsApiTest` |
| TC-032 | REQ-008 | Admin token | `POST /api/events` with past date | `400 Bad Request`, error code `EVENT_DATE_IN_PAST` | Regression | REST Assured `EventsApiTest` |
| TC-033 | REQ-008 | Admin token | `POST /api/events` with capacity = 0 | `400 Bad Request` | Regression | REST Assured `EventsApiTest` |
| TC-034 | REQ-008 | Admin token | `POST /api/events` then check DB | One row in `events` with matching values | Integration | JDBC `EventCrudDbTest` |

## Admin — Edit Event (REQ-009)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-035 | REQ-009 | Admin, event exists | Edit title, save | List and detail page reflect new title | Functional | Selenium `AdminEventCrudTest` |
| TC-036 | REQ-009 | Admin token | `PUT /api/events/{id}` with new fields | `200 OK`, body reflects changes | Functional | REST Assured `EventsApiTest` |
| TC-037 | REQ-009 | Admin token | `PUT /api/events/999999` (nonexistent) | `404 Not Found` | Regression | REST Assured `EventsApiTest` |
| TC-038 | REQ-009 | Admin, event has registrations | Edit event capacity | Existing registrations remain unaffected | Integration | JDBC `EventCrudDbTest` |

## Admin — Delete Event (REQ-010)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-039 | REQ-010 | Admin, event exists | Click delete, confirm | Event removed from list immediately | Smoke | Selenium `AdminEventCrudTest` |
| TC-040 | REQ-010 | Admin token | `DELETE /api/events/{id}` | `204 No Content` | Smoke | REST Assured `EventsApiTest` |
| TC-041 | REQ-010 | Admin, event has registrations | Delete the event, then query `registrations` table | No orphan rows remain for that event ID | Integration | JDBC `OrphanRegistrationTest` |

## Member — Register & Cancel (REQ-011, REQ-012, REQ-013)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-042 | REQ-011 | Member, event has open seats | Click Register | Confirmation shown, remaining seats -1 | Smoke | Selenium `RegistrationTest`, Playwright |
| TC-043 | REQ-011 | Member, event at full capacity | Click Register | Error shown: event full, no row created | Functional | Selenium `RegistrationTest` |
| TC-044 | REQ-011 | Member already registered | Click Register again on same event | Error shown: already registered | Functional | Selenium `RegistrationTest` |
| TC-045 | REQ-012 | Member registered for an event | Click Cancel | Registration removed, remaining seats +1 | Smoke | Selenium `RegistrationTest`, Playwright |
| TC-046 | REQ-012 | Member not registered | `DELETE /api/events/{id}/registrations` | `404 Not Found` | Functional | REST Assured `RegistrationApiTest` |
| TC-047 | REQ-011 | Member token, event at capacity | `POST /api/events/{id}/registrations` | `409 Conflict`, error code `EVENT_FULL` | Regression | REST Assured `RegistrationApiTest` |
| TC-048 | REQ-011 | Member token, already registered | `POST /api/events/{id}/registrations` again | `409 Conflict`, error code `ALREADY_REGISTERED` | Regression | REST Assured `RegistrationApiTest` |
| TC-049 | REQ-011 | Event capacity = 1, two members | Member A registers, then Member B registers | A succeeds, B gets `409 Conflict` | Regression | REST Assured `RegistrationApiTest` |
| TC-050 | REQ-013 | Member with 2 registrations | `GET /api/me/registrations` | `200 OK`, array has exactly those 2 events | Functional | REST Assured `RegistrationApiTest` |
| TC-051 | REQ-013 | Member with 0 registrations | `GET /api/me/registrations` | `200 OK`, empty array (not an error) | Functional | REST Assured `RegistrationApiTest` |
| TC-052 | REQ-011 | Member registers via API | Check DB `registrations` row and event's computed remaining count | Row exists; remaining = capacity - count(registrations) | Integration | JDBC `RegistrationDbTest` |
| TC-053 | REQ-012 | Member cancels via API | Check DB | Row no longer exists in `registrations` | Integration | JDBC `RegistrationDbTest` |
| TC-054 | REQ-011 | Unique constraint on (user_id, event_id) | Attempt direct duplicate insert via JDBC | Database rejects with unique-constraint violation | Integration | JDBC `RegistrationDbTest` |

## Exploratory / Manual (not automated by design)

| ID | Req | Preconditions | Steps | Expected Result | Type | Automation |
|---|---|---|---|---|---|---|
| TC-055 | REQ-004 | Logged in | Resize browser window to mobile width | Page remains usable, no horizontal scroll | Functional | Not automated (manual visual check) |
| TC-056 | REQ-008 | Logged in as admin | Tab through the create-event form using only the keyboard | Every field and the submit button are reachable in order | Functional | Not automated (manual visual check) |
