# BUG-001: Registration allows one member past a full event

- **Severity:** High — breaks a core business rule (no overbooking).
- **Priority:** P1
- **Environment:** `QA_DEFECTS_ENABLED=true`, local/CI, any browser or direct API call.
- **Linked test case:** TC-047 (`RegistrationApiTest#registering_for_full_event_returns_409`)

## Steps to Reproduce
1. Set `QA_DEFECTS_ENABLED=true` and restart the SUT.
2. As an admin, create an event with `capacity=1`.
3. As Member A, `POST /api/events/{id}/registrations`.
4. As Member B (different account), `POST /api/events/{id}/registrations`
   for the same event.

## Expected Result
Step 4 returns `409 Conflict` with `code: "EVENT_FULL"` — the event already
has as many registrations as its capacity.

## Actual Result
Step 4 returns `201 Created`. The event now has 2 registrations against a
capacity of 1.

## Evidence
```
$ curl -s -o /dev/null -w "casey(1st): %{http_code}\n" -X POST .../events/13/registrations -H "Authorization: Bearer $CASEY_TOKEN"
casey(1st): 201
$ curl -s -o /dev/null -w "%{http_code}\n" -X POST .../events/13/registrations -H "Authorization: Bearer $DREW_TOKEN"
201   # expected 409 — event's capacity was already 1 and already full
```

## Root Cause
`RegistrationService.register()` compares `currentCount > event.getCapacity()`
instead of `>=` when the defect flag is enabled — the off-by-one lets exactly
one extra registration through.

## Fix
Set `QA_DEFECTS_ENABLED=false` (the default). The correct comparison
(`currentCount >= event.getCapacity()`) is always used in that mode.
