package at.tgm.client.dashboard;

import at.tgm.client.ClientNetworkController;
import at.tgm.network.packets.C2SDeleteSchueler;
import at.tgm.network.packets.C2SToggleSchuelerStatus;
import at.tgm.network.packets.S2CResponseSchuelerOperation;
import at.tgm.objects.NutzerStatus;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SchuelerDashboardPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(SchuelerDashboardPanel.class);

    private final JLabel nameLabel = new JLabel();
    private final JLabel usernameLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JLabel classLabel = new JLabel();
    private final JLabel emailLabel = new JLabel();
    private final JLabel phoneLabel = new JLabel();
    private final JLabel beschreibungLabel = new JLabel();
    private final JLabel lastLoginLabel = new JLabel();
    private final JLabel createdAtLabel = new JLabel();
    
    private Schueler currentSchueler;
    private final DashboardFrame parent;
    private JButton toggleStatusButton;
    private JButton deleteButton;

    public SchuelerDashboardPanel(DashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Schülerprofil");
        header.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(header, BorderLayout.WEST);
        
        JButton backButton = new JButton("← Zurück");
        backButton.addActionListener(e -> {
            if (parent != null) {
                parent.showSchuelerList(null); // Wird neu geladen
                if (parent.getController() != null) {
                    parent.getController().onSchuelerMenuClicked();
                }
            }
        });
        headerPanel.add(backButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row++;
        center.add(nameLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        center.add(usernameLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        center.add(statusLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        center.add(classLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        center.add(emailLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        center.add(phoneLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.weighty = 0;
        center.add(beschreibungLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        center.add(lastLoginLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++;
        center.add(createdAtLabel, gbc);

        add(center, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton quizesButton = new JButton("Quizes anzeigen");
        quizesButton.addActionListener(e -> {
            if (currentSchueler != null && parent != null) {
                parent.loadSchuelerQuizes(currentSchueler.getUsername());
            }
        });
        buttonPanel.add(quizesButton);
        
        toggleStatusButton = new JButton("Ausschreiben");
        toggleStatusButton.addActionListener(e -> handleToggleStatus());
        buttonPanel.add(toggleStatusButton);
        
        deleteButton = new JButton("Loeschen");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> handleDelete());
        buttonPanel.add(deleteButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setSchueler(Schueler s) {
        this.currentSchueler = s;
        
        if (s == null) {
            nameLabel.setText("Kein Schüler ausgewählt.");
            usernameLabel.setText("");
            statusLabel.setText("");
            classLabel.setText("");
            emailLabel.setText("");
            phoneLabel.setText("");
            beschreibungLabel.setText("");
            lastLoginLabel.setText("");
            createdAtLabel.setText("");
            return;
        }

        String displayName =
                s.getDisplayName() != null && !s.getDisplayName().isEmpty()
                        ? s.getDisplayName()
                        : s.getUsername();
        
        String firstName = s.getFirstName() != null ? s.getFirstName() : "";
        String lastName = s.getLastName() != null ? s.getLastName() : "";
        String fullName = (!firstName.isEmpty() || !lastName.isEmpty()) 
            ? (firstName + " " + lastName).trim() 
            : displayName;

        nameLabel.setText("Name: " + fullName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        usernameLabel.setText("Benutzername: " + s.getUsername());
        
        // Status mit Farbe
        NutzerStatus status = s.getStatus();
        String statusText = (status != null) ? status.name() : "OFFLINE";
        Color statusColor = colorForStatus(status);
        statusLabel.setText("Status: ● " + statusText);
        statusLabel.setForeground(statusColor);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        classLabel.setText("Klasse: " + (s.getSchoolClass() != null ? s.getSchoolClass() : "-"));
        emailLabel.setText("E-Mail: " + (s.getEmail() != null ? s.getEmail() : "-"));
        phoneLabel.setText("Telefon: " + (s.getPhoneNumber() != null ? s.getPhoneNumber() : "-"));
        
        String beschreibung = s.getBeschreibung();
        if (beschreibung != null && !beschreibung.isEmpty()) {
            beschreibungLabel.setText("<html><b>Beschreibung:</b><br>" + beschreibung + "</html>");
        } else {
            beschreibungLabel.setText("Beschreibung: -");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if (s.getLastLoginTimestamp() > 0) {
            lastLoginLabel.setText("Letzter Login: " + sdf.format(s.getLastLoginTimestamp()));
        } else {
            lastLoginLabel.setText("Letzter Login: -");
        }
        createdAtLabel.setText("Account erstellt: " + sdf.format(s.getCreatedAt()));
        
        // Update button text based on deactivated status
        if (toggleStatusButton != null) {
            if (s.isDeactivated()) {
                toggleStatusButton.setText("Einschreiben");
            } else {
                toggleStatusButton.setText("Ausschreiben");
            }
        }
    }
    
    private void handleToggleStatus() {
        if (currentSchueler == null) {
            return;
        }
        
        String username = currentSchueler.getUsername();
        logger.info("Toggle Status für Schüler: {}", username);
        
        // In separatem Thread ausführen, um UI nicht zu blockieren
        new Thread(() -> {
            try {
                C2SToggleSchuelerStatus request = new C2SToggleSchuelerStatus(username);
                S2CResponseSchuelerOperation response = ClientNetworkController.socketClient
                    .getChannel()
                    .sendAndWait(
                        request,
                        S2CResponseSchuelerOperation.class,
                        5,
                        TimeUnit.SECONDS
                    );
                
                // UI-Update im EDT (Event Dispatch Thread)
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        // Update local student object
                        currentSchueler.setDeactivated(!currentSchueler.isDeactivated());
                        // Update button text
                        if (currentSchueler.isDeactivated()) {
                            toggleStatusButton.setText("Einschreiben");
                        } else {
                            toggleStatusButton.setText("Ausschreiben");
                        }
                        JOptionPane.showMessageDialog(this, 
                            response.getMessage(), 
                            "Erfolg", 
                            JOptionPane.INFORMATION_MESSAGE);
                        // Refresh student list to ensure consistency
                        if (parent != null && parent.getController() != null) {
                            parent.getController().onSchuelerMenuClicked();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            response.getMessage(), 
                            "Fehler", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
                
            } catch (TimeoutException e) {
                logger.error("Timeout beim Toggle Status", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Operation konnte nicht abgeschlossen werden (Timeout).", 
                        "Fehler", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } catch (Exception e) {
                logger.error("Fehler beim Toggle Status", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Fehler: " + e.getMessage(), 
                        "Fehler", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void handleDelete() {
        if (currentSchueler == null) {
            return;
        }
        
        String username = currentSchueler.getUsername();
        String displayName = currentSchueler.getDisplayName() != null && !currentSchueler.getDisplayName().isEmpty()
            ? currentSchueler.getDisplayName()
            : username;
        
        // Bestätigungsdialog
        int result = JOptionPane.showConfirmDialog(
            this,
            "Möchten Sie den Schüler '" + displayName + "' wirklich komplett löschen?\n" +
            "Diese Aktion kann nicht rückgängig gemacht werden!",
            "Schüler löschen",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        logger.info("Lösche Schüler: {}", username);
        
        // In separatem Thread ausführen, um UI nicht zu blockieren
        new Thread(() -> {
            try {
                C2SDeleteSchueler request = new C2SDeleteSchueler(username);
                S2CResponseSchuelerOperation response = ClientNetworkController.socketClient
                    .getChannel()
                    .sendAndWait(
                        request,
                        S2CResponseSchuelerOperation.class,
                        5,
                        TimeUnit.SECONDS
                    );
                
                // UI-Update im EDT (Event Dispatch Thread)
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this, 
                            response.getMessage(), 
                            "Erfolg", 
                            JOptionPane.INFORMATION_MESSAGE);
                        // Zurück zur Schülerliste
                        if (parent != null) {
                            parent.showSchuelerList(null); // Wird neu geladen
                            if (parent.getController() != null) {
                                parent.getController().onSchuelerMenuClicked();
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            response.getMessage(), 
                            "Fehler", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
                
            } catch (TimeoutException e) {
                logger.error("Timeout beim Löschen", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Operation konnte nicht abgeschlossen werden (Timeout).", 
                        "Fehler", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } catch (Exception e) {
                logger.error("Fehler beim Löschen", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Fehler: " + e.getMessage(), 
                        "Fehler", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private Color colorForStatus(NutzerStatus s) {
        if (s == null) return Color.GRAY;
        switch (s) {
            case ONLINE: return new Color(0, 180, 0);
            case AWAY: return new Color(255, 170, 0);
            case BUSY: return new Color(200, 0, 0);
            case BANNED: return Color.BLACK;
            case OFFLINE:
            default: return Color.GRAY;
        }
    }
}
