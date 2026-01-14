package at.tgm.client.dashboard;

import at.tgm.objects.Lehrer;

import javax.swing.*;
import java.awt.*;

public class LehrerListPanel extends JPanel {

    private final Lehrer[] lehrer;
    private final DashboardFrame parent;

    public LehrerListPanel(Lehrer[] lehrer, DashboardFrame parent) {
        this.lehrer = lehrer;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Lehrerübersicht");
        header.setFont(new Font("Arial", Font.BOLD, 20));
        add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        if (lehrer == null || lehrer.length == 0) {
            JLabel empty = new JLabel("Keine Lehrer gefunden.");
            empty.setFont(new Font("Arial", Font.PLAIN, 14));
            listPanel.add(empty);
        } else {
            for (Lehrer l : lehrer) {
                listPanel.add(buildEntry(l));
                listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JComponent buildEntry(Lehrer l) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setBackground(Color.WHITE);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String displayName =
                l.getDisplayName() != null && !l.getDisplayName().isEmpty()
                        ? l.getDisplayName()
                        : l.getUsername();

        JLabel name = new JLabel(displayName);
        name.setFont(new Font("Arial", Font.BOLD, 14));

        String statusText = l.isDeactivated() ? "Deaktiviert" : "Aktiv";
        JLabel status = new JLabel("Status: " + statusText);
        status.setForeground(l.isDeactivated() ? Color.RED : new Color(0, 150, 0));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(name);
        textPanel.add(status);

        panel.add(textPanel, BorderLayout.CENTER);

        // Rechts: Buttons für Aktionen
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton toggleButton = new JButton(l.isDeactivated() ? "Aktivieren" : "Deaktivieren");
        JButton deleteButton = new JButton("Löschen");
        
        toggleButton.addActionListener(e -> {
            parent.toggleLehrerStatus(l.getUsername());
        });
        
        deleteButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                parent,
                "Möchten Sie den Lehrer '" + l.getUsername() + "' wirklich löschen?",
                "Lehrer löschen",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                parent.deleteLehrer(l.getUsername());
            }
        });
        
        buttonPanel.add(toggleButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }
}
