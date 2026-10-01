import { Page } from '@playwright/test';

export class EventFormPage {
  constructor(private readonly page: Page) {}

  async openNew() {
    await this.page.goto('/admin/events/new');
  }

  async fillForm(opts: {
    title: string;
    description: string;
    eventDate: string; // ISO yyyy-MM-dd
    location: string;
    capacity: number;
  }) {
    await this.page.getByTestId('event-title-input').fill(opts.title);
    await this.page.getByTestId('event-description-input').fill(opts.description);
    // Playwright's fill() sets the input value directly (not key-by-key),
    // so native <input type="date"> doesn't have the segmented-sendKeys
    // problem Selenium runs into here.
    await this.page.getByTestId('event-date-input').fill(opts.eventDate);
    await this.page.getByTestId('event-location-input').fill(opts.location);
    await this.page.getByTestId('event-capacity-input').fill(String(opts.capacity));
  }

  async submit() {
    await this.page.getByTestId('event-form-submit').click();
  }
}
