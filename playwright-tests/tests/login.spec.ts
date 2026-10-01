import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

// Runs in the 'anonymous-flows' project (see playwright.config.ts) — no
// pre-authenticated storageState, since these tests need the real login form.

test('valid credentials log the member in and show the event list', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.open();
  await loginPage.loginAs('jordan.member@igniters.org', 'Passw0rd!');

  await expect(page).toHaveURL(/\/events$/);
});

test('wrong password shows an error and does not log in', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.open();
  await loginPage.loginAs('jordan.member@igniters.org', 'not-the-password');

  await expect(loginPage.errorMessage()).toBeVisible();
  await expect(page).toHaveURL(/\/login/);
});
