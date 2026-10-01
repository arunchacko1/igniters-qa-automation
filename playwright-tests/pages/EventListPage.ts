import { Page } from '@playwright/test';

export class EventListPage {
  constructor(private readonly page: Page) {}

  async open() {
    await this.page.goto('/events');
  }

  async searchByTitle(query: string) {
    await this.page.getByTestId('search-input').fill(query);
    await this.page.getByTestId('search-submit').click();
    await this.page.waitForURL(/q=/);
  }

  eventTitleLinks() {
    return this.page.getByTestId('event-title-link');
  }

  async clickEventTitled(title: string) {
    await this.eventTitleLinks().filter({ hasText: title }).click();
  }

  emptyState() {
    return this.page.getByTestId('empty-state');
  }

  createEventLink() {
    return this.page.getByTestId('nav-create-event-link');
  }
}
