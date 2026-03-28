# Meanings Panel — Design Spec

## Purpose

Add a new "Meanings" tab (between Vocabulary and Word Gym) that lets users browse and filter meanings directly — without needing to first find the parent word. Primary use case: learning phrasal verbs, idioms, or other categories that span multiple words.

## Layout

Two-row settings panel at the top, split pane below (30/70).

### Settings (top)
- **Row 1:** Search text field + Search button. The search bar width matches the filter row below.
- **Row 2:** Level toggle buttons (A1–C2, J7) | Status toggle buttons (Known, Learnt, Learning, Put off, Undefined) | Part of Speech dropdown (last).

Search triggers on button click or Enter key. Text searches meaning `target` field (LIKE match).

### Left side (30%) — Meanings Table
Table columns:
| Column | Source field | Notes |
|--------|-------------|-------|
| Meaning | `target` | Main display text |
| Level | `proficiencyLevel` | Colored by level |
| Status | `learningStatus` | Colored by status |
| Part of Speech | `partOfSpeech` | Last column |

Click a row → right side shows that meaning's detail tree.

### Right side (70%) — Meaning Detail Tree
Reuses existing `MeaningTree` component with a new `setMeaning(Meaning)` method. Shows a single meaning in the same tree structure: Source > Part of Speech > Target > Description > Examples.

### Right-click on table row
Same popup as existing tree right-click on MEANING nodes:
- Learning Status submenu (KNOWN, LEARNT, LEARNING, PUT_OFF, UNDEFINED)
- Part of Speech submenu (all values)

Updates refresh both table and detail tree.

## Architecture

Follows existing `VocabularyPanel` patterns: constructor injection, `Consumer` callbacks, background loading via `UiUtils.runInBackground()`.

### New classes
- `ui/panel/MeaningsPanel` — orchestrator panel (settings + split pane with table + tree)
- `ui/settings/MeaningSettingsPanel` — two-row filter panel, accepts `Consumer<MeaningSearchParams>`
- `ui/meaning/MeaningsTable` — JTable with MeaningsTableModel
- `ui/meaning/MeaningsTableModel` — AbstractTableModel (4 columns)
- `ui/meaning/MeaningDto` — row DTO built from Meaning domain object
- `ui/meaning/MeaningsTableClickListener` — left-click loads detail, right-click for status/pos updates
- `ui/meaning/MeaningsTableCellRenderer` — colored cells for level/status

### Modified classes
- **`MeaningSearchParams`** — add `String text` field; change `proficiencyLevel` to `Set<ProficiencyLevel>` and `learningStatus` to `Set<LearningStatus>` for multi-select filtering
- **`MeaningRepository.search()`** — update query to support text LIKE, multi-value levels/statuses
- **`MeaningService.search()`** — no logic changes, passes through to repository
- **`MeaningTree`** — add `setMeaning(Meaning meaning)` method for single-meaning display
- **`ApplicationFrame`** — add "Meanings" tab between Vocabulary and Word Gym, pass MeaningService + ExampleRepository

### Data flow
1. User configures filters in MeaningSettingsPanel, clicks Search
2. MeaningSettingsPanel builds MeaningSearchParams, invokes callback
3. Callback: `meaningService.search(params)` → `List<Meaning>` → `MeaningsTable.reloadTable(meanings)`
4. User clicks row → `MeaningsTableClickListener` fires
5. Loads meaning with examples via service → `meaningTree.setMeaning(meaning)`
6. Right-click updates → `meaningService.updateLearningStatus/updatePartOfSpeech` → refresh table + tree

## Testing

- **MeaningRepository integration tests** — verify search with text, multi-level, multi-status filters
- **MeaningService unit tests** — verify search delegation (existing pattern)
- No UI tests (matching project convention)
