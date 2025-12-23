package at.tgm.client.dashboard;

import at.tgm.objects.Schueler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SchuelerListPanel extends JPanel {

    private final Schueler[] schueler;
    private final DashboardFrame parent;

    public SchuelerListPanel(Schueler[] schueler, DashboardFrame parent) {
        this.schueler = schueler;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Schülerübersicht");
        header.setFont(new Font("Arial", Font.BOLD, 20));
        add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        if (schueler == null || schueler.length == 0) {
            JLabel empty = new JLabel("Keine Schüler gefunden.");
            empty.setFont(new Font("Arial", Font.PLAIN, 14));
            listPanel.add(empty);
        } else {
            for (Schueler s : schueler) {
                listPanel.add(buildEntry(s));
                listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JComponent buildEntry(Schueler s) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setBackground(Color.WHITE);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String displayName =
                s.getDisplayName() != null && !s.getDisplayName().isEmpty()
                        ? s.getDisplayName()
                        : s.getUsername();

        JLabel name = new JLabel(displayName);
        name.setFont(new Font("Arial", Font.BOLD, 14));

        String classText = (s.getSchoolClass() != null) ? s.getSchoolClass() : "-";
        JLabel clazz = new JLabel("Klasse: " + classText);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(name);
        textPanel.add(clazz);

        panel.add(textPanel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parent.showSchuelerDashboard(s);
            }
        });

        return panel;
    }
}
