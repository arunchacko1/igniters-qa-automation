# SQL Cheatsheet

The actual queries used in `qa-tests/.../db`, with a short explanation of
each. All run over a plain JDBC `Connection` — no ORM — so they see exactly
what's on disk.

### Find a row by its primary key
```sql
SELECT title, location, capacity, event_date FROM events WHERE id = ?;
```
The basic "does this row exist with these values" check, used after every
create/update through the API to confirm the write actually landed.

### Count related rows
```sql
SELECT count(*) FROM registrations WHERE event_id = ?;
```
Used to compute "remaining capacity" independently of the application code's
own computation (`capacity - count(registrations)`), and to confirm a
registration survived an unrelated edit to its event.

### Find orphaned rows with a LEFT JOIN
```sql
SELECT r.id
FROM registrations r
LEFT JOIN events e ON r.event_id = e.id
WHERE e.id IS NULL;
```
The core referential-integrity check. A `LEFT JOIN` keeps every row from
`registrations` even when there's no matching `events` row — the `WHERE e.id
IS NULL` filters down to exactly the registrations whose event no longer
exists. Because `registrations.event_id` has no database-level foreign key
(see `docs/decisions.md` #9), this query is the only thing that actually
catches that failure mode.

### Confirm a unique constraint by trying to violate it
```sql
INSERT INTO registrations (user_id, event_id) VALUES (?, ?);
-- run again with the same (user_id, event_id) --
```
The second insert throws a `SQLException` with SQLSTATE `23505`
(`unique_violation`). Asserting on that specific SQLSTATE — not just "it
threw" — is what proves the *constraint* rejected it, rather than some
unrelated connection problem.

### Insert and get the generated id back
```sql
INSERT INTO registrations (user_id, event_id) VALUES (?, ?) RETURNING id;
```
Postgres' `RETURNING` clause avoids a second round-trip (`SELECT
lastval()`/`currval()`) to find out what id was just assigned — used when a
test needs to manufacture a row directly (bypassing the API) to test a query
like the orphan-finder above.

### Look up a seeded user's id by email
```sql
SELECT id FROM users WHERE email = ?;
```
The seed data and the API never expose a user's numeric id directly — tests
that need it (e.g. to build a registration row by hand) resolve it this way.
