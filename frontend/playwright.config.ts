import { defineConfig } from '@playwright/test';

/**
 * E2E gegen den vollen Stack (Keycloak :8081, Backend :8080, Frontend :4200).
 * Voraussetzung: Stack läuft (siehe e2e/README bzw. scripts). Browser: Chromium (headless).
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 90_000,
  expect: { timeout: 15_000 },
  retries: 0,
  reporter: 'line',
  use: {
    baseURL: process.env['E2E_BASE_URL'] ?? 'http://localhost:4200',
    headless: true,
    ignoreHTTPSErrors: true,
    actionTimeout: 15_000,
  },
});
