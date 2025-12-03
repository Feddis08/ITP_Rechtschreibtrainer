package at.tgm.client.quiz;

import at.tgm.objects.FachbegriffItem;

import javax.swing.*;
import java.awt.*;

public class QuizFrame extends JFrame {

    private final FachbegriffItem[] items;

    public QuizFrame(FachbegriffItem[] items) {
        this.items = items;

        setTitle("Quiz – Fachbegriffe");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildListPanel(), BorderLayout.CENTER);

        setVisible(true);
    }

    // HEADER
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Fachbegriffe Übersicht", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        header.add(title, BorderLayout.WEST);

        return header;
    }

    // LIST PANEL – scrollbare Liste der Items
    private JScrollPane buildListPanel() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        for (FachbegriffItem item : items) {
            listPanel.add(buildItemCard(item));
            listPanel.add(Box.createVerticalStrut(10));
        }

        return new JScrollPane(listPanel);
    }

    // EINZELNE KARTE / ITEM-DARSTELLUNG
    private JPanel buildItemCard(FachbegriffItem item) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(250, 250, 250));

        // Titelzeile: Word (oder "???")
        String word = (item.getWord() != null ? item.getWord() : "???");

        JLabel title = new JLabel(word);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        card.add(title, BorderLayout.NORTH);

        // Mittlerer Text: Phrase
        JTextArea phrase = new JTextArea(item.getPhrase());
        phrase.setWrapStyleWord(true);
        phrase.setLineWrap(true);
        phrase.setEditable(false);
        phrase.setBackground(new Color(250, 250, 250));

        card.add(phrase, BorderLayout.CENTER);

        // Footer mit Level + Points
        JLabel footer = new JLabel("Level: " + item.getLevel() + "   |   Punkte: " + item.getPoints());
        footer.setFont(new Font("Arial", Font.ITALIC, 12));

        card.add(footer, BorderLayout.SOUTH);

        return card;
    }
}
