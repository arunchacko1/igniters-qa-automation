import { defineConfig, devices } from '@playwright/test';

// The base URL is configurable the same way every other test layer is —
// local docker compose by default, overridden in CI or against staging.
const baseURL = process.env.BASE_URL ?? 'http://localhost:8080';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  retries: process.env.CI ? 1 : 0,
  reporter: [['html', { open: 'never' }]],
  use: {
    baseURL,
    // Capturing a trace only once a test has already failed once (not on
    // every run) keeps local runs fast while still giving CI a trace to
    // attach as an artifact when something actually breaks.
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    // Logs in once per role and saves the session; every other project
    // depends on this instead of re-doing the login UI flow per test.
    { name: 'setup', testMatch: /.*\.setup\.ts/ },
    {
      // login.spec.ts tests the login screen itself, so it needs a clean,
      // unauthenticated context — it can't share the logged-in storageState
      // the other projects use.
      name: 'anonymous-flows',
      use: { ...devices['Desktop Chrome'] },
      testMatch: /login\.spec\.ts/,
    },
    {
      name: 'member-flows',
      use: { ...devices['Desktop Chrome'], storageState: 'playwright/.auth/member.json' },
      dependencies: ['setup'],
      testMatch: /.*\.spec\.ts/,
      testIgnore: [/admin-.*\.spec\.ts/, /login\.spec\.ts/],
    },
    {
      name: 'admin-flows',
      use: { ...devices['Desktop Chrome'], storageState: 'playwright/.auth/admin.json' },
      dependencies: ['setup'],
      testMatch: /admin-.*\.spec\.ts/,
    },
  ],
});
