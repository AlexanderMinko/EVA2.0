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
