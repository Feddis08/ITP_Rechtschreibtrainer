package at.tgm.client.dashboard;


import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

public class StatsPanel extends JPanel {

    private final Quiz[] quizzes;
    private final DashboardFrame parent;

    public StatsPanel(Quiz[] quizzes, DashboardFrame parent) {
        this.quizzes = quizzes;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel headerPanel = new JPanel(new BorderLayout());
        String headerText = (parent != null && parent.getCurrentNutzer() instanceof at.tgm.objects.Lehrer)
            ? "Quiz-Ergebnisse des Schülers"
            : "Deine vergangenen Quiz-Ergebnisse";
        JLabel header = new JLabel(headerText);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(header, BorderLayout.WEST);
        
        JButton backButton = new JButton("← Zurück");
        backButton.addActionListener(e -> {
            if (parent != null) {
                // Für Schüler: zurück zum Home
                if (parent.getCurrentNutzer() instanceof at.tgm.objects.Schueler) {
                    parent.showCardPublic("HOME");
                } else {
                    // Für Lehrer: zurück zum Schüler-Dashboard
                    Schueler schueler = parent.getCurrentViewedSchueler();
                    if (schueler != null) {
                        parent.showSchuelerDashboard(schueler);
                    } else {
                        // Fallback: zurück zur Schülerliste
                        if (parent.getController() != null) {
                            parent.getController().onSchuelerMenuClicked();
                        }
                    }
                }
            }
        });
        headerPanel.add(backButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        if (quizzes == null || quizzes.length == 0) {
            JLabel empty = new JLabel("Du hast bisher keine Quizzes abgeschlossen.");
            empty.setFont(new Font("Arial", Font.PLAIN, 14));
            listPanel.add(empty);
        } else {
            for (Quiz q : quizzes) {
                listPanel.add(buildQuizEntry(q));
                listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildQuizEntry(Quiz q) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Punkte und Farben
        int points = q.getPoints();
        int max = q.getMaxPoints();
        double percent = (max == 0 ? 0 : (points * 100.0 / max));

        Color statusColor = (percent >= 50) ? new Color(0, 180, 0) : new Color(200, 0, 0);

        JLabel title = new JLabel("Quiz — " + (int) percent + "% erreicht");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(statusColor);

        // Datum & Dauer
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String date = sdf.format(q.getTimeStarted());
        long durationSec = (q.getTimeEnded() - q.getTimeStarted()) / 1000;

        JLabel info = new JLabel(
                "<html>"
                        + "Start: " + date + "<br>"
                        + "Dauer: " + durationSec + " Sekunden<br>"
                        + "Punkte: " + points + " / " + max
                        + "</html>"
        );

        panel.add(title, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);

        // Klick → Quiz-Review öffnen
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Quiz-Ergebnis wie bei "Finish"
                FachbegriffItem[] userItems = q.getUserItems();
                if (userItems == null) {
                    // Fallback: Wenn userItems null ist, zeige zumindest eine leere Liste
                    userItems = new FachbegriffItem[0];
                }
                parent.showQuizResults(
                        userItems,
                        q.getPoints(),
                        q.getMaxPoints()
                );
            }
        });

        return panel;
    }
}
