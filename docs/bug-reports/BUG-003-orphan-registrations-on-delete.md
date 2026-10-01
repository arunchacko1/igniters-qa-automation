# BUG-003: Deleting an event leaves orphaned registration rows

- **Severity:** High — corrupts data; orphaned rows reference a deleted event.
- **Priority:** P1
- **Environment:** `QA_DEFECTS_ENABLED=true`, local/CI, any browser or direct API call.
- **Linked test case:** TC-041 (`OrphanRegistrationTest#delete_event_leaves_no_orphans`)

## Steps to Reproduce
1. Set `QA_DEFECTS_ENABLED=true` and restart the SUT.
2. As admin, create an event.
3. As a member, register for it.
4. As admin, `DELETE /api/events/{id}`.
5. Query the database: `SELECT r.id FROM registrations r LEFT JOIN events e ON r.event_id = e.id WHERE e.id IS NULL;`

## Expected Result
Step 4 removes the event's registrations along with the event itself. The
query in step 5 returns no rows.

## Actual Result
The event row is deleted successfully (`204 No Content`), but its
registration row remains, now pointing at an event id that no longer
exists. The query in step 5 returns that row.

## Evidence
```
$ curl -X DELETE .../api/events/13 ...
204
$ docker compose exec db psql -U igniters -d igniters_qa -c "
    SELECT count(*) FROM registrations r LEFT JOIN events e ON r.event_id = e.id WHERE e.id IS NULL;"
 count
-------
     2    -- expected 0
```

## Root Cause
`EventService.deleteEvent()` skips the `registrationRepository.deleteByEventId(id)`
cleanup call when the defect flag is enabled. Because `registrations.event_id`
has no database-level foreign key (see `docs/decisions.md` #9), nothing else
stops the orphan from being created.

## Fix
Set `QA_DEFECTS_ENABLED=false` (the default). The correct path deletes
dependent registrations before deleting the event, inside the same
transaction.
