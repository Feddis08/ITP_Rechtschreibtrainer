package at.tgm.client.dashboard;

import at.tgm.client.ClientNetworkController;
import at.tgm.network.packets.*;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QuizTemplatesVerwaltungPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(QuizTemplatesVerwaltungPanel.class);

    private final DashboardFrame parent;
    private Quiz[] quizTemplates;
    private JPanel listPanel;
    private JButton refreshButton;
    private JButton createButton;

    public QuizTemplatesVerwaltungPanel(DashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Quiz-Templates-Verwaltung");
        header.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(header, BorderLayout.WEST);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createButton = new JButton("+ Neues Quiz-Template");
        createButton.addActionListener(e -> showCreateDialog());
        buttonPanel.add(createButton);

        refreshButton = new JButton("Aktualisieren");
        refreshButton.addActionListener(e -> loadQuizTemplates());
        buttonPanel.add(refreshButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Liste
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Lade Daten
        loadQuizTemplates();
    }

    public void setQuizTemplates(Quiz[] quizTemplates) {
        this.quizTemplates = quizTemplates;
        updateList();
    }

    private void updateList() {
        listPanel.removeAll();

        if (quizTemplates == null || quizTemplates.length == 0) {
            JLabel empty = new JLabel("Keine Quiz-Templates vorhanden.");
            empty.setFont(new Font("Arial", Font.PLAIN, 14));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(empty);
        } else {
            for (Quiz quiz : quizTemplates) {
                if (quiz != null) {
                    listPanel.add(buildEntry(quiz));
                    listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JComponent buildEntry(Quiz quiz) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setBackground(Color.WHITE);

        // Linke Seite: Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        FachbegriffItem[] items = quiz.getItems();
        int itemCount = items != null ? items.length : 0;

        String templateName = quiz.getName() != null && !quiz.getName().isEmpty() 
            ? quiz.getName() 
            : "Quiz-Template (ID: " + quiz.getId() + ")";
        JLabel titleLabel = new JLabel(templateName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(titleLabel);

        JLabel countLabel = new JLabel("Anzahl Lernkarten: " + itemCount);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        countLabel.setForeground(Color.GRAY);
        infoPanel.add(countLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        // Rechte Seite: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton editButton = new JButton("Bearbeiten");
        editButton.addActionListener(e -> showEditDialog(quiz));
        buttonPanel.add(editButton);

        JButton deleteButton = new JButton("Löschen");
        deleteButton.addActionListener(e -> deleteQuizTemplate(quiz));
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void loadQuizTemplates() {
        refreshButton.setEnabled(false);
        createButton.setEnabled(false);

        new Thread(() -> {
            try {
                C2SGETAllQuizTemplates request = new C2SGETAllQuizTemplates();
                S2CPOSTAllQuizTemplates response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CPOSTAllQuizTemplates.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    setQuizTemplates(response.getQuizTemplates());
                    refreshButton.setEnabled(true);
                    createButton.setEnabled(true);
                });

            } catch (TimeoutException e) {
                logger.error("Timeout beim Laden der Quiz-Templates", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent,
                            "Timeout: Der Server hat nicht geantwortet.",
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                    refreshButton.setEnabled(true);
                    createButton.setEnabled(true);
                });
            } catch (IOException | InterruptedException e) {
                logger.error("Fehler beim Laden der Quiz-Templates", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent,
                            "Fehler beim Laden: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                    refreshButton.setEnabled(true);
                    createButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void showCreateDialog() {
        QuizTemplateErstellenDialog dialog = new QuizTemplateErstellenDialog(parent, quiz -> {
            createQuizTemplate(quiz);
        });
        dialog.setVisible(true);
    }

    private void showEditDialog(Quiz quiz) {
        QuizTemplateBearbeitenDialog dialog = new QuizTemplateBearbeitenDialog(parent, quiz, updatedQuiz -> {
            updateQuizTemplate(quiz.getId(), updatedQuiz);
        });
        dialog.setVisible(true);
    }

    private void createQuizTemplate(Quiz quiz) {
        new Thread(() -> {
            try {
                C2SPOSTQuizTemplate request = new C2SPOSTQuizTemplate(quiz);
                S2CResponseQuizTemplateOperation response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CResponseQuizTemplateOperation.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(parent,
                                "Quiz-Template erfolgreich erstellt!",
                                "Erfolg",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadQuizTemplates();
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Fehler: " + response.getMessage(),
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                logger.error("Fehler beim Erstellen des QuizTemplates", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent,
                            "Fehler beim Erstellen: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void updateQuizTemplate(long id, Quiz quiz) {
        new Thread(() -> {
            try {
                C2SPUTQuizTemplate request = new C2SPUTQuizTemplate(id, quiz);
                S2CResponseQuizTemplateOperation response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CResponseQuizTemplateOperation.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(parent,
                                "Quiz-Template erfolgreich aktualisiert!",
                                "Erfolg",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadQuizTemplates();
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Fehler: " + response.getMessage(),
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                logger.error("Fehler beim Aktualisieren des QuizTemplates", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent,
                            "Fehler beim Aktualisieren: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void deleteQuizTemplate(Quiz quiz) {
        int result = JOptionPane.showConfirmDialog(parent,
                "Möchten Sie das Quiz-Template (ID: " + quiz.getId() + ") wirklich löschen?",
                "Quiz-Template löschen",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        new Thread(() -> {
            try {
                C2SDELETEQuizTemplate request = new C2SDELETEQuizTemplate(quiz.getId());
                S2CResponseQuizTemplateOperation response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CResponseQuizTemplateOperation.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(parent,
                                "Quiz-Template erfolgreich gelöscht!",
                                "Erfolg",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadQuizTemplates();
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Fehler: " + response.getMessage(),
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                logger.error("Fehler beim Löschen des QuizTemplates", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent,
                            "Fehler beim Löschen: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}
