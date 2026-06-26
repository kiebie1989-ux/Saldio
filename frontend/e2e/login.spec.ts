import { test, expect, Page } from '@playwright/test';

/**
 * Meldet als Admin über die echte Keycloak-Loginmaske an und wartet, bis die App-Shell steht.
 */
async function loginAlsAdmin(page: Page): Promise<void> {
  await page.goto('/');
  // angular-oauth2-oidc leitet unangemeldet zur Keycloak-Loginmaske
  await page.waitForURL(/:8081\/realms\/bwa/, { timeout: 30_000 });
  await page.fill('#username', 'admin');
  await page.fill('#password', 'admin');
  await page.click('#kc-login');
  // Zurück in der App (port-unabhängig: auf die App-Shell warten)
  await expect(page.locator('.app-title')).toContainText('DATEV-BWA Controlling', { timeout: 30_000 });
}

/**
 * End-to-End: echter OIDC-Login über Keycloak.
 * Nach Anmeldung als Admin ist das Dashboard sichtbar und die admin-only Navigation vorhanden.
 */
test('Admin-Login über Keycloak führt zum Dashboard mit Admin-Navigation', async ({ page }) => {
  await loginAlsAdmin(page);
  await expect(page.locator('.benutzer')).toContainText('admin');

  // Navigation inkl. admin-only Eintrag
  await expect(page.getByRole('link', { name: 'Dashboard' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Benutzer & Zugriff' })).toBeVisible();
});

/**
 * End-to-End: nach Login zu geschützten Berichtsseiten navigieren und sicherstellen,
 * dass die Routen geladen werden und ihre Inhalte rendern (Auth-Guard + Lazy-Routes + API-Calls).
 */
test('Nach Login lassen sich Berichtsseiten öffnen und rendern Inhalt', async ({ page }) => {
  await loginAlsAdmin(page);

  // Kennzahlen-Seite
  await page.getByRole('link', { name: 'Kennzahlen' }).click();
  await expect(page).toHaveURL(/\/kennzahlen$/);
  await expect(page.locator('h1')).toContainText('Kennzahlen');
  // Mandantenauswahl ist befüllt (Stammdaten über die API geladen)
  await expect(page.locator('mat-select').first()).toBeVisible();

  // GuV-Seite
  await page.getByRole('link', { name: 'GuV' }).click();
  await expect(page).toHaveURL(/\/guv$/);
  await expect(page.locator('h1')).toContainText('Gewinn- und Verlustrechnung');
});
