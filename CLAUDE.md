# CLAUDE.md

## Project Overview

EVA 2.0 (English Vocabulary Assistant) is a desktop Swing application for managing and learning English vocabulary. Rewritten from Spring Boot + Hibernate to plain Java 21 + jOOQ + H2 + Flyway.

## Build & Run Commands

```bash
./mvnw clean compile                                    # Compile
./mvnw test                                              # Run all tests
./mvnw test -Dtest=TestClass                             # Run single test class
./mvnw compile dependency:copy-dependencies -DoutputDirectory=target/dependency  # Build with deps
java -cp "target/classes:target/dependency/*" com.english.eva.App               # Run app
```

**Requirements:** Java 21+, Maven 3+

## Tech Stack

- **Java 21** — no Lombok, uses records for DTOs
- **jOOQ** (open source, no codegen) — typesafe SQL with manual `DSL.table()`/`DSL.field()` references
- **H2 2.1.214** — file-based embedded database (version pinned for backward compatibility with existing data)
- **Flyway** — schema migrations (`src/main/resources/db/migration/`)
- **Swing + FlatLaf + MigLayout + SwingX** — UI
- **SLF4J + Logback** — logging
- **JUnit 5 + AssertJ + Mockito** — testing

## Architecture

**No framework DI** — manual wiring in `App.main()`. All dependencies passed via constructors.

### Layer structure (all under `com.english.eva`):

- **`domain/`** — Plain POJOs: `Word` (has `List<Meaning>`), `Meaning` (has `List<Example>`, holds `wordId` as Long, not object ref), `Example` (holds `meaningId`). Enums: `PartOfSpeech`, `ProficiencyLevel` (A1-C2 + J7), `LearningStatus`, `MeaningSource`.
- **`model/`** — `SearchParams` (record), `MeaningSearchParams` for search/filter criteria.
- **`repository/`** — jOOQ-based DAOs. `ExampleRepository` -> `MeaningRepository` -> `WordRepository`. Use `r.get("COLUMN_NAME", Type.class)` for `selectFrom` results and `r.get(0, Type.class)` for `returningResult` fields.
- **`service/`** — `WordService` (has LRU meaning cache, 200 entries), `MeaningService`. Services use `MeaningService.setWordService()` for cache invalidation (circular dep resolved via setter).
- **`config/`** — `DatabaseConfig`: creates H2 DataSource + Flyway migrations + jOOQ DSLContext. Supports `mem:` prefix for in-memory test databases.
- **`ui/`** — Swing UI layer:
  - `frame/ApplicationFrame` — main JFrame with tabbed pane
  - `panel/VocabularyPanel` — word table + meaning tree with settings
  - `panel/WordGymPanel` — vocabulary quiz game
  - `word/` — table components (`WordsTable`, `WordTableModel`, `WordDto`, renderers, listeners)
  - `meaning/` — tree components (`MeaningTree`, `MeaningNode` with typed `NodeType` enum, renderer, listener)
  - `settings/` — `SettingsPanel` (search/filter), `StatisticPanel` (word/meaning counts)
  - `util/ColorUtils` — color maps for levels/statuses, `UiUtils` — SwingWorker helper

### Key patterns

- **Constructor injection everywhere** — no static setters, no service locator.
- **Callback-based UI communication** — `Consumer`/`Runnable` callbacks between panels.
- **Explicit transactions** — `dsl.transaction(ctx -> { ... })` in services.
- **Bulk loading** — `findByWordIds()` and `findByMeaningIds()` prevent N+1 queries. All list queries load meanings+examples in 3 total queries.
- **Background loading** — `UiUtils.runInBackground()` wraps DB calls off the EDT.
- **LRU cache** — `WordService.meaningCache` caches word+meanings, evicted on mutations.
- **MeaningNode** — typed node object (not encoded strings) for meaning tree. Uses `NodeType` enum: SOURCE, PART_OF_SPEECH, MEANING, DESCRIPTION, EXAMPLES_HEADER, EXAMPLE.

## Database

H2 file-based at `~/English/eva/eva`. Schema managed by Flyway. Baseline migration: `V1__baseline_schema.sql`.

Tables: `words`, `meaning`, `example`. Enums stored as STRING in VARCHAR columns.

**Important:** H2 version is pinned to 2.1.214 for compatibility with existing database files. Do not upgrade without migrating the data file.

## Testing

- **Repository tests** — integration tests against in-memory H2 (`TestDatabaseConfig.create()`)
- **Service tests** — unit tests with Mockito mocks (requires `-Dnet.bytebuddy.experimental=true` in surefire config)
- No UI tests

## jOOQ Conventions

- No codegen — use `DSL.table("tablename")` and `DSL.field("columnname")`
- For `selectFrom()` results, access columns as: `r.get("COLUMN_NAME", Type.class)` (H2 uppercases)
- For `returningResult()` results, access by index: `r.get(0, Long.class)`
- For `selectDistinct` with joins, alias columns: `field("words.id").as("ID")`
