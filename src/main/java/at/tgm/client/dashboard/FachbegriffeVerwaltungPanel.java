package at.tgm.client.dashboard;

import at.tgm.client.ClientNetworkController;
import at.tgm.network.packets.*;
import at.tgm.objects.FachbegriffItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FachbegriffeVerwaltungPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(FachbegriffeVerwaltungPanel.class);

    private final DashboardFrame parent;
    private FachbegriffItem[] fachbegriffe;
    private JPanel listPanel;
    private JButton refreshButton;
    private JButton createButton;

    public FachbegriffeVerwaltungPanel(DashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Lernkarten-Verwaltung");
        header.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(header, BorderLayout.WEST);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createButton = new JButton("+ Neue Lernkarte");
        createButton.addActionListener(e -> showCreateDialog());
        buttonPanel.add(createButton);

        refreshButton = new JButton("Aktualisieren");
        refreshButton.addActionListener(e -> loadFachbegriffe());
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
        loadFachbegriffe();
    }

    public void setFachbegriffe(FachbegriffItem[] fachbegriffe) {
        this.fachbegriffe = fachbegriffe;
        updateList();
    }

    private void updateList() {
        listPanel.removeAll();

        if (fachbegriffe == null || fachbegriffe.length == 0) {
            JLabel empty = new JLabel("Keine Lernkarten vorhanden.");
            empty.setFont(new Font("Arial", Font.PLAIN, 14));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(empty);
        } else {
            for (FachbegriffItem item : fachbegriffe) {
                if (item != null) {
                    listPanel.add(buildEntry(item));
                    listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JComponent buildEntry(FachbegriffItem item) {
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

        JLabel wordLabel = new JLabel(item.getWord() != null ? item.getWord() : "-");
        wordLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(wordLabel);

        JLabel phraseLabel = new JLabel("<html>" + (item.getPhrase() != null ? item.getPhrase() : "-") + "</html>");
        phraseLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        phraseLabel.setForeground(Color.GRAY);
        infoPanel.add(phraseLabel);

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        metaPanel.setOpaque(false);
        metaPanel.add(new JLabel("Level: " + item.getLevel()));
        metaPanel.add(new JLabel("|"));
        metaPanel.add(new JLabel("Punkte: " + item.getMaxPoints()));
        infoPanel.add(metaPanel);

        panel.add(infoPanel, BorderLayout.CENTER);

        // Rechte Seite: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton editButton = new JButton("Bearbeiten");
        editButton.addActionListener(e -> showEditDialog(item));
        buttonPanel.add(editButton);

        JButton deleteButton = new JButton("Löschen");
        deleteButton.addActionListener(e -> deleteFachbegriff(item));
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void loadFachbegriffe() {
        refreshButton.setEnabled(false);
        createButton.setEnabled(false);

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
                    setFachbegriffe(response.getFachbegriffe());
                    refreshButton.setEnabled(true);
                    createButton.setEnabled(true);
                });

            } catch (TimeoutException e) {
                logger.error("Timeout beim Laden der Fachbegriffe", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent,
                            "Timeout: Der Server hat nicht geantwortet.",
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                    refreshButton.setEnabled(true);
                    createButton.setEnabled(true);
                });
            } catch (IOException | InterruptedException e) {
                logger.error("Fehler beim Laden der Fachbegriffe", e);
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
        FachbegriffErstellenDialog dialog = new FachbegriffErstellenDialog(parent, item -> {
            createFachbegriff(item);
        });
        dialog.setVisible(true);
    }

    private void showEditDialog(FachbegriffItem item) {
        FachbegriffBearbeitenDialog dialog = new FachbegriffBearbeitenDialog(parent, item, updatedItem -> {
            updateFachbegriff(item.getId(), updatedItem);
        });
        dialog.setVisible(true);
    }

    private void createFachbegriff(FachbegriffItem item) {
        new Thread(() -> {
            try {
                C2SPOSTFachbegriff request = new C2SPOSTFachbegriff(item);
                S2CResponseFachbegriffOperation response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CResponseFachbegriffOperation.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(parent,
                                "Lernkarte erfolgreich erstellt!",
                                "Erfolg",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadFachbegriffe();
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Fehler: " + response.getMessage(),
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                logger.error("Fehler beim Erstellen des Fachbegriffs", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent,
                            "Fehler beim Erstellen: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void updateFachbegriff(long id, FachbegriffItem item) {
        new Thread(() -> {
            try {
                C2SPUTFachbegriff request = new C2SPUTFachbegriff(id, item);
                S2CResponseFachbegriffOperation response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CResponseFachbegriffOperation.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(parent,
                                "Lernkarte erfolgreich aktualisiert!",
                                "Erfolg",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadFachbegriffe();
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Fehler: " + response.getMessage(),
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                logger.error("Fehler beim Aktualisieren des Fachbegriffs", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent,
                            "Fehler beim Aktualisieren: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void deleteFachbegriff(FachbegriffItem item) {
        int result = JOptionPane.showConfirmDialog(parent,
                "Möchten Sie die Lernkarte '" + item.getWord() + "' wirklich löschen?",
                "Lernkarte löschen",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        new Thread(() -> {
            try {
                C2SDELETEFachbegriff request = new C2SDELETEFachbegriff(item.getId());
                S2CResponseFachbegriffOperation response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                                request,
                                S2CResponseFachbegriffOperation.class,
                                5,
                                TimeUnit.SECONDS
                        );

                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(parent,
                                "Lernkarte erfolgreich gelöscht!",
                                "Erfolg",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadFachbegriffe();
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Fehler: " + response.getMessage(),
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                logger.error("Fehler beim Löschen des Fachbegriffs", e);
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
