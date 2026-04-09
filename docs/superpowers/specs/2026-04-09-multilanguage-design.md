# Multilanguage Support — Design Spec

## Purpose

Support multiple languages (starting with English and German) by using separate H2 database files per language, selected at app startup.

## Startup Flow

1. App launches and shows a modal "Select Language" dialog with two buttons: **English** / **German**
2. User picks one, dialog closes
3. Main app opens connected to the selected language's database
4. Title bar reflects the language: "EVA 2.0" for English, "EVA 2.0 [DE]" for German

## Database Paths

- English default: `~/English/eva/eva` (current behavior, unchanged)
- German default: `~/German/eva/eva`
- Overridable via system properties: `-Deva.db.english=<path>` and `-Deva.db.german=<path>`

## Architecture

### New classes
- **`Language` enum** (`com.english.eva.domain`) — `ENGLISH("EN", "EVA 2.0", "~/English/eva/eva")`, `GERMAN("DE", "EVA 2.0 [DE]", "~/German/eva/eva")`. Each value holds: code, app title, default db path. A method to resolve the actual db path checks the system property first, falls back to the default.
- **`LanguageDialog`** (`com.english.eva.ui.frame`) — small modal JDialog shown before the main frame. Displays "Select Language" with two buttons. Returns the selected `Language`. Blocks until a selection is made.

### Modified classes
- **`App.main()`** — show `LanguageDialog` first, use selected language's db path and title. Pass title to `ApplicationFrame`.
- **`ApplicationFrame`** — accept title string parameter instead of hardcoded "English Vocabulary Assistant 2.0".

### Unchanged
- `DatabaseConfig` — already accepts any path, no changes needed
- Schema, migrations, domain model — identical for both languages
- All UI panels (Vocabulary, Meanings, Word Gym) — no changes

## Future extensibility

Adding a new language = adding a new enum value to `Language` and a button in `LanguageDialog`. No other changes needed.
