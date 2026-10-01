import { test, expect } from '@playwright/test';
import { EventListPage } from '../pages/EventListPage';
import { createEvent, deleteEvent, login, uniqueTitle, daysFromNowIso } from '../fixtures/apiClient';

// Runs in the 'member-flows' project — already logged in via storageState.

test.describe('event list', () => {
  let adminToken: string;
  let eventId: number;
  let title: string;

  test.beforeEach(async () => {
    adminToken = await login('avery.admin@igniters.org', 'Passw0rd!');
    title = uniqueTitle('Playwright Browse Event');
    eventId = await createEvent(adminToken, title, daysFromNowIso(10), 'PW Hall', 10);
  });

  test.afterEach(async () => {
    await deleteEvent(adminToken, eventId);
  });

  test('a newly created event appears in the event list', async ({ page }) => {
    const listPage = new EventListPage(page);
    await listPage.open();

    await expect(listPage.eventTitleLinks().filter({ hasText: title })).toBeVisible();
  });

  test('searching by title filters the list down to matching events', async ({ page }) => {
    const listPage = new EventListPage(page);
    await listPage.open();
    await listPage.searchByTitle(title);

    await expect(listPage.eventTitleLinks()).toHaveCount(1);
    await expect(listPage.eventTitleLinks()).toContainText(title);
  });
});
