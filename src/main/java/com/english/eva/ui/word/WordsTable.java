package com.english.eva.ui.word;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JTable;

import com.english.eva.domain.Word;
import com.english.eva.service.WordService;

public class WordsTable extends JTable {

    private final WordService wordService;
    private SortingDetails sortingDetails = new SortingDetails();
    private WordTableModel wordTableModel;
    private List<Word> words;

    public WordsTable(WordService wordService) {
        this.wordService = wordService;
        this.words = wordService.getAll();
        wordTableModel = new WordTableModel(words);
        setModel(wordTableModel);
        setDefaultRenderer(Object.class, new WordTableCellRenderer());
        initColumnModel();
        getTableHeader().setDefaultRenderer(
                new WordsTableHeaderRenderer(getTableHeader().getDefaultRenderer()));
        getTableHeader().addMouseListener(new WordsHeaderClickListener(this));
    }

    public void setTableClickListener(TableClickListener listener) {
        addMouseListener(listener);
    }

    private void initColumnModel() {
        getColumnModel().getColumn(WordTableModel.COLUMN_FREQUENCY).setPreferredWidth(90);
        getColumnModel().getColumn(WordTableModel.COLUMN_FREQUENCY).setMaxWidth(120);
        getColumnModel().getColumn(WordTableModel.COLUMN_PROGRESS).setPreferredWidth(110);
        getColumnModel().getColumn(WordTableModel.COLUMN_PROGRESS).setMaxWidth(110);
    }

    public void sortData() {
        var comparator = getSortingComparator();
        if (Objects.nonNull(comparator)) {
            words = words.stream().sorted(comparator).toList();
            wordTableModel.setData(words);
            initColumnModel();
        }
    }

    private Comparator<Word> getSortingComparator() {
        Comparator<Word> comparator = null;
        if ("Word".equals(sortingDetails.getColumnName())) {
            comparator = Comparator.comparing(Word::getText, String.CASE_INSENSITIVE_ORDER);
        } else if ("Frequency".equals(sortingDetails.getColumnName())) {
            comparator = Comparator.comparing(Word::getFrequency, Comparator.naturalOrder());
        }
        if (Objects.isNull(comparator)) {
            return null;
        }
        if ("desc".equals(sortingDetails.getDirection())) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    public SortingDetails getSortingDetails() {
        return sortingDetails;
    }

    public void setSortingDetails(SortingDetails sortingDetails) {
        this.sortingDetails = sortingDetails;
    }

    public WordTableModel getWordTableModel() {
        return wordTableModel;
    }

    public void reloadTable() {
        var wordsIds = words.stream().map(Word::getId).collect(Collectors.toSet());
        this.words = wordService.getByWordIds(wordsIds).stream().sorted().toList();
        wordTableModel.setData(words);
        initColumnModel();
    }

    public void reloadTable(Word word) {
        var wordsIds = words.stream().map(Word::getId).collect(Collectors.toSet());
        var currentWords = new java.util.ArrayList<>(wordService.getByWordIds(wordsIds));
        currentWords.add(word);
        this.words = currentWords.stream().sorted().toList();
        wordTableModel.setData(words);
        initColumnModel();
    }

    public void reloadTable(List<Word> words) {
        this.words = words.stream().sorted().toList();
        wordTableModel.setData(this.words);
        initColumnModel();
    }
}
