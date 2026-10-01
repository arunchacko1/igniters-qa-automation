import { test, expect } from '@playwright/test';
import { EventDetailPage } from '../pages/EventDetailPage';
import { createEvent, deleteEvent, login, uniqueTitle, daysFromNowIso } from '../fixtures/apiClient';

test('the detail page shows the event title and full remaining capacity', async ({ page }) => {
  const adminToken = await login('avery.admin@igniters.org', 'Passw0rd!');
  const title = uniqueTitle('Playwright Detail Event');
  const eventId = await createEvent(adminToken, title, daysFromNowIso(10), 'PW Hall', 8);

  try {
    const detailPage = new EventDetailPage(page);
    await detailPage.open(eventId);

    await expect(detailPage.title()).toHaveText(title);
    await expect(detailPage.remainingSeats()).toContainText('8');
  } finally {
    await deleteEvent(adminToken, eventId);
  }
});
