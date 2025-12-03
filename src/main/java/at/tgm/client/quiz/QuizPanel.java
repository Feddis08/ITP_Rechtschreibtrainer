package at.tgm.client.quiz;

import at.tgm.objects.FachbegriffItem;

import javax.swing.*;
import java.awt.*;

public class QuizPanel extends JPanel {

    private FachbegriffItem[] items;

    public QuizPanel(FachbegriffItem[] items) {
        this.items = items;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildListPanel(), BorderLayout.CENTER);
    }

    public void setItems(FachbegriffItem[] items) {
        this.items = items;
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Fachbegriffe Übersicht", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);

        return header;
    }

    private JScrollPane buildListPanel() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        if (items != null) {
            for (FachbegriffItem item : items) {
                listPanel.add(buildItemCard(item));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private JPanel buildItemCard(FachbegriffItem item) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(250, 250, 250));

        String word = (item.getWord() != null ? item.getWord() : "???");

        JLabel title = new JLabel(word);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        card.add(title, BorderLayout.NORTH);

        JTextArea phrase = new JTextArea(item.getPhrase());
        phrase.setWrapStyleWord(true);
        phrase.setLineWrap(true);
        phrase.setEditable(false);
        phrase.setOpaque(false);

        card.add(phrase, BorderLayout.CENTER);

        JLabel footer = new JLabel("Level: " + item.getLevel() + "   |   Punkte: " + item.getPoints());
        footer.setFont(new Font("Arial", Font.ITALIC, 12));
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }
}
