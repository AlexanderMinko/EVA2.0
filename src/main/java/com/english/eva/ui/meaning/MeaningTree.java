package com.english.eva.ui.meaning;

import java.util.Comparator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.english.eva.domain.Meaning;
import com.english.eva.domain.MeaningSource;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.Word;

public class MeaningTree extends JTree {

    private Word word;

    public MeaningTree() {
        setVisible(false);
        setRootVisible(false);
        setShowsRootHandles(true);
        setCellRenderer(new MeaningTreeCellRenderer());
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public void showSelectedUserObjectTree() {
        setRootVisible(false);
        var root = new DefaultMutableTreeNode(word.getText());
        var meanings = word.getMeanings();
        var sources = meanings.stream().map(Meaning::getMeaningSource).distinct().toList();

        if (sources.isEmpty()) {
            root.setUserObject(MeaningNode.source("Here is no meaning!"));
            setRootVisible(true);
        }

        for (MeaningSource source : sources) {
            var sourceNode = new DefaultMutableTreeNode(MeaningNode.source(source.getLabel()));
            root.add(sourceNode);

            var partsOfSpeechBySource = meanings.stream()
                    .filter(m -> m.getMeaningSource() == source)
                    .map(Meaning::getPartOfSpeech)
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .toList();

            for (PartOfSpeech pos : partsOfSpeechBySource) {
                var posNode = new DefaultMutableTreeNode(MeaningNode.partOfSpeech(pos.getLabel()));

                var meaningsBySourceAndPos = meanings.stream()
                        .filter(m -> m.getMeaningSource() == source)
                        .filter(m -> m.getPartOfSpeech() == pos)
                        .toList();

                for (Meaning meaning : meaningsBySourceAndPos) {
                    var meaningNode = new DefaultMutableTreeNode(
                            MeaningNode.meaning(meaning.getId(), meaning.getTarget(), meaning.getLearningStatus()));

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

                    posNode.add(meaningNode);
                }
                sourceNode.add(posNode);
            }
        }

        setModel(new DefaultTreeModel(root));
        expandTree();
        setVisible(true);
    }

    private void expandTree() {
        for (int i = 0; i < getRowCount(); i++) {
            var path = getPathForRow(i);
            var lastNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (lastNode.getUserObject() instanceof MeaningNode node
                    && node.getType() == MeaningNode.NodeType.EXAMPLES_HEADER) {
                continue;
            }
            expandPath(path);
        }
    }
}
