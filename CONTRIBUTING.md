# Contributing / Mitwirken

Thanks for your interest! Danke für dein Interesse!

## Ground rules / Grundregeln

- Open an issue before larger changes, so we can agree on the approach.
  Vor größeren Änderungen bitte ein Issue öffnen, damit wir den Ansatz abstimmen.
- Keep the test suite green and add tests for new behaviour (TDD welcome).
  Die Testsuite muss grün bleiben; neues Verhalten mit Tests absichern.
- Match the surrounding code style; comments and UI strings are in German.
  Code-Stil der Umgebung übernehmen; Kommentare und UI-Texte sind deutsch.

## Local setup

See the **Development** and **Tests** sections in [`README.md`](README.md).

## Before opening a PR / Vor dem Pull Request

```bash
cd backend  && mvn verify
cd frontend && npx ng test --watch=false && npx ng build
```

- One focused change per PR. Reference the related issue.
  Ein fokussiertes Thema pro PR, zugehöriges Issue verlinken.
- Describe what and why; note any migration or config impact.
  Was und warum beschreiben; Auswirkungen auf Migrationen/Konfiguration nennen.

## Commit messages

Imperative mood, concise subject, body explaining the why where helpful.
Imperativ, knapper Betreff, Begründung im Body wo sinnvoll.

## License of contributions

By contributing you agree that your contributions are licensed under the project's
[MIT License](LICENSE). Mit deinem Beitrag stimmst du der MIT-Lizenzierung zu.
