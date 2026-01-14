package at.tgm.client.quiz;

import at.tgm.client.ClientNetworkController;
import at.tgm.network.packets.C2SPOSTQuizResults;
import at.tgm.objects.FachbegriffItem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class QuizPanel extends JPanel {

    private FachbegriffItem[] items;          // Array mit User-Eingaben (word)
    private FachbegriffItem[] originalItems;  // "altes" Array mit den vollen Punkten
    private int currentIndex = 0;

    // UI-Komponenten
    private JLabel progressLabel;
    private JTextArea phraseArea;
    private JTextField answerField;
    private JButton submitButton;

    public QuizPanel(FachbegriffItem[] items) {
        this.items = (items != null) ? items : new FachbegriffItem[0];
        this.originalItems = this.items; // für späteren Vergleich (volle Punkte & Userwort)

        buildUI();

        if (this.items.length > 0) {
            showCurrentQuestion();
        } else {
            showEmptyState();
        }
    }

    public void setItems(FachbegriffItem[] items) {
        this.items = (items != null) ? items : new FachbegriffItem[0];
        this.originalItems = this.items;
        this.currentIndex = 0;
        if (this.items.length > 0) {
            showCurrentQuestion();
        } else {
            showEmptyState();
        }
    }

    // =========================
    // UI AUFBAU
    // =========================

    private void buildUI() {
        setLayout(new BorderLayout());

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Fachbegriffe Quiz", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);

        progressLabel = new JLabel("", SwingConstants.RIGHT);
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        header.add(progressLabel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // CENTER: Phrase + Eingabe
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        phraseArea = new JTextArea();
        phraseArea.setWrapStyleWord(true);
        phraseArea.setLineWrap(true);
        phraseArea.setEditable(false);
        phraseArea.setOpaque(false);
        phraseArea.setFont(new Font("Arial", Font.PLAIN, 16));

        JScrollPane phraseScroll = new JScrollPane(phraseArea);
        phraseScroll.setBorder(BorderFactory.createTitledBorder("Satz / Phrase"));
        phraseScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        center.add(phraseScroll, BorderLayout.CENTER);

        // Eingabe-Feld + Submit-Button
        JPanel answerPanel = new JPanel(new BorderLayout(10, 10));

        answerField = new JTextField();
        answerField.setFont(new Font("Arial", Font.PLAIN, 16));

        submitButton = new JButton("Submit");

        answerPanel.add(answerField, BorderLayout.CENTER);
        answerPanel.add(submitButton, BorderLayout.EAST);

        center.add(answerPanel, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // Listener
        submitButton.addActionListener(e -> handleSubmit());
        answerField.addActionListener(e -> handleSubmit());
    }

    private void showEmptyState() {
        phraseArea.setText("Keine Fachbegriffe vorhanden.");
        progressLabel.setText("");
        answerField.setEnabled(false);
        submitButton.setEnabled(false);
    }

    // =========================
    // QUIZ LOGIK
    // =========================

    private void showCurrentQuestion() {
        if (items == null || items.length == 0) {
            showEmptyState();
            return;
        }

        if (currentIndex < 0 || currentIndex >= items.length) {
            onQuizFinished();
            return;
        }

        FachbegriffItem current = items[currentIndex];

        phraseArea.setText(current.getPhrase());
        answerField.setText("");
        answerField.requestFocusInWindow();

        progressLabel.setText("Frage " + (currentIndex + 1) + " von " + items.length);
    }

    private void handleSubmit() {
        if (items == null || items.length == 0) {
            return;
        }

        String answer = answerField.getText().trim();
        items[currentIndex].setWord(answer); // User-Eingabe in das Item schreiben

        currentIndex++;

        if (currentIndex < items.length) {
            showCurrentQuestion();
        } else {
            // Quiz fertig
            answerField.setEnabled(false);
            submitButton.setEnabled(false);
            onQuizFinished();
        }
    }

    /**
     * Wird aufgerufen, wenn der Benutzer die LETZTE Frage beantwortet hat.
     *
     * items[] enthält:
     *   - phrase
     *   - word = User-Eingabe
     *   - points = volle Punkte (Original)
     *
     * Hier schickst du die User-Antworten an den Server.
     */
    protected void onQuizFinished() {
        C2SPOSTQuizResults packet = new C2SPOSTQuizResults(this.items);
        try {
            ClientNetworkController.socketClient.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================
    // ERGEBNIS-ANSICHT (von außen aufrufbar)
    // =========================

    /**
     * Vom Netzwerk-Thread / Controller aufrufen, sobald der Server
     * die korrigierten Fachbegriffe und Punkte zurückgeschickt hat.
     *
     * @param fachbegriffe Array vom Server:
     *                     - word  = KORREKTES Wort
     *                     - points = ERREICHTE Punkte
     * @param points       Summe aller erreichten Punkte
     * @param maxPoints    maximale Punkte über alle Fragen
     */
    public void showResults(FachbegriffItem[] fachbegriffe, int points, int maxPoints) {
        if (fachbegriffe == null) {
            fachbegriffe = new FachbegriffItem[0];
        }

        removeAll();
        setLayout(new BorderLayout());

        // Header mit Punktestand
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Ergebnis", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel scoreLabel = new JLabel("Punkte: " + points + " / " + maxPoints, SwingConstants.RIGHT);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));

        header.add(title, BorderLayout.WEST);
        header.add(scoreLabel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Liste der Fragen mit grün/gelb/rot
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        for (int i = 0; i < fachbegriffe.length; i++) {
            FachbegriffItem serverItem = fachbegriffe[i];
            FachbegriffItem originalItem =
                    (originalItems != null && i < originalItems.length) ? originalItems[i] : null;

            listPanel.add(buildResultCard(serverItem, originalItem));
            listPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel buildResultCard(FachbegriffItem result, FachbegriffItem original) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Verwende direkt die Daten vom Server (result)
        String phrase = (result != null) ? result.getPhrase() : (original != null ? original.getPhrase() : "");
        String correctWord = (result != null && result.getWord() != null) ? result.getWord() : "—";
        // Verwende userWord aus dem result-Item (vom Server gespeichert), fallback zu original für Rückwärtskompatibilität
        String userWord = (result != null && result.getUserWord() != null) ? result.getUserWord() 
                        : (original != null && original.getWord() != null) ? original.getWord() : "—";

        // Punkte direkt vom Server-Item verwenden
        int earnedPoints = (result != null) ? result.getPoints() : 0;
        int maxPoints = (result != null) ? result.getMaxPoints() : (original != null ? original.getPoints() : 0);

        // Farblogik:
        // 0 Punkte      -> rot
        // 0 < Punkte < voll -> gelb
        // volle Punkte  -> grün
        Color bg;
        if (earnedPoints <= 0) {
            bg = new Color(255, 200, 200); // rot
        } else if (earnedPoints < maxPoints) {
            bg = new Color(255, 245, 200); // gelblich
        } else {
            bg = new Color(200, 255, 200); // grün
        }

        card.setBackground(bg);

        // Phrase
        JTextArea phraseArea = new JTextArea(phrase);
        phraseArea.setWrapStyleWord(true);
        phraseArea.setLineWrap(true);
        phraseArea.setEditable(false);
        phraseArea.setOpaque(false);
        phraseArea.setFont(new Font("Arial", Font.PLAIN, 14));

        card.add(phraseArea, BorderLayout.CENTER);

        // Unterer Bereich: Vergleich + Punkte
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        JLabel userLabel = new JLabel("Deine Antwort: " + userWord);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel correctLabel = new JLabel("Richtig: " + correctWord);
        correctLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel pointsLabel = new JLabel("Punkte: " + earnedPoints + " / " + maxPoints);
        pointsLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        bottom.add(userLabel);
        bottom.add(correctLabel);
        bottom.add(pointsLabel);

        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }
}
