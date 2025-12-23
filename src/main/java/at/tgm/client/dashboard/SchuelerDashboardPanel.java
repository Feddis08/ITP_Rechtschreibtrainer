package at.tgm.client.dashboard;

import at.tgm.objects.Schueler;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class SchuelerDashboardPanel extends JPanel {

    private final JLabel nameLabel = new JLabel();
    private final JLabel classLabel = new JLabel();
    private final JLabel emailLabel = new JLabel();
    private final JLabel lastLoginLabel = new JLabel();
    private final JLabel createdAtLabel = new JLabel();

    public SchuelerDashboardPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Schülerprofil");
        header.setFont(new Font("Arial", Font.BOLD, 20));
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new GridLayout(0, 1, 4, 4));

        center.add(nameLabel);
        center.add(classLabel);
        center.add(emailLabel);
        center.add(lastLoginLabel);
        center.add(createdAtLabel);

        add(center, BorderLayout.CENTER);
    }

    public void setSchueler(Schueler s) {
        if (s == null) {
            nameLabel.setText("Kein Schüler ausgewählt.");
            classLabel.setText("");
            emailLabel.setText("");
            lastLoginLabel.setText("");
            createdAtLabel.setText("");
            return;
        }

        String displayName =
                s.getDisplayName() != null && !s.getDisplayName().isEmpty()
                        ? s.getDisplayName()
                        : s.getUsername();

        nameLabel.setText("Name: " + displayName);
        classLabel.setText("Klasse: " + (s.getSchoolClass() != null ? s.getSchoolClass() : "-"));
        emailLabel.setText("E-Mail: " + (s.getEmail() != null ? s.getEmail() : "-"));

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if (s.getLastLoginTimestamp() > 0) {
            lastLoginLabel.setText("Letzter Login: " + sdf.format(s.getLastLoginTimestamp()));
        } else {
            lastLoginLabel.setText("Letzter Login: -");
        }
        createdAtLabel.setText("Account erstellt: " + sdf.format(s.getCreatedAt()));
    }
}
