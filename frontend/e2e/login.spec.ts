import { test, expect } from '@playwright/test';

/**
 * End-to-End: echter OIDC-Login über Keycloak.
 * Die App leitet unangemeldet zu Keycloak; nach Anmeldung als Admin ist das Dashboard sichtbar
 * und die admin-only Navigation ("Benutzer & Zugriff") vorhanden.
 */
test('Admin-Login über Keycloak führt zum Dashboard mit Admin-Navigation', async ({ page }) => {
  await page.goto('/');

  // angular-oauth2-oidc leitet zur Keycloak-Loginmaske
  await page.waitForURL(/:8081\/realms\/bwa/, { timeout: 30_000 });
  await page.fill('#username', 'admin');
  await page.fill('#password', 'admin');
  await page.click('#kc-login');

  // Zurück in der App (port-unabhängig: auf das App-Shell warten)
  await expect(page.locator('.app-title')).toContainText('DATEV-BWA Controlling', { timeout: 30_000 });
  await expect(page.locator('.benutzer')).toContainText('admin');

  // Navigation inkl. admin-only Eintrag
  await expect(page.getByRole('link', { name: 'Dashboard' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Benutzer & Zugriff' })).toBeVisible();
});
