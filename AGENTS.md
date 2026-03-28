# AGENTS.md

## Code Style

- Java 21 features encouraged: records, pattern matching, `var`, switch expressions
- No Lombok — write getters/setters explicitly or use records for immutable DTOs
- No annotations for DI — all wiring is manual in `App.main()`
- Prefer `Optional<T>` over returning null for lookups
- Use SLF4J logging (`LoggerFactory.getLogger(ClassName.class)`)

## Adding New Features

### New domain entity
1. Create plain POJO in `domain/`
2. Add Flyway migration in `src/main/resources/db/migration/V{N}__description.sql`
3. Create repository in `repository/` with jOOQ queries
4. Wire in `App.main()`

### New UI panel
1. Create panel class with constructor injection (services passed as constructor args)
2. Add tab in `ApplicationFrame`
3. Use `UiUtils.runInBackground()` for any DB calls from UI event handlers

### New repository query
- Use `DSL.table("tablename")` and `DSL.field("columnname")`
- For `selectFrom` results: `r.get("COLUMN_NAME", Type.class)` (uppercase)
- For `returningResult` results: `r.get(0, Type.class)` (index-based)
- Always bulk-load child entities (examples for meanings) to avoid N+1

## Testing Conventions

### Repository tests (integration)
- Use `TestDatabaseConfig.create()` for fresh in-memory H2 per test class
- Test real SQL against real H2 — no mocks
- Insert test data via jOOQ DSL in `@BeforeEach`

### Service tests (unit)
- Use `@ExtendWith(MockitoExtension.class)` with `@Mock` and `@InjectMocks`
- Test business logic and delegation, not SQL

## Running

```bash
./mvnw compile dependency:copy-dependencies -DoutputDirectory=target/dependency
java -cp "target/classes:target/dependency/*" com.english.eva.App
```

## Cache Invalidation

`WordService` has an LRU cache for word+meanings. Any mutation that changes meanings must call `wordService.evictCache(wordId)`. Currently handled in:
- `WordService.save()`, `WordService.delete()`
- `MeaningService.save()`, `MeaningService.updateLearningStatus()`, `MeaningService.updatePartOfSpeech()`

If you add new mutation methods, remember to evict.

## H2 Compatibility

The database file was created with H2 2.1.x. The H2 dependency is pinned to `2.1.214`. Do NOT upgrade H2 without first migrating the database file to the new format.
