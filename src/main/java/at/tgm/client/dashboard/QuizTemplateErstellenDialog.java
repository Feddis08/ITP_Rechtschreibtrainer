package at.tgm.client.dashboard;

import at.tgm.client.ClientNetworkController;
import at.tgm.network.packets.C2SGETAllFachbegriffe;
import at.tgm.network.packets.S2CPOSTAllFachbegriffe;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class QuizTemplateErstellenDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(QuizTemplateErstellenDialog.class);

    private JList<FachbegriffItem> availableList;
    private JList<FachbegriffItem> selectedList;
    private DefaultListModel<FachbegriffItem> availableModel;
    private DefaultListModel<FachbegriffItem> selectedModel;
    private Consumer<Quiz> onSave;
    private JButton addButton;
    private JButton removeButton;
    private JButton saveButton;
    private JTextField nameField;

    public QuizTemplateErstellenDialog(JFrame parent, Consumer<Quiz> onSave) {
        super(parent, "Neues Quiz-Template erstellen", true);
        this.onSave = onSave;

        setLayout(new BorderLayout());
        setSize(700, 550);
        setLocationRelativeTo(parent);

        // Header mit Namensfeld
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        
        JLabel header = new JLabel("Wählen Sie Lernkarten für das Quiz-Template aus:");
        header.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(header, BorderLayout.NORTH);
        
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Name:"));
        nameField = new JTextField(25);
        namePanel.add(nameField);
        headerPanel.add(namePanel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = buildContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Erstellen");
        saveButton.addActionListener(new SaveActionListener());
        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initialisiere Buttons nach vollständiger Erstellung
        updateButtons();

        // Lade verfügbare Fachbegriffe
        loadFachbegriffe();
    }

    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Linke Seite: Verfügbare Fachbegriffe
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Verfügbare Lernkarten"));
        availableModel = new DefaultListModel<>();
        availableList = new JList<>(availableModel);
        availableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        availableList.setCellRenderer(new FachbegriffListCellRenderer());
        JScrollPane leftScroll = new JScrollPane(availableList);
        leftPanel.add(leftScroll, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.WEST);

        // Mitte: Buttons
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());

        addButton = new JButton(">");
        addButton.addActionListener(e -> {
            int[] indices = availableList.getSelectedIndices();
            List<FachbegriffItem> toAdd = new ArrayList<>();
            for (int i : indices) {
                toAdd.add(availableModel.getElementAt(i));
            }
            for (FachbegriffItem item : toAdd) {
                selectedModel.addElement(item);
                availableModel.removeElement(item);
            }
            updateButtons();
        });
        centerPanel.add(addButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        removeButton = new JButton("<");
        removeButton.addActionListener(e -> {
            int[] indices = selectedList.getSelectedIndices();
            List<FachbegriffItem> toRemove = new ArrayList<>();
            for (int i : indices) {
                toRemove.add(selectedModel.getElementAt(i));
            }
            for (FachbegriffItem item : toRemove) {
                availableModel.addElement(item);
                selectedModel.removeElement(item);
            }
            updateButtons();
        });
        centerPanel.add(removeButton);
        centerPanel.add(Box.createVerticalGlue());

        panel.add(centerPanel, BorderLayout.CENTER);

        // Rechte Seite: Ausgewählte Fachbegriffe
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Ausgewählte Lernkarten"));
        selectedModel = new DefaultListModel<>();
        selectedList = new JList<>(selectedModel);
        selectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        selectedList.setCellRenderer(new FachbegriffListCellRenderer());
        JScrollPane rightScroll = new JScrollPane(selectedList);
        rightPanel.add(rightScroll, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        updateButtons();

        return panel;
    }

    private void updateButtons() {
        if (addButton != null) {
            addButton.setEnabled(availableList != null && availableList.getSelectedIndices().length > 0);
        }
        if (removeButton != null) {
            removeButton.setEnabled(selectedList != null && selectedList.getSelectedIndices().length > 0);
        }
        if (saveButton != null && selectedModel != null) {
            saveButton.setEnabled(selectedModel.size() > 0);
        }
    }

    private void loadFachbegriffe() {
        saveButton.setEnabled(false);
        new Thread(() -> {
            try {
                C2SGETAllFachbegriffe request = new C2SGETAllFachbegriffe();
                S2CPOSTAllFachbegriffe response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CPOSTAllFachbegriffe.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    FachbegriffItem[] items = response.getFachbegriffe();
                    availableModel.clear();
                    if (items != null) {
                        for (FachbegriffItem item : items) {
                            if (item != null) {
                                availableModel.addElement(item);
                            }
                        }
                    }
                    saveButton.setEnabled(selectedModel.size() > 0);
                });

            } catch (Exception e) {
                logger.error("Fehler beim Laden der Fachbegriffe", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Fehler beim Laden der Lernkarten: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                    dispose();
                });
            }
        }).start();

        // Selection Listeners für Button-Updates
        availableList.addListSelectionListener(e -> updateButtons());
        selectedList.addListSelectionListener(e -> updateButtons());
    }

    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(QuizTemplateErstellenDialog.this,
                        "Bitte geben Sie einen Namen für das Quiz-Template ein!",
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedModel.size() == 0) {
                JOptionPane.showMessageDialog(QuizTemplateErstellenDialog.this,
                        "Bitte wählen Sie mindestens eine Lernkarte aus!",
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            FachbegriffItem[] items = new FachbegriffItem[selectedModel.size()];
            for (int i = 0; i < selectedModel.size(); i++) {
                items[i] = selectedModel.getElementAt(i);
            }

            Quiz quiz = new Quiz(name, items);
            onSave.accept(quiz);
            dispose();
        }
    }

    private static class FachbegriffListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof FachbegriffItem) {
                FachbegriffItem item = (FachbegriffItem) value;
                String text = "<html><b>" + (item.getWord() != null ? item.getWord() : "-") + "</b><br/>" +
                        "<small>Level: " + item.getLevel() + " | Punkte: " + item.getMaxPoints() + "</small></html>";
                setText(text);
            }
            return this;
        }
    }
}
