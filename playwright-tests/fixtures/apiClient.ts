// Creates and tears down test data through the real API, same reasoning as
// qa-tests/.../support/TestDataClient.java: independent tests, no shared
// fixtures, and the setup code exercises the same endpoints the API suite does.

const apiBaseUrl = process.env.API_BASE_URL ?? 'http://localhost:8080/api';

export async function login(email: string, password: string): Promise<string> {
  const res = await fetch(`${apiBaseUrl}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  const body = await res.json();
  return body.token;
}

export function uniqueTitle(prefix: string): string {
  return `${prefix} ${Math.random().toString(36).slice(2, 10)}`;
}

export async function createEvent(
  adminToken: string,
  title: string,
  eventDate: string,
  location: string,
  capacity: number,
): Promise<number> {
  const res = await fetch(`${apiBaseUrl}/events`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${adminToken}` },
    body: JSON.stringify({ title, description: 'Created by Playwright tests.', eventDate, location, capacity }),
  });
  const body = await res.json();
  return body.id;
}

export async function deleteEvent(adminToken: string, eventId: number): Promise<void> {
  await fetch(`${apiBaseUrl}/events/${eventId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${adminToken}` },
  });
}

export async function registerForEvent(memberToken: string, eventId: number): Promise<void> {
  await fetch(`${apiBaseUrl}/events/${eventId}/registrations`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${memberToken}` },
  });
}

export function daysFromNowIso(days: number): string {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString().split('T')[0];
}
