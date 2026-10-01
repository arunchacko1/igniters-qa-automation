# BUG-002: Event search is case-sensitive

- **Severity:** Medium — degrades usability, no data integrity impact.
- **Priority:** P2
- **Environment:** `QA_DEFECTS_ENABLED=true`, local/CI, any browser or direct API call.
- **Linked test case:** TC-016/TC-020 (`EventSearchTest#search_is_case_insensitive`)

## Steps to Reproduce
1. Set `QA_DEFECTS_ENABLED=true` and restart the SUT.
2. Note a seeded/created event titled e.g. "Worship Night".
3. `GET /api/events?q=WORSHIP` (or search "WORSHIP" in the UI).

## Expected Result
The event titled "Worship Night" is returned — search should not care about
letter case.

## Actual Result
Zero results. Searching with the exact original casing ("Worship") still
works, but any other casing returns nothing.

## Evidence
```
$ curl -s ".../api/events?q=Worship" ...   # exact case
[{"title":"Worship Night", ...}]
$ curl -s ".../api/events?q=WORSHIP" ...   # different case
[]   # expected the same single-item result as above
```

## Root Cause
`EventService.matchesTitle()` skips `toLowerCase()` on both sides of the
`contains` check when the defect flag is enabled, falling back to a
case-sensitive `String.contains`.

## Fix
Set `QA_DEFECTS_ENABLED=false` (the default). The correct path lower-cases
both the query and the title before comparing.
