# BUG-005: The create-event web form accepts a past date the API rejects

- **Severity:** Medium — inconsistent validation between entry points; a past-dated event can be created through the UI.
- **Priority:** P2
- **Environment:** `QA_DEFECTS_ENABLED=true`, local/CI, browser (Thymeleaf form).
- **Linked test case:** TC-029 (`AdminEventCrudTest#create_with_past_date_is_rejected`)

## Steps to Reproduce
1. Set `QA_DEFECTS_ENABLED=true` and restart the SUT.
2. Log in as an admin in the browser, go to **Create Event**.
3. Fill in a valid title/description/location/capacity, but set the date to
   a day in the past. Submit.
4. Separately, `POST /api/events` with the same past date via the API.

## Expected Result
Both step 3 and step 4 reject the past date with the same
`EVENT_DATE_IN_PAST` error — the rule is "an event's date must be in the
future," full stop, regardless of which entry point is used.

## Actual Result
Step 3 succeeds: the event is created and the browser redirects to its
detail page. Step 4 still correctly returns `400 EVENT_DATE_IN_PAST` for the
identical payload.

## Evidence
```
$ curl -s -b cookies.txt -X POST http://localhost:8080/admin/events \
    --data-urlencode "title=Buggy Past Event" --data-urlencode "eventDate=2020-01-01" ...
302 Found
Location: http://localhost:8080/events/15   # created successfully — should have re-shown the form with an error

$ curl -s -X POST http://localhost:8080/api/events \
    -d '{"title":"x","eventDate":"2020-01-01", ...}'
{"code":"EVENT_DATE_IN_PAST","message":"Event date must be in the future."}
```

## Root Cause
`AdminEventPageController.create()` calls
`eventService.createEvent(form.toRequest(), !qaDefects.isEnabled())` — the
`enforceDateRule` flag is only `true` when defects are disabled. The API
controller always passes `true`. With the flag on, only the web path skips
its own validation while the API path never does, producing the mismatch.

## Fix
Set `QA_DEFECTS_ENABLED=false` (the default), which makes the web form
enforce the exact same rule the API does.
