import { test as setup, expect } from '@playwright/test';

// Seeded accounts — mirrors sut-app's V2__seed_data.sql.
const MEMBER_EMAIL = 'jordan.member@igniters.org';
const ADMIN_EMAIL = 'avery.admin@igniters.org';
const PASSWORD = 'Passw0rd!';

const memberAuthFile = 'playwright/.auth/member.json';
const adminAuthFile = 'playwright/.auth/admin.json';

setup('authenticate as member', async ({ page }) => {
  await page.goto('/login');
  await page.getByTestId('login-email').fill(MEMBER_EMAIL);
  await page.getByTestId('login-password').fill(PASSWORD);
  await page.getByTestId('login-submit').click();
  await expect(page.getByTestId('nav-bar')).toBeVisible();
  await page.context().storageState({ path: memberAuthFile });
});

setup('authenticate as admin', async ({ page }) => {
  await page.goto('/login');
  await page.getByTestId('login-email').fill(ADMIN_EMAIL);
  await page.getByTestId('login-password').fill(PASSWORD);
  await page.getByTestId('login-submit').click();
  await expect(page.getByTestId('nav-create-event-link')).toBeVisible();
  await page.context().storageState({ path: adminAuthFile });
});
