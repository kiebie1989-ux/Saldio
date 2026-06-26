import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Mandant {
  id: number;
  name: string;
  status: string;
  imEinzelbericht?: boolean;
  inKumulierung?: boolean;
  imFinalbericht?: boolean;
  typ?: string;
  bemerkung?: string;
  datevMandantennr?: string;
  datevBeraternr?: string;
}

export interface MandantEingabe {
  name: string;
  status: string;
  imEinzelbericht: boolean;
  inKumulierung: boolean;
  imFinalbericht: boolean;
  typ: string;
  bemerkung: string;
  datevMandantennr: string;
  datevBeraternr: string;
}

export interface Konto {
  id: number;
  skr03: string;
  skr04: string;
  bezeichnung: string;
  bwaGruppe: string;
  guvBilanzPosition: string;
  kontenklasse: string;
  vorzeichen: string;
  aktiv: boolean;
}

export interface MitarbeiterDto {
  personalnummer: string;
  name: string;
  mandant: string;
  kostenstelle: string;
  team: string;
  monatslohn: number;
  stundenProMonat: number;
  euroProStunde: number;
  agAnteil: number;
  gesamtkosten: number;
}

export interface Einstellung {
  schluessel: string;
  wert: string;
  beschreibung: string;
}

export interface ImportErgebnis {
  id: number;
  dateiname: string;
  quelle: string;
  importiertAm: string;
  zeilenGesamt: number;
  zeilenOk: number;
  zeilenWarnung: number;
  status: string;
}

export interface DashboardBericht {
  mandant: string;
  jahr: number;
  kpis: {
    umsatzYtd: number;
    rohertragYtd: number;
    ebitYtd: number;
    ebitMarge: number;
    mitarbeiter: number;
  };
  monatsreihe: { monat: string; umsatz: number; rohertrag: number; ebit: number }[];
}

export type Ampel = 'GRUEN' | 'GELB' | 'ROT' | 'NEUTRAL';

export interface Kennzahl {
  name: string;
  wert: number;
  einheit: string;
  zielwert: number | null;
  ampel: Ampel;
  richtung: string;
  interpretation: string;
}

export interface BereichsAnalyse {
  bereich: string;
  ampel: Ampel;
  bewertung: string;
  massnahme: string;
  analyse: string;
}

export interface Abschnitt {
  titel: string;
  text: string;
}

export interface Mandantenbericht {
  mandant: string;
  jahr: number;
  quelle: string;
  bereiche: BereichsAnalyse[];
  managementkommentar: Abschnitt[];
}

export interface StrukturZeile {
  position: string;
  art: 'WERT' | 'SUMME' | 'PRUEFUNG';
  monate: Record<string, number>;
  ytd: number;
}

export interface StrukturBericht {
  mandant: string;
  jahr: number;
  monate: string[];
  zeilen: StrukturZeile[];
}

export interface KostenartZeile {
  monat: string;
  umsatz: number;
  wareneinsatz: number;
  weQuote: number;
  personal: number;
  persQuote: number;
  sonstige: number;
  sonsQuote: number;
  gesamtkosten: number;
  gesamtkostenquote: number;
}

export interface KostenstelleZeile {
  kostenstelle: string;
  personalkosten: number;
  anteil: number;
}

export interface KostenstrukturBericht {
  mandant: string;
  jahr: number;
  kostenarten: KostenartZeile[];
  kostenstellen: KostenstelleZeile[];
}

export interface MandantKennzahl {
  mandant: string;
  umsatz: number;
  rohertrag: number;
  rohertragsquote: number;
  ebit: number;
  ebitMarge: number;
}

export interface KumuliertBericht {
  modus: string;
  jahr: number;
  mandanten: MandantKennzahl[];
  summe: MandantKennzahl;
}

export interface BenutzerDto {
  sub: string;
  benutzername: string;
  alleMandanten: boolean;
  mandanten: string[];
}

export type Modus = 'EINZELN' | 'KUMULIERT' | 'FINAL';
export type Szenario = 'PESSIMISTISCH' | 'BASIS' | 'OPTIMISTISCH';

export interface PlanZeile {
  monat: string;
  typ: 'IST' | 'PLAN';
  umsatz: number;
  rohertrag: number;
  ebit: number;
}

export interface PlanungBericht {
  mandant: string;
  jahr: number;
  bisMonat: number;
  szenario: string;
  zeilen: PlanZeile[];
  jahresprognose: { umsatz: number; rohertrag: number; ebit: number };
}

/** Zentraler Zugriff auf die Auswertungs-Endpunkte des Backends. */
@Injectable({ providedIn: 'root' })
export class BwaApiService {
  private readonly http = inject(HttpClient);

  getMandanten(): Observable<Mandant[]> {
    return this.http.get<Mandant[]>('/api/mandanten');
  }

  getKontenrahmen(): Observable<Konto[]> {
    return this.http.get<Konto[]>('/api/kontenrahmen');
  }

  createMandant(eingabe: MandantEingabe): Observable<Mandant> {
    return this.http.post<Mandant>('/api/mandanten', eingabe);
  }

  updateMandant(id: number, eingabe: MandantEingabe): Observable<Mandant> {
    return this.http.put<Mandant>(`/api/mandanten/${id}`, eingabe);
  }

  stornoImport(id: number): Observable<ImportErgebnis> {
    return this.http.post<ImportErgebnis>(`/api/import/${id}/storno`, {});
  }

  getMitarbeiter(): Observable<MitarbeiterDto[]> {
    return this.http.get<MitarbeiterDto[]>('/api/mitarbeiter');
  }

  getEinstellungen(): Observable<Einstellung[]> {
    return this.http.get<Einstellung[]>('/api/einstellungen');
  }

  updateEinstellung(schluessel: string, wert: string): Observable<Einstellung> {
    return this.http.put<Einstellung>('/api/einstellungen', { schluessel, wert });
  }

  getImportHistorie(): Observable<ImportErgebnis[]> {
    return this.http.get<ImportErgebnis[]>('/api/import');
  }

  importiere(typ: 'csv' | 'extf', datei: File): Observable<ImportErgebnis> {
    const form = new FormData();
    form.append('typ', typ);
    form.append('datei', datei);
    return this.http.post<ImportErgebnis>('/api/import', form);
  }

  getDashboard(mandant: string, jahr: number): Observable<DashboardBericht> {
    return this.http.get<DashboardBericht>('/api/dashboard', { params: this.params(mandant, jahr) });
  }

  getKennzahlen(mandant: string, jahr: number): Observable<Kennzahl[]> {
    return this.http.get<Kennzahl[]>('/api/kennzahlen', { params: this.params(mandant, jahr) });
  }

  getGuv(mandant: string, jahr: number): Observable<StrukturBericht> {
    return this.http.get<StrukturBericht>('/api/guv', { params: this.params(mandant, jahr) });
  }

  getBilanz(mandant: string, jahr: number): Observable<StrukturBericht> {
    return this.http.get<StrukturBericht>('/api/bilanz', { params: this.params(mandant, jahr) });
  }

  getMandantenbericht(mandant: string, jahr: number): Observable<Mandantenbericht> {
    return this.http.get<Mandantenbericht>('/api/mandantenbericht', { params: this.params(mandant, jahr) });
  }

  pdfUrl(mandant: string, jahr: number): string {
    return `/api/mandantenbericht/pdf?${this.params(mandant, jahr).toString()}`;
  }

  getKostenstruktur(mandant: string, jahr: number): Observable<KostenstrukturBericht> {
    return this.http.get<KostenstrukturBericht>('/api/kostenstruktur', { params: this.params(mandant, jahr) });
  }

  getKumuliert(modus: Modus, jahr: number): Observable<KumuliertBericht> {
    return this.http.get<KumuliertBericht>('/api/kumuliert', {
      params: new HttpParams().set('modus', modus).set('jahr', jahr),
    });
  }

  getPlanung(mandant: string, jahr: number, bisMonat: number, szenario: Szenario): Observable<PlanungBericht> {
    return this.http.get<PlanungBericht>('/api/planung', {
      params: this.params(mandant, jahr).set('bisMonat', bisMonat).set('szenario', szenario),
    });
  }

  getBenutzer(): Observable<BenutzerDto[]> {
    return this.http.get<BenutzerDto[]>('/api/benutzer');
  }

  updateBenutzer(sub: string, alleMandanten: boolean, mandanten: string[]): Observable<BenutzerDto> {
    return this.http.put<BenutzerDto>('/api/benutzer', { sub, alleMandanten, mandanten });
  }

  private params(mandant: string, jahr: number): HttpParams {
    return new HttpParams().set('mandant', mandant).set('jahr', jahr);
  }
}
