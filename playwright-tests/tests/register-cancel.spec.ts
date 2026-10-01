import { test, expect } from '@playwright/test';
import { EventDetailPage } from '../pages/EventDetailPage';
import { createEvent, deleteEvent, login, registerForEvent, uniqueTitle, daysFromNowIso } from '../fixtures/apiClient';

test.describe('register and cancel', () => {
  let adminToken: string;
  let eventId: number;

  test.afterEach(async () => {
    await deleteEvent(adminToken, eventId);
  });

  test('registering decreases remaining seats and swaps the button to Cancel', async ({ page }) => {
    adminToken = await login('avery.admin@igniters.org', 'Passw0rd!');
    eventId = await createEvent(
      adminToken,
      uniqueTitle('Playwright Register Event'),
      daysFromNowIso(10),
      'PW Hall',
      1,
    );

    const detailPage = new EventDetailPage(page);
    await detailPage.open(eventId);
    await detailPage.register();

    await expect(detailPage.remainingSeats()).toContainText('0');
    await expect(detailPage.cancelButton()).toBeVisible();
  });

  test('registering for a full event shows an error', async ({ page }) => {
    adminToken = await login('avery.admin@igniters.org', 'Passw0rd!');
    eventId = await createEvent(
      adminToken,
      uniqueTitle('Playwright Full Event'),
      daysFromNowIso(10),
      'PW Hall',
      1,
    );
    // Fill the only seat with a different member before this test's own member tries.
    const otherMemberToken = await login('casey.member@igniters.org', 'Passw0rd!');
    await registerForEvent(otherMemberToken, eventId);

    const detailPage = new EventDetailPage(page);
    await detailPage.open(eventId);
    await detailPage.register();

    await expect(detailPage.errorMessage()).toBeVisible();
    await expect(detailPage.registerButton()).toBeVisible();
  });
});
