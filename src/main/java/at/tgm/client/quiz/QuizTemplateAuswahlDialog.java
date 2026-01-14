package at.tgm.client.quiz;

import at.tgm.client.ClientNetworkController;
import at.tgm.network.packets.C2SGETQuizTemplatesForSchueler;
import at.tgm.network.packets.S2CPOSTAllQuizTemplates;
import at.tgm.objects.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QuizTemplateAuswahlDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(QuizTemplateAuswahlDialog.class);

    private JList<Quiz> templateList;
    private DefaultListModel<Quiz> templateModel;
    private JButton startButton;
    private JButton cancelButton;
    private Long selectedTemplateId = null;

    public QuizTemplateAuswahlDialog(JFrame parent) {
        super(parent, "Quiz-Template auswählen", true);
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JLabel header = new JLabel("Wählen Sie ein Quiz-Template aus:");
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        add(header, BorderLayout.NORTH);

        // Liste der Templates
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        templateModel = new DefaultListModel<>();
        templateList = new JList<>(templateModel);
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.setCellRenderer(new QuizTemplateListCellRenderer());
        templateList.addListSelectionListener(e -> {
            Quiz selected = templateList.getSelectedValue();
            startButton.setEnabled(selected != null);
        });
        JScrollPane scrollPane = new JScrollPane(templateList);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        add(listPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        startButton = new JButton("Quiz starten");
        startButton.setEnabled(false);
        startButton.addActionListener(e -> {
            Quiz selected = templateList.getSelectedValue();
            if (selected != null) {
                selectedTemplateId = selected.getId();
            } else {
                // Zufälliges Quiz (null = 0)
                selectedTemplateId = 0L;
            }
            dispose();
        });
        cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> {
            selectedTemplateId = null;
            dispose();
        });
        buttonPanel.add(cancelButton);
        buttonPanel.add(startButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Lade Templates
        loadTemplates();
    }

    private void loadTemplates() {
        startButton.setEnabled(false);
        new Thread(() -> {
            try {
                C2SGETQuizTemplatesForSchueler request = new C2SGETQuizTemplatesForSchueler();
                S2CPOSTAllQuizTemplates response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CPOSTAllQuizTemplates.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    Quiz[] templates = response.getQuizTemplates();
                    templateModel.clear();
                    
                    // Füge "Zufälliges Quiz" als erste Option hinzu
                    templateModel.addElement(null); // null = zufälliges Quiz
                    
                    // Füge verfügbare Templates hinzu
                    if (templates != null && templates.length > 0) {
                        for (Quiz quiz : templates) {
                            if (quiz != null) {
                                templateModel.addElement(quiz);
                            }
                        }
                    }
                    
                    // Wähle erstes Element (Zufälliges Quiz) aus
                    templateList.setSelectedIndex(0);
                });

            } catch (TimeoutException e) {
                logger.error("Timeout beim Laden der Quiz-Templates", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Timeout: Der Server hat nicht geantwortet.",
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                    dispose();
                });
            } catch (IOException | InterruptedException e) {
                logger.error("Fehler beim Laden der Quiz-Templates", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Fehler beim Laden: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                    dispose();
                });
            }
        }).start();
    }

    public Long getSelectedTemplateId() {
        return selectedTemplateId;
    }

    private static class QuizTemplateListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                setText("<html><b>Zufälliges Quiz</b><br/><small>10 zufällige Lernkarten</small></html>");
                setEnabled(true);
            } else if (value instanceof Quiz) {
                Quiz quiz = (Quiz) value;
                String name = quiz.getName() != null && !quiz.getName().isEmpty()
                    ? quiz.getName()
                    : "Quiz-Template (ID: " + quiz.getId() + ")";
                int itemCount = quiz.getItems() != null ? quiz.getItems().length : 0;
                setText("<html><b>" + name + "</b><br/><small>" + itemCount + " Lernkarten</small></html>");
                setEnabled(true);
            }
            return this;
        }
    }
}
