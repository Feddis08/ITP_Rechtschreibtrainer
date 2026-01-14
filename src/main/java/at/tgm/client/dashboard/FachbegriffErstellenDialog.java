package at.tgm.client.dashboard;

import at.tgm.objects.FachbegriffItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class FachbegriffErstellenDialog extends JDialog {

    private JTextField wordField;
    private JSpinner levelSpinner;
    private JSpinner pointsSpinner;
    private JTextArea phraseArea;
    private Consumer<FachbegriffItem> onSave;

    public FachbegriffErstellenDialog(JFrame parent, Consumer<FachbegriffItem> onSave) {
        super(parent, "Neue Lernkarte erstellen", true);
        this.onSave = onSave;

        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JPanel formPanel = buildFormPanel();
        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Erstellen");
        saveButton.addActionListener(new SaveActionListener());
        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fachbegriff
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Fachbegriff *:"), gbc);
        gbc.gridx = 1;
        wordField = new JTextField(25);
        panel.add(wordField, gbc);

        // Level
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Level *:"), gbc);
        gbc.gridx = 1;
        levelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        panel.add(levelSpinner, gbc);

        // Punkte
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Punkte *:"), gbc);
        gbc.gridx = 1;
        pointsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        panel.add(pointsSpinner, gbc);

        // Beschreibung
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Beschreibung *:"), gbc);
        gbc.gridx = 1;
        phraseArea = new JTextArea(5, 25);
        phraseArea.setLineWrap(true);
        phraseArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(phraseArea);
        panel.add(scrollPane, gbc);

        return panel;
    }

    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String word = wordField.getText().trim();
            String phrase = phraseArea.getText().trim();
            int level = (Integer) levelSpinner.getValue();
            int points = (Integer) pointsSpinner.getValue();

            if (word.isEmpty()) {
                JOptionPane.showMessageDialog(FachbegriffErstellenDialog.this,
                        "Bitte geben Sie einen Fachbegriff ein!",
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (phrase.isEmpty()) {
                JOptionPane.showMessageDialog(FachbegriffErstellenDialog.this,
                        "Bitte geben Sie eine Beschreibung ein!",
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            FachbegriffItem item = new FachbegriffItem(word, level, points, phrase);
            onSave.accept(item);
            dispose();
        }
    }
}
