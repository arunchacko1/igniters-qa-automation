# Requirements — Igniters Event Registration App

User stories for the youth group event registration system. Each story has an ID
used throughout `test-cases.md` and `traceability-matrix.md` so every requirement
can be traced to the tests that verify it.

Priority scale: **P1** = must have for launch, **P2** = important, **P3** = nice to have.

---

### REQ-001 — Member login
**As a** youth group member, **I want to** log in with my email and password
**so that** I can see events and manage my registrations.

Acceptance criteria:
- Valid credentials redirect to the event list and start a session.
- Invalid credentials show an error message and stay on the login page.
- The password field masks input.

Priority: **P1**

---

### REQ-002 — Role-based access
**As an** admin, **I want** the system to recognize my ADMIN role
**so that** only I can see event management controls (create/edit/delete).

Acceptance criteria:
- A MEMBER never sees create/edit/delete controls in the UI.
- A MEMBER calling an admin-only API endpoint receives `403 Forbidden`.
- An unauthenticated request to a protected endpoint receives `401 Unauthorized`.

Priority: **P1**

---

### REQ-003 — Logout
**As a** logged-in user, **I want to** log out
**so that** my session ends and the next visitor cannot use my account.

Acceptance criteria:
- Logout clears the session and returns the user to the login page.
- After logout, visiting a protected page redirects to login.

Priority: **P2**

---

### REQ-004 — Browse events
**As a** member, **I want to** see a list of upcoming events
**so that** I can decide what to register for.

Acceptance criteria:
- The list shows title, date, location, and remaining capacity for each event.
- The list only shows events after login.

Priority: **P1**

---

### REQ-005 — Search events by title
**As a** member, **I want to** search events by title keyword
**so that** I can find a specific event quickly.

Acceptance criteria:
- Searching `"camp"` returns every event whose title contains "camp".
- Search is case-insensitive (`"CAMP"` and `"camp"` return the same results).
- A search with no matches shows an empty-state message, not an error.

Priority: **P2**

---

### REQ-006 — Filter events by date
**As a** member, **I want to** filter events by date
**so that** I only see events on a day I'm available.

Acceptance criteria:
- Filtering by a date returns only events on that date.
- Filtering by a date with no events shows an empty-state message.

Priority: **P2**

---

### REQ-007 — View event detail
**As a** member, **I want to** open an event's detail page
**so that** I can read its full description before registering.

Acceptance criteria:
- The detail page shows title, description, date, location, capacity, and remaining seats.
- Opening a non-existent event ID shows a 404 page / `404` API response.

Priority: **P1**

---

### REQ-008 — Admin creates an event
**As an** admin, **I want to** create a new event
**so that** members can discover and register for it.

Acceptance criteria:
- All fields (title, description, date, location, capacity) are required.
- The event date must be in the future; past dates are rejected with a clear message.
- A successfully created event appears immediately in the event list.

Priority: **P1**

---

### REQ-009 — Admin edits an event
**As an** admin, **I want to** edit an event's details
**so that** I can fix mistakes or update information.

Acceptance criteria:
- Editing updates only the fields submitted; other fields are unchanged.
- Editing a non-existent event ID returns `404 Not Found`.
- Existing registrations are preserved after an edit.

Priority: **P2**

---

### REQ-010 — Admin deletes an event
**As an** admin, **I want to** delete an event
**so that** cancelled events no longer appear to members.

Acceptance criteria:
- A deleted event disappears from the event list immediately.
- Deleting an event also removes all of its registrations (no orphan rows).

Priority: **P1**

---

### REQ-011 — Member registers for an event
**As a** member, **I want to** register for an event
**so that** I reserve a seat.

Acceptance criteria:
- Registering succeeds and decreases the remaining-capacity count by one.
- Registering for a full event is rejected with `409 Conflict` and a clear message.
- Registering twice for the same event is rejected with `409 Conflict`, not a duplicate row.

Priority: **P1**

---

### REQ-012 — Member cancels a registration
**As a** member, **I want to** cancel my registration
**so that** I free up my seat if my plans change.

Acceptance criteria:
- Cancelling removes the registration row and increases remaining capacity by one.
- Cancelling a registration that doesn't exist returns `404 Not Found`.

Priority: **P1**

---

### REQ-013 — View my registrations
**As a** member, **I want to** see a list of events I've registered for
**so that** I can keep track of my commitments.

Acceptance criteria:
- `GET /api/me/registrations` returns only the current user's registrations.
- The list is empty (not an error) for a member with no registrations.

Priority: **P2**
