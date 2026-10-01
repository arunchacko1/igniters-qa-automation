import { Page } from '@playwright/test';

export class EventDetailPage {
  constructor(private readonly page: Page) {}

  async open(eventId: number | string) {
    await this.page.goto(`/events/${eventId}`);
  }

  title() {
    return this.page.getByTestId('event-detail-title');
  }

  remainingSeats() {
    return this.page.getByTestId('event-detail-remaining');
  }

  async register() {
    await this.page.getByTestId('register-button').click();
  }

  async cancelRegistration() {
    await this.page.getByTestId('cancel-registration-button').click();
  }

  registerButton() {
    return this.page.getByTestId('register-button');
  }

  cancelButton() {
    return this.page.getByTestId('cancel-registration-button');
  }

  errorMessage() {
    return this.page.getByTestId('error-message');
  }
}
