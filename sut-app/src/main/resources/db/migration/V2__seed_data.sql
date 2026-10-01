-- Seed data: 2 admins, 5 members, 10 events.
-- Every seeded user shares the password "Passw0rd!" — this is a local/CI
-- fixture, never a real credential, so one shared bcrypt hash keeps the
-- file readable. See README "Seeded test accounts" for the full list.
INSERT INTO users (name, email, password_hash, role) VALUES
    ('Avery Admin',   'avery.admin@igniters.org',   '$2b$10$kr7H4nIOCWNdBnf0Ckeduu86cRLMHiqBtdxSt5k6NwOouvkViaqfe', 'ADMIN'),
    ('Riley Admin',   'riley.admin@igniters.org',    '$2b$10$kr7H4nIOCWNdBnf0Ckeduu86cRLMHiqBtdxSt5k6NwOouvkViaqfe', 'ADMIN'),
    ('Jordan Member', 'jordan.member@igniters.org',  '$2b$10$kr7H4nIOCWNdBnf0Ckeduu86cRLMHiqBtdxSt5k6NwOouvkViaqfe', 'MEMBER'),
    ('Casey Member',  'casey.member@igniters.org',   '$2b$10$kr7H4nIOCWNdBnf0Ckeduu86cRLMHiqBtdxSt5k6NwOouvkViaqfe', 'MEMBER'),
    ('Drew Member',   'drew.member@igniters.org',    '$2b$10$kr7H4nIOCWNdBnf0Ckeduu86cRLMHiqBtdxSt5k6NwOouvkViaqfe', 'MEMBER'),
    ('Sky Member',    'sky.member@igniters.org',     '$2b$10$kr7H4nIOCWNdBnf0Ckeduu86cRLMHiqBtdxSt5k6NwOouvkViaqfe', 'MEMBER'),
    ('Rowan Member',  'rowan.member@igniters.org',   '$2b$10$kr7H4nIOCWNdBnf0Ckeduu86cRLMHiqBtdxSt5k6NwOouvkViaqfe', 'MEMBER');

-- Event dates are relative to insert time (CURRENT_DATE + N) so the seed
-- data never "expires" and tests never need updating just because real
-- calendar time moved on.
INSERT INTO events (title, description, event_date, location, capacity) VALUES
    ('Summer Camp Kickoff',       'Opening weekend for summer camp with games and a bonfire.',          CURRENT_DATE + INTERVAL '7 day',   'Pine Lake Campground',      40),
    ('Food Drive Service Day',    'Pack and deliver food boxes for the community pantry.',              CURRENT_DATE + INTERVAL '10 day',  'Igniters Main Hall',        25),
    ('Worship Night',              'An evening of music and worship open to all members.',                CURRENT_DATE + INTERVAL '3 day',   'Igniters Main Hall',        100),
    ('Hiking Trip: Eagle Ridge',  'Day hike with a packed lunch at the summit.',                        CURRENT_DATE + INTERVAL '14 day',  'Eagle Ridge Trailhead',      15),
    ('Game Night',                 'Board games and pizza — bring a friend.',                             CURRENT_DATE + INTERVAL '5 day',   'Igniters Youth Room',       30),
    ('Car Wash Fundraiser',       'Fundraiser car wash for the winter retreat.',                        CURRENT_DATE + INTERVAL '12 day',  'Church Parking Lot',        20),
    ('Movie Night',                'Family-friendly movie and snacks.',                                   CURRENT_DATE + INTERVAL '6 day',   'Igniters Main Hall',        50),
    ('Beach Cleanup',              'Volunteer morning cleaning up the shoreline.',                       CURRENT_DATE + INTERVAL '9 day',   'Sunset Beach',               35),
    ('Winter Retreat Info Night', 'Info session and sign-ups for the winter retreat.',                  CURRENT_DATE + INTERVAL '2 day',   'Igniters Main Hall',        60),
    ('Talent Show',                'Members showcase music, comedy, and art.',                            CURRENT_DATE + INTERVAL '21 day',  'Igniters Main Hall',        80);

-- A few starter registrations so the UI and API have something to show
-- out of the box (member registering/cancelling is still exercised by tests
-- against events they create, to keep tests independent of this seed data).
INSERT INTO registrations (user_id, event_id)
SELECT u.id, e.id
FROM users u, events e
WHERE u.email = 'jordan.member@igniters.org' AND e.title = 'Worship Night';

INSERT INTO registrations (user_id, event_id)
SELECT u.id, e.id
FROM users u, events e
WHERE u.email = 'casey.member@igniters.org' AND e.title = 'Worship Night';
