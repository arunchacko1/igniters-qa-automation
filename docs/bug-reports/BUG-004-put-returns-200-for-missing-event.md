# BUG-004: PUT on a nonexistent event id returns 200 instead of 404

- **Severity:** Medium — misleading success response; no data is actually written.
- **Priority:** P2
- **Environment:** `QA_DEFECTS_ENABLED=true`, local/CI, direct API call.
- **Linked test case:** TC-037 (`EventsApiTest#update_nonexistent_event_returns_404`)

## Steps to Reproduce
1. Set `QA_DEFECTS_ENABLED=true` and restart the SUT.
2. `PUT /api/events/999999999` (an id that was never created) with a valid
   body.

## Expected Result
`404 Not Found` with `code: "EVENT_NOT_FOUND"`.

## Actual Result
`200 OK`, with a response body that echoes back the submitted fields as if
the update succeeded. No row exists at that id, before or after the call.

## Evidence
```
$ curl -s -o /dev/null -w "%{http_code}\n" -X PUT .../api/events/999999 \
    -d '{"title":"x","description":"y","eventDate":"2099-01-01","location":"z","capacity":5}'
200   # expected 404
```

## Root Cause
`EventService.updateEvent()`'s `orElseGet` branch fabricates and returns an
`EventResponse` built directly from the request body when the defect flag is
enabled, instead of throwing `NotFoundException`. Nothing is persisted.

## Fix
Set `QA_DEFECTS_ENABLED=false` (the default). The correct path throws
`NotFoundException`, which the global exception handler turns into a 404.
