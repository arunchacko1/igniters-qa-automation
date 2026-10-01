import { test, expect } from '@playwright/test';
import { EventFormPage } from '../pages/EventFormPage';
import { EventListPage } from '../pages/EventListPage';
import { login, deleteEvent, uniqueTitle, daysFromNowIso } from '../fixtures/apiClient';

// Runs in the 'admin-flows' project — logged in as admin via storageState.

test('admin can create a new event and it appears on the list', async ({ page }) => {
  const title = uniqueTitle('Playwright Admin Event');
  const formPage = new EventFormPage(page);
  await formPage.openNew();
  await formPage.fillForm({
    title,
    description: 'Created in a Playwright test.',
    eventDate: daysFromNowIso(10),
    location: 'PW Admin Hall',
    capacity: 5,
  });
  await formPage.submit();

  await expect(page).toHaveURL(/\/events\/\d+$/);
  // A successful create redirects to /events/{id} — grab the id before
  // navigating away, so it can be cleaned up afterward.
  const eventId = Number(page.url().match(/\/events\/(\d+)$/)![1]);

  const listPage = new EventListPage(page);
  await listPage.open();
  await expect(listPage.eventTitleLinks().filter({ hasText: title })).toBeVisible();

  const adminToken = await login('avery.admin@igniters.org', 'Passw0rd!');
  await deleteEvent(adminToken, eventId);
});

test('submitting the create form with a past date shows a validation error', async ({ page }) => {
  const formPage = new EventFormPage(page);
  await formPage.openNew();
  await formPage.fillForm({
    title: uniqueTitle('Should Not Be Created'),
    description: 'desc',
    eventDate: '2000-01-01',
    location: 'Hall',
    capacity: 5,
  });
  await formPage.submit();

  await expect(page.getByTestId('error-message')).toBeVisible();
});
