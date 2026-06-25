import { Routes } from '@angular/router';
import { authGuard, rolleGuard } from './core/auth.guard';
import { Rolle } from './core/auth.service';

/** Navigationsstruktur — spiegelt die Excel-Blätter als Feature-Routen, mit Mindestrolle. */
export interface NavItem {
  path: string;
  label: string;
  icon: string;
  rolle: Rolle;
}

export const NAV_ITEMS: NavItem[] = [
  { path: 'dashboard', label: 'Dashboard', icon: 'dashboard', rolle: 'leser' },
  { path: 'kennzahlen', label: 'Kennzahlen', icon: 'speed', rolle: 'leser' },
  { path: 'guv', label: 'GuV', icon: 'request_quote', rolle: 'leser' },
  { path: 'bilanz', label: 'Bilanz', icon: 'account_balance', rolle: 'leser' },
  { path: 'mandantenbericht', label: 'Mandantenbericht', icon: 'description', rolle: 'leser' },
  { path: 'ki-auswertung', label: 'KI-Auswertung', icon: 'smart_toy', rolle: 'leser' },
  { path: 'kumuliert', label: 'Kumuliert / Final', icon: 'group_work', rolle: 'leser' },
  { path: 'planung', label: 'Planung', icon: 'event', rolle: 'leser' },
  { path: 'kostenstruktur', label: 'Kostenstruktur', icon: 'pie_chart', rolle: 'leser' },
  { path: 'stammdaten', label: 'Stammdaten', icon: 'table_chart', rolle: 'leser' },
  { path: 'import', label: 'Import', icon: 'upload_file', rolle: 'bearbeiter' },
  { path: 'einstellungen', label: 'Einstellungen', icon: 'settings', rolle: 'admin' },
  { path: 'benutzer', label: 'Benutzer & Zugriff', icon: 'manage_accounts', rolle: 'admin' },
];

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
  },
  {
    path: 'kennzahlen',
    canActivate: [authGuard],
    loadComponent: () => import('./features/kennzahlen/kennzahlen').then((m) => m.Kennzahlen),
  },
  {
    path: 'guv',
    canActivate: [authGuard],
    loadComponent: () => import('./features/guv/guv').then((m) => m.Guv),
  },
  {
    path: 'bilanz',
    canActivate: [authGuard],
    loadComponent: () => import('./features/bilanz/bilanz').then((m) => m.Bilanz),
  },
  {
    path: 'mandantenbericht',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/mandantenbericht/mandantenbericht').then((m) => m.Mandantenbericht),
  },
  {
    path: 'ki-auswertung',
    canActivate: [authGuard],
    loadComponent: () => import('./features/ki-auswertung/ki-auswertung').then((m) => m.KiAuswertung),
  },
  {
    path: 'kumuliert',
    canActivate: [authGuard],
    loadComponent: () => import('./features/kumuliert/kumuliert').then((m) => m.Kumuliert),
  },
  {
    path: 'planung',
    canActivate: [authGuard],
    loadComponent: () => import('./features/planung/planung').then((m) => m.Planung),
  },
  {
    path: 'kostenstruktur',
    canActivate: [authGuard],
    loadComponent: () => import('./features/kostenstruktur/kostenstruktur').then((m) => m.Kostenstruktur),
  },
  {
    path: 'stammdaten',
    canActivate: [authGuard],
    loadComponent: () => import('./features/stammdaten/stammdaten').then((m) => m.Stammdaten),
  },
  {
    path: 'import',
    canActivate: [authGuard, rolleGuard('bearbeiter')],
    loadComponent: () => import('./features/import/import-seite').then((m) => m.ImportSeite),
  },
  {
    path: 'einstellungen',
    canActivate: [authGuard, rolleGuard('admin')],
    loadComponent: () => import('./features/einstellungen/einstellungen').then((m) => m.Einstellungen),
  },
  {
    path: 'benutzer',
    canActivate: [authGuard, rolleGuard('admin')],
    loadComponent: () => import('./features/benutzer/benutzer').then((m) => m.Benutzer),
  },
  { path: '**', redirectTo: 'dashboard' },
];
