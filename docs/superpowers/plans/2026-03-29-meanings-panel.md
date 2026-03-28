# Meanings Panel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a "Meanings" tab that lets users browse, filter, and inspect meanings directly — without needing to find the parent word first.

**Architecture:** New `MeaningsPanel` (between Vocabulary and Word Gym) follows the same patterns as `VocabularyPanel`: settings panel at top with `Consumer` callback, split pane below (30% table / 70% tree). Extends `MeaningSearchParams` to support multi-select levels/statuses and text search, updates `MeaningRepository.search()` accordingly.

**Tech Stack:** Java 21, Swing + MigLayout + FlatLaf, jOOQ (no codegen), JUnit 5 + AssertJ + Mockito

---

## File Map

### Modified files
| File | Change |
|------|--------|
| `src/main/java/com/english/eva/model/MeaningSearchParams.java` | Add `text` field, change single-value level/status/pos to `Set<>` for multi-select |
| `src/main/java/com/english/eva/repository/MeaningRepository.java:108-128` | Update `search()` to handle text LIKE, multi-value IN filters |
| `src/main/java/com/english/eva/ui/meaning/MeaningTree.java` | Add `showMeaning(Meaning)` method for single-meaning display |
| `src/main/java/com/english/eva/ui/frame/ApplicationFrame.java:26-28` | Add Meanings tab between Vocabulary and Word Gym |
| `src/test/java/com/english/eva/repository/MeaningRepositoryTest.java` | Add search tests |

### New files
| File | Purpose |
|------|---------|
| `src/main/java/com/english/eva/ui/meaning/MeaningDto.java` | Table row DTO from Meaning domain object |
| `src/main/java/com/english/eva/ui/meaning/MeaningsTableModel.java` | AbstractTableModel for meanings table (4 columns) |
| `src/main/java/com/english/eva/ui/meaning/MeaningsTable.java` | JTable wrapper with reload/renderer setup |
| `src/main/java/com/english/eva/ui/meaning/MeaningsTableCellRenderer.java` | Colored cells for level/status columns |
| `src/main/java/com/english/eva/ui/meaning/MeaningsTableClickListener.java` | Left-click loads detail, right-click for status/pos updates |
| `src/main/java/com/english/eva/ui/settings/MeaningSettingsPanel.java` | Two-row filter panel (search + level/status/pos filters) |
| `src/main/java/com/english/eva/ui/panel/MeaningsPanel.java` | Orchestrator: settings + split pane with table + tree |

---

### Task 1: Extend MeaningSearchParams for multi-select filters

**Files:**
- Modify: `src/main/java/com/english/eva/model/MeaningSearchParams.java`

- [ ] **Step 1: Rewrite MeaningSearchParams to support text + multi-select**

Replace the entire file content with:

```java
package com.english.eva.model;

import java.util.Set;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeaningSearchParams {

    private String text;
    private Set<ProficiencyLevel> proficiencyLevels;
    private Set<LearningStatus> learningStatuses;
    private PartOfSpeech partOfSpeech;

    public MeaningSearchParams(LearningStatus learningStatus, PartOfSpeech partOfSpeech,
                                ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevels = learningStatus != null ? null : Set.of();
        this.learningStatuses = learningStatus != null ? Set.of(learningStatus) : Set.of();
        this.partOfSpeech = partOfSpeech;
        this.proficiencyLevels = proficiencyLevel != null ? Set.of(proficiencyLevel) : Set.of();
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS (the old single-value constructor is preserved for backward compatibility, though it may not be called anywhere now)

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/english/eva/model/MeaningSearchParams.java
git commit -m "refactor: extend MeaningSearchParams for multi-select filters"
```

---

### Task 2: Update MeaningRepository.search() for new params

**Files:**
- Modify: `src/main/java/com/english/eva/repository/MeaningRepository.java:108-128`
- Test: `src/test/java/com/english/eva/repository/MeaningRepositoryTest.java`

- [ ] **Step 1: Write failing tests for the updated search**

Add these tests to `MeaningRepositoryTest.java` after the existing `saveBatch_insertsMultipleMeanings` test (before the `createAndSaveMeaning` helper):

```java
@Test
void search_byText_filtersByTarget() {
    createAndSaveMeaning("carry out", PartOfSpeech.PHRASAL_VERB);
    createAndSaveMeaning("carry on", PartOfSpeech.PHRASAL_VERB);
    createAndSaveMeaning("give up", PartOfSpeech.PHRASAL_VERB);

    var params = new MeaningSearchParams();
    params.setText("carry");

    var results = meaningRepository.search(params);

    assertThat(results).hasSize(2);
    assertThat(results).extracting(Meaning::getTarget)
            .containsExactlyInAnyOrder("carry out", "carry on");
}

@Test
void search_byMultipleLevels_filtersCorrectly() {
    createAndSaveMeaningWithLevel("word1", ProficiencyLevel.A1);
    createAndSaveMeaningWithLevel("word2", ProficiencyLevel.B2);
    createAndSaveMeaningWithLevel("word3", ProficiencyLevel.C1);

    var params = new MeaningSearchParams();
    params.setProficiencyLevels(Set.of(ProficiencyLevel.A1, ProficiencyLevel.B2));

    var results = meaningRepository.search(params);

    assertThat(results).hasSize(2);
    assertThat(results).extracting(Meaning::getProficiencyLevel)
            .containsExactlyInAnyOrder(ProficiencyLevel.A1, ProficiencyLevel.B2);
}

@Test
void search_byMultipleStatuses_filtersCorrectly() {
    createAndSaveMeaningWithStatus("known-word", LearningStatus.KNOWN);
    createAndSaveMeaningWithStatus("learning-word", LearningStatus.LEARNING);
    createAndSaveMeaningWithStatus("putoff-word", LearningStatus.PUT_OFF);

    var params = new MeaningSearchParams();
    params.setLearningStatuses(Set.of(LearningStatus.KNOWN, LearningStatus.LEARNING));

    var results = meaningRepository.search(params);

    assertThat(results).hasSize(2);
    assertThat(results).extracting(Meaning::getLearningStatus)
            .containsExactlyInAnyOrder(LearningStatus.KNOWN, LearningStatus.LEARNING);
}

@Test
void search_byPartOfSpeech_filtersCorrectly() {
    createAndSaveMeaning("carry out", PartOfSpeech.PHRASAL_VERB);
    createAndSaveMeaning("a trial", PartOfSpeech.NOUN);

    var params = new MeaningSearchParams();
    params.setPartOfSpeech(PartOfSpeech.PHRASAL_VERB);

    var results = meaningRepository.search(params);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getTarget()).isEqualTo("carry out");
}

@Test
void search_combinedFilters_narrowsResults() {
    createAndSaveMeaning("carry out", PartOfSpeech.PHRASAL_VERB);
    createAndSaveMeaning("carry on", PartOfSpeech.PHRASAL_VERB);
    createAndSaveMeaning("a trial", PartOfSpeech.NOUN);

    var params = new MeaningSearchParams();
    params.setText("carry");
    params.setPartOfSpeech(PartOfSpeech.PHRASAL_VERB);

    var results = meaningRepository.search(params);

    assertThat(results).hasSize(2);
}
```

Also add these two helper methods alongside the existing `createAndSaveMeaning`:

```java
private Meaning createAndSaveMeaningWithLevel(String target, ProficiencyLevel level) {
    var meaning = new Meaning();
    meaning.setWordId(1L);
    meaning.setTarget(target);
    meaning.setPartOfSpeech(PartOfSpeech.NOUN);
    meaning.setProficiencyLevel(level);
    meaning.setMeaningSource(MeaningSource.CAMBRIDGE_DICTIONARY);
    meaning.setLearningStatus(LearningStatus.LEARNING);
    meaning.setDateCreated(LocalDateTime.now());
    meaning.setLastModified(LocalDateTime.now());
    return meaningRepository.save(meaning);
}

private Meaning createAndSaveMeaningWithStatus(String target, LearningStatus status) {
    var meaning = new Meaning();
    meaning.setWordId(1L);
    meaning.setTarget(target);
    meaning.setPartOfSpeech(PartOfSpeech.NOUN);
    meaning.setProficiencyLevel(ProficiencyLevel.B1);
    meaning.setMeaningSource(MeaningSource.CAMBRIDGE_DICTIONARY);
    meaning.setLearningStatus(status);
    meaning.setDateCreated(LocalDateTime.now());
    meaning.setLastModified(LocalDateTime.now());
    return meaningRepository.save(meaning);
}
```

Add these imports to the test file:

```java
import java.util.Set;
import com.english.eva.model.MeaningSearchParams;
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest=MeaningRepositoryTest -q`
Expected: FAIL — the old `search()` method uses `getLearningStatus()` (single value) which no longer exists on the updated `MeaningSearchParams`.

- [ ] **Step 3: Update MeaningRepository.search() to handle new params**

Replace the `search` method in `MeaningRepository.java` (lines 108-128) with:

```java
public List<Meaning> search(MeaningSearchParams params) {
    Condition condition = DSL.noCondition();
    if (Objects.nonNull(params.getText()) && !params.getText().isBlank()) {
        condition = condition.and(field("target").likeIgnoreCase("%" + params.getText() + "%"));
    }
    if (Objects.nonNull(params.getLearningStatuses()) && !params.getLearningStatuses().isEmpty()) {
        condition = condition.and(field("learning_status")
                .in(params.getLearningStatuses().stream().map(Enum::name).toList()));
    }
    if (Objects.nonNull(params.getPartOfSpeech())) {
        condition = condition.and(field("part_of_speech").eq(params.getPartOfSpeech().name()));
    }
    if (Objects.nonNull(params.getProficiencyLevels()) && !params.getProficiencyLevels().isEmpty()) {
        condition = condition.and(field("proficiency_level")
                .in(params.getProficiencyLevels().stream().map(Enum::name).toList()));
    }
    var meanings = dsl.selectFrom(table("meaning"))
            .where(condition)
            .fetch(this::mapToMeaning);
    var meaningIds = meanings.stream().map(Meaning::getId).collect(Collectors.toSet());
    var examplesByMeaningId = exampleRepository.findByMeaningIds(meaningIds);
    for (var meaning : meanings) {
        meaning.setExamples(examplesByMeaningId.getOrDefault(meaning.getId(), List.of()));
    }
    return meanings;
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest=MeaningRepositoryTest -q`
Expected: All tests PASS (both old and new)

- [ ] **Step 5: Run all tests to check nothing is broken**

Run: `./mvnw test -q`
Expected: All tests PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/english/eva/repository/MeaningRepository.java \
       src/test/java/com/english/eva/repository/MeaningRepositoryTest.java
git commit -m "feat: update meaning search to support text, multi-level, multi-status filters"
```

---

### Task 3: Add showMeaning() to MeaningTree

**Files:**
- Modify: `src/main/java/com/english/eva/ui/meaning/MeaningTree.java`

- [ ] **Step 1: Add showMeaning(Meaning) method**

Add this method to `MeaningTree.java` after the `showSelectedUserObjectTree()` method (after line 95):

```java
public void showMeaning(Meaning meaning) {
    setRootVisible(false);
    var root = new DefaultMutableTreeNode(meaning.getTarget());

    var sourceNode = new DefaultMutableTreeNode(
            MeaningNode.source(meaning.getMeaningSource() != null
                    ? meaning.getMeaningSource().getLabel() : "Unknown"));
    root.add(sourceNode);

    var posNode = new DefaultMutableTreeNode(
            MeaningNode.partOfSpeech(meaning.getPartOfSpeech() != null
                    ? meaning.getPartOfSpeech().getLabel() : "Unknown"));
    sourceNode.add(posNode);

    var meaningNode = new DefaultMutableTreeNode(
            MeaningNode.meaning(meaning.getId(), meaning.getTarget(), meaning.getLearningStatus()));
    posNode.add(meaningNode);

    var descNode = new DefaultMutableTreeNode(
            MeaningNode.description(meaning.getProficiencyLevel(), meaning.getDescription()));
    meaningNode.add(descNode);

    if (!meaning.getExamples().isEmpty()) {
        var examplesNode = new DefaultMutableTreeNode(MeaningNode.examplesHeader());
        for (var example : meaning.getExamples()) {
            examplesNode.add(new DefaultMutableTreeNode(MeaningNode.example(example.getText())));
        }
        meaningNode.add(examplesNode);
    }

    setModel(new DefaultTreeModel(root));
    expandTree();
    setVisible(true);
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/english/eva/ui/meaning/MeaningTree.java
git commit -m "feat: add showMeaning() for single-meaning tree display"
```

---

### Task 4: Create MeaningDto and MeaningsTableModel

**Files:**
- Create: `src/main/java/com/english/eva/ui/meaning/MeaningDto.java`
- Create: `src/main/java/com/english/eva/ui/meaning/MeaningsTableModel.java`

- [ ] **Step 1: Create MeaningDto**

Create `src/main/java/com/english/eva/ui/meaning/MeaningDto.java`:

```java
package com.english.eva.ui.meaning;

import java.util.Objects;

import com.english.eva.domain.Meaning;
import lombok.Getter;

@Getter
public class MeaningDto {

    private final long id;
    private final String target;
    private final String level;
    private final String status;
    private final String partOfSpeech;

    public MeaningDto(Meaning meaning) {
        this.id = meaning.getId();
        this.target = meaning.getTarget();
        this.level = Objects.nonNull(meaning.getProficiencyLevel())
                ? meaning.getProficiencyLevel().name() : "";
        this.status = Objects.nonNull(meaning.getLearningStatus())
                ? meaning.getLearningStatus().getLabel() : "";
        this.partOfSpeech = Objects.nonNull(meaning.getPartOfSpeech())
                ? meaning.getPartOfSpeech().getLabel() : "";
    }
}
```

- [ ] **Step 2: Create MeaningsTableModel**

Create `src/main/java/com/english/eva/ui/meaning/MeaningsTableModel.java`:

```java
package com.english.eva.ui.meaning;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.english.eva.domain.Meaning;

public class MeaningsTableModel extends AbstractTableModel {

    public static final int COLUMN_MEANING = 0;
    public static final int COLUMN_LEVEL = 1;
    public static final int COLUMN_STATUS = 2;
    public static final int COLUMN_PART_OF_SPEECH = 3;

    private static final String[] COLUMN_NAMES = {
            "Meaning", "Level", "Status", "Part of Speech"
    };

    private List<MeaningDto> meaningDtoList = new ArrayList<>();

    public void setData(List<Meaning> meanings) {
        this.meaningDtoList = meanings.stream().map(MeaningDto::new).toList();
        fireTableDataChanged();
    }

    public MeaningDto getMeaningDtoAt(int row) {
        return meaningDtoList.get(row);
    }

    @Override
    public int getRowCount() {
        return meaningDtoList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var dto = meaningDtoList.get(rowIndex);
        return switch (columnIndex) {
            case COLUMN_MEANING -> dto.getTarget();
            case COLUMN_LEVEL -> dto.getLevel();
            case COLUMN_STATUS -> dto.getStatus();
            case COLUMN_PART_OF_SPEECH -> dto.getPartOfSpeech();
            default -> throw new IllegalStateException("Unexpected column: " + columnIndex);
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/english/eva/ui/meaning/MeaningDto.java \
       src/main/java/com/english/eva/ui/meaning/MeaningsTableModel.java
git commit -m "feat: add MeaningDto and MeaningsTableModel"
```

---

### Task 5: Create MeaningsTableCellRenderer

**Files:**
- Create: `src/main/java/com/english/eva/ui/meaning/MeaningsTableCellRenderer.java`

- [ ] **Step 1: Create the renderer**

Create `src/main/java/com/english/eva/ui/meaning/MeaningsTableCellRenderer.java`:

```java
package com.english.eva.ui.meaning;

import static com.english.eva.ui.util.ColorUtils.LEARNING_COLOURS;
import static com.english.eva.ui.util.ColorUtils.LEVEL_COLOURS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class MeaningsTableCellRenderer extends JPanel implements TableCellRenderer {

    private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

    public MeaningsTableCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        var renderer = defaultRenderer.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        setBackground(isSelected ? Color.LIGHT_GRAY : table.getBackground());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
        var font = renderer.getFont();
        var stringValue = String.valueOf(value);
        removeAll();

        if (column == MeaningsTableModel.COLUMN_MEANING) {
            var label = new JLabel(" " + stringValue);
            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            add(label, BorderLayout.CENTER);
        } else if (column == MeaningsTableModel.COLUMN_LEVEL) {
            var label = new JLabel(stringValue);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize() - 2));
            if (!stringValue.isBlank() && LEVEL_COLOURS.containsKey(stringValue)) {
                label.setOpaque(true);
                label.setBackground(LEVEL_COLOURS.get(stringValue));
                label.setForeground(Color.WHITE);
                label.setBorder(new EmptyBorder(2, 3, 0, 3));
            }
            add(label, BorderLayout.CENTER);
        } else if (column == MeaningsTableModel.COLUMN_STATUS) {
            var label = new JLabel(stringValue);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            if (!stringValue.isBlank() && LEARNING_COLOURS.containsKey(stringValue)) {
                label.setOpaque(true);
                label.setBackground(LEARNING_COLOURS.get(stringValue));
            }
            label.setBorder(new EmptyBorder(3, 3, 3, 3));
            add(label, BorderLayout.CENTER);
        } else {
            var label = new JLabel(stringValue);
            label.setHorizontalAlignment(JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
        return this;
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/english/eva/ui/meaning/MeaningsTableCellRenderer.java
git commit -m "feat: add MeaningsTableCellRenderer with colored level/status cells"
```

---

### Task 6: Create MeaningsTable

**Files:**
- Create: `src/main/java/com/english/eva/ui/meaning/MeaningsTable.java`

- [ ] **Step 1: Create MeaningsTable**

Create `src/main/java/com/english/eva/ui/meaning/MeaningsTable.java`:

```java
package com.english.eva.ui.meaning;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import com.english.eva.domain.Meaning;

public class MeaningsTable extends JTable {

    private MeaningsTableModel meaningsTableModel;
    private List<Meaning> meanings = new ArrayList<>();

    public MeaningsTable() {
        meaningsTableModel = new MeaningsTableModel();
        setModel(meaningsTableModel);
        setDefaultRenderer(Object.class, new MeaningsTableCellRenderer());
        initColumnModel();
    }

    private void initColumnModel() {
        getColumnModel().getColumn(MeaningsTableModel.COLUMN_LEVEL).setPreferredWidth(50);
        getColumnModel().getColumn(MeaningsTableModel.COLUMN_LEVEL).setMaxWidth(60);
        getColumnModel().getColumn(MeaningsTableModel.COLUMN_STATUS).setPreferredWidth(80);
        getColumnModel().getColumn(MeaningsTableModel.COLUMN_STATUS).setMaxWidth(100);
        getColumnModel().getColumn(MeaningsTableModel.COLUMN_PART_OF_SPEECH).setPreferredWidth(110);
        getColumnModel().getColumn(MeaningsTableModel.COLUMN_PART_OF_SPEECH).setMaxWidth(130);
    }

    public void reloadTable(List<Meaning> meanings) {
        this.meanings = meanings;
        meaningsTableModel.setData(meanings);
        initColumnModel();
    }

    public MeaningsTableModel getMeaningsTableModel() {
        return meaningsTableModel;
    }

    public List<Meaning> getMeanings() {
        return meanings;
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/english/eva/ui/meaning/MeaningsTable.java
git commit -m "feat: add MeaningsTable component"
```

---

### Task 7: Create MeaningsTableClickListener

**Files:**
- Create: `src/main/java/com/english/eva/ui/meaning/MeaningsTableClickListener.java`

- [ ] **Step 1: Create the click listener**

Create `src/main/java/com/english/eva/ui/meaning/MeaningsTableClickListener.java`:

```java
package com.english.eva.ui.meaning;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.service.MeaningService;
import com.english.eva.ui.util.UiUtils;

public class MeaningsTableClickListener extends MouseAdapter {

    private final MeaningsTable meaningsTable;
    private final MeaningTree meaningTree;
    private final MeaningService meaningService;
    private final Runnable onRefresh;

    public MeaningsTableClickListener(MeaningsTable meaningsTable, MeaningTree meaningTree,
                                       MeaningService meaningService, Runnable onRefresh) {
        this.meaningsTable = meaningsTable;
        this.meaningTree = meaningTree;
        this.meaningService = meaningService;
        this.onRefresh = onRefresh;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        int row = meaningsTable.rowAtPoint(event.getPoint());
        if (row < 0) return;

        meaningsTable.setRowSelectionInterval(row, row);
        var dto = meaningsTable.getMeaningsTableModel().getMeaningDtoAt(row);

        if (SwingUtilities.isLeftMouseButton(event)) {
            meaningTree.showLoading();
            UiUtils.runInBackground(
                    () -> meaningService.getById(dto.getId()),
                    result -> result.ifPresent(meaningTree::showMeaning)
            );
        } else if (SwingUtilities.isRightMouseButton(event)) {
            showPopupMenu(event, dto.getId());
        }
    }

    private void showPopupMenu(MouseEvent event, long meaningId) {
        var popupMenu = new JPopupMenu();

        Arrays.stream(LearningStatus.values())
                .map(status -> {
                    var item = new JMenuItem(status.getLabel());
                    item.addActionListener(e -> {
                        meaningService.updateLearningStatus(meaningId, status);
                        refreshAfterUpdate(meaningId);
                    });
                    return item;
                })
                .forEach(popupMenu::add);

        var posMenu = new JMenu("Part of Speech");
        Arrays.stream(PartOfSpeech.values())
                .map(pos -> {
                    var item = new JMenuItem(pos.getLabel());
                    item.addActionListener(e -> {
                        meaningService.updatePartOfSpeech(meaningId, pos);
                        refreshAfterUpdate(meaningId);
                    });
                    return item;
                })
                .forEach(posMenu::add);

        popupMenu.addSeparator();
        popupMenu.add(posMenu);
        popupMenu.show(event.getComponent(), event.getX(), event.getY());
    }

    private void refreshAfterUpdate(long meaningId) {
        onRefresh.run();
        meaningService.getById(meaningId).ifPresent(meaningTree::showMeaning);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/english/eva/ui/meaning/MeaningsTableClickListener.java
git commit -m "feat: add MeaningsTableClickListener with right-click status/pos updates"
```

---

### Task 8: Create MeaningSettingsPanel

**Files:**
- Create: `src/main/java/com/english/eva/ui/settings/MeaningSettingsPanel.java`

- [ ] **Step 1: Create the two-row settings panel**

Create `src/main/java/com/english/eva/ui/settings/MeaningSettingsPanel.java`:

```java
package com.english.eva.ui.settings;

import static com.english.eva.ui.util.ColorUtils.LEARNING_COLOURS;
import static com.english.eva.ui.util.ColorUtils.LEVEL_COLOURS;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.ProficiencyLevel;
import com.english.eva.model.MeaningSearchParams;

public class MeaningSettingsPanel extends JPanel {

    private final Consumer<MeaningSearchParams> onSearch;
    private JTextField searchField;
    private JButton searchButton;
    private List<JToggleButton> levelButtons;
    private List<JToggleButton> statusButtons;
    private JComboBox<String> partOfSpeechCombo;
    private JPanel levelsPanel;
    private JPanel statusPanel;
    private JPanel posPanel;

    public MeaningSettingsPanel(Consumer<MeaningSearchParams> onSearch) {
        this.onSearch = onSearch;

        setBorder(new TitledBorder("Settings"));
        var groupLayout = new GroupLayout(this);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);
        setLayout(groupLayout);

        initSearchField();
        initSearchButton();
        initLevelsBar();
        initStatusBar();
        initPartOfSpeechCombo();

        var filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.X_AXIS));
        filtersPanel.add(levelsPanel);
        filtersPanel.add(statusPanel);
        filtersPanel.add(posPanel);

        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(searchField)
                        .addComponent(searchButton))
                .addComponent(filtersPanel));

        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(BASELINE)
                        .addComponent(searchField)
                        .addComponent(searchButton))
                .addComponent(filtersPanel));
    }

    private void initSearchField() {
        searchField = new JTextField("Search...", 20);
        searchField.setForeground(Color.GRAY);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeSearch();
                }
            }
        });
        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if ("Search...".equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Search...");
                }
            }
        });
    }

    private void initSearchButton() {
        searchButton = new JButton("Search");
        searchButton.addActionListener(event -> executeSearch());
    }

    private void initLevelsBar() {
        levelButtons = List.of(
                new JToggleButton(ProficiencyLevel.A1.name()),
                new JToggleButton(ProficiencyLevel.A2.name()),
                new JToggleButton(ProficiencyLevel.B1.name()),
                new JToggleButton(ProficiencyLevel.B2.name()),
                new JToggleButton(ProficiencyLevel.C1.name()),
                new JToggleButton(ProficiencyLevel.C2.name()),
                new JToggleButton(ProficiencyLevel.J7.name())
        );
        var toggleAll = new JToggleButton("Select All");

        levelButtons.forEach(btn -> {
            btn.setForeground(Color.WHITE);
            btn.setBackground(LEVEL_COLOURS.get(btn.getText()));
            var font = btn.getFont();
            btn.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            btn.addActionListener(e -> {
                btn.setForeground(Color.WHITE);
                btn.setBackground(btn.isSelected()
                        ? new Color(0, 0, 0, 0)
                        : LEVEL_COLOURS.get(btn.getText()));
            });
        });

        toggleAll.addActionListener(event -> {
            var selected = ((AbstractButton) event.getSource()).getModel().isSelected();
            levelButtons.forEach(btn -> {
                btn.setSelected(selected);
                btn.setBackground(btn.isSelected()
                        ? new Color(0, 0, 0, 0)
                        : LEVEL_COLOURS.get(btn.getText()));
            });
        });

        levelsPanel = new JPanel();
        levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.X_AXIS));
        levelButtons.forEach(levelsPanel::add);
        levelsPanel.add(toggleAll);
    }

    private void initStatusBar() {
        statusButtons = List.of(
                new JToggleButton(LearningStatus.KNOWN.getLabel()),
                new JToggleButton(LearningStatus.LEARNT.getLabel()),
                new JToggleButton(LearningStatus.LEARNING.getLabel()),
                new JToggleButton(LearningStatus.PUT_OFF.getLabel()),
                new JToggleButton(LearningStatus.UNDEFINED.getLabel())
        );
        var toggleAll = new JToggleButton("Select All");

        statusButtons.forEach(toggle -> {
            toggle.setBackground(LEARNING_COLOURS.get(toggle.getText()));
            var font = toggle.getFont();
            toggle.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            toggle.addActionListener(event ->
                    toggle.setBackground(toggle.isSelected()
                            ? new Color(0, 0, 0, 0)
                            : LEARNING_COLOURS.get(toggle.getText())));
        });
        toggleAll.addActionListener(event ->
                statusButtons.forEach(btn -> {
                    btn.setSelected(toggleAll.isSelected());
                    btn.setBackground(btn.isSelected()
                            ? new Color(0, 0, 0, 0)
                            : LEARNING_COLOURS.get(btn.getText()));
                }));

        statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusButtons.forEach(statusPanel::add);
        statusPanel.add(toggleAll);
    }

    private void initPartOfSpeechCombo() {
        var items = new String[PartOfSpeech.values().length + 1];
        items[0] = "All";
        for (int i = 0; i < PartOfSpeech.values().length; i++) {
            items[i + 1] = PartOfSpeech.values()[i].getLabel();
        }
        partOfSpeechCombo = new JComboBox<>(new DefaultComboBoxModel<>(items));

        posPanel = new JPanel();
        posPanel.setLayout(new BoxLayout(posPanel, BoxLayout.X_AXIS));
        posPanel.add(partOfSpeechCombo);
    }

    private void executeSearch() {
        var searchText = searchField.getText();
        String text = "Search...".equals(searchText) ? null : searchText;

        Set<ProficiencyLevel> selectedLevels = levelButtons.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .map(ProficiencyLevel::valueOf)
                .collect(Collectors.toSet());

        Set<LearningStatus> selectedStatuses = statusButtons.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .map(LearningStatus::findByLabel)
                .collect(Collectors.toSet());

        var selectedPos = (String) partOfSpeechCombo.getSelectedItem();
        PartOfSpeech partOfSpeech = "All".equals(selectedPos) ? null : PartOfSpeech.findByLabel(selectedPos);

        onSearch.accept(new MeaningSearchParams(text, selectedLevels, selectedStatuses, partOfSpeech));
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/english/eva/ui/settings/MeaningSettingsPanel.java
git commit -m "feat: add MeaningSettingsPanel with two-row search + filters layout"
```

---

### Task 9: Create MeaningsPanel and wire into ApplicationFrame

**Files:**
- Create: `src/main/java/com/english/eva/ui/panel/MeaningsPanel.java`
- Modify: `src/main/java/com/english/eva/ui/frame/ApplicationFrame.java`

- [ ] **Step 1: Create MeaningsPanel**

Create `src/main/java/com/english/eva/ui/panel/MeaningsPanel.java`:

```java
package com.english.eva.ui.panel;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.english.eva.service.MeaningService;
import com.english.eva.ui.meaning.MeaningTree;
import com.english.eva.ui.meaning.MeaningsTable;
import com.english.eva.ui.meaning.MeaningsTableClickListener;
import com.english.eva.ui.settings.MeaningSettingsPanel;
import com.english.eva.ui.util.UiUtils;
import net.miginfocom.swing.MigLayout;

public class MeaningsPanel extends JPanel {

    public MeaningsPanel(MeaningService meaningService) {
        setLayout(new MigLayout("fill"));
        setPreferredSize(new Dimension(600, 600));

        var meaningTree = new MeaningTree();
        var meaningsTable = new MeaningsTable();

        var settingsPanel = new MeaningSettingsPanel(searchParams ->
                UiUtils.runInBackground(
                        () -> meaningService.search(searchParams),
                        meaningsTable::reloadTable
                )
        );

        var clickListener = new MeaningsTableClickListener(
                meaningsTable, meaningTree, meaningService,
                () -> settingsPanel.triggerSearch()
        );
        meaningsTable.addMouseListener(clickListener);

        var mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.3);
        mainSplitPane.setLeftComponent(new JScrollPane(meaningsTable));
        mainSplitPane.setRightComponent(new JScrollPane(meaningTree));

        add(settingsPanel, "cell 0 0, pushx");
        add(mainSplitPane, "cell 0 1, push, grow");
    }
}
```

- [ ] **Step 2: Add triggerSearch() to MeaningSettingsPanel**

Add this public method at the end of `MeaningSettingsPanel.java` (before the closing `}`):

```java
public void triggerSearch() {
    executeSearch();
}
```

- [ ] **Step 3: Wire MeaningsPanel into ApplicationFrame**

In `src/main/java/com/english/eva/ui/frame/ApplicationFrame.java`, replace lines 26-27:

```java
        rootTab.addTab("Vocabulary", new VocabularyPanel(wordService, meaningService, exampleRepository));
        rootTab.addTab("Word Gym", new WordGymPanel(meaningService));
```

with:

```java
        rootTab.addTab("Vocabulary", new VocabularyPanel(wordService, meaningService, exampleRepository));
        rootTab.addTab("Meanings", new MeaningsPanel(meaningService));
        rootTab.addTab("Word Gym", new WordGymPanel(meaningService));
```

Add this import to `ApplicationFrame.java`:

```java
import com.english.eva.ui.panel.MeaningsPanel;
```

- [ ] **Step 4: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Run all tests**

Run: `./mvnw test -q`
Expected: All tests PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/english/eva/ui/panel/MeaningsPanel.java \
       src/main/java/com/english/eva/ui/settings/MeaningSettingsPanel.java \
       src/main/java/com/english/eva/ui/frame/ApplicationFrame.java
git commit -m "feat: add Meanings panel tab with search, filter, and meaning detail view"
```

---

### Task 10: Manual smoke test

- [ ] **Step 1: Build and run the app**

```bash
./mvnw compile dependency:copy-dependencies -DoutputDirectory=target/dependency -q
java -cp "target/classes:target/dependency/*" com.english.eva.App
```

- [ ] **Step 2: Verify**

1. Three tabs visible: Vocabulary, **Meanings**, Word Gym
2. Click Meanings tab — settings panel with search field + button on top row, Level / Status / Part of Speech on bottom row
3. Click Search with no filters — all meanings load in the table (columns: Meaning, Level, Status, Part of Speech)
4. Click a table row — right side shows single meaning tree with Source > Part of Speech > Target > Description > Examples
5. Right-click a row — popup with Learning Status options + Part of Speech submenu
6. Select a status — table refreshes, tree updates
7. Type in search field, select "Phrasal verb" from dropdown, click Search — table shows filtered results
8. Select some Level and Status toggles, click Search — results narrow as expected

- [ ] **Step 3: Final commit if any fixes needed**
