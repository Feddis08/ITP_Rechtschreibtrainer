package at.tgm.client.dashboard;

import at.tgm.client.ClientNetworkController;
import at.tgm.client.GuiController;
import at.tgm.network.packets.C2SPOSTSchuelerVorschlag;
import at.tgm.network.packets.S2CResponseSchuelerVorschlag;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SchuelerAnlegenPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(SchuelerAnlegenPanel.class);
    
    private final DashboardFrame parent;
    
    // Formularfelder
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField ageField;
    private JTextField schoolClassField;
    private JTextField emailField;
    private JTextField phoneNumberField;
    private JTextField displayNameField;
    private JTextArea beschreibungArea;
    private JTextField profilePictureUrlField;
    
    private JButton submitButton;
    private JLabel statusLabel;

    public SchuelerAnlegenPanel(DashboardFrame parent, GuiController controller) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        
        JLabel header = new JLabel("Schüler anlegen");
        header.setFont(new Font("Arial", Font.BOLD, 20));
        add(header, BorderLayout.NORTH);
        
        JPanel formPanel = buildFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = buildButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Benutzername *:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Passwort *:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Vorname:"), gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(20);
        panel.add(firstNameField, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Nachname:"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(20);
        panel.add(lastNameField, gbc);
        
        // Age
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Alter:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(20);
        panel.add(ageField, gbc);
        
        // School Class
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Klasse:"), gbc);
        gbc.gridx = 1;
        schoolClassField = new JTextField(20);
        panel.add(schoolClassField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("E-Mail:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Phone Number
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Telefonnummer:"), gbc);
        gbc.gridx = 1;
        phoneNumberField = new JTextField(20);
        panel.add(phoneNumberField, gbc);
        
        // Display Name
        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(new JLabel("Anzeigename:"), gbc);
        gbc.gridx = 1;
        displayNameField = new JTextField(20);
        panel.add(displayNameField, gbc);
        
        // Beschreibung
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Beschreibung:"), gbc);
        gbc.gridx = 1;
        beschreibungArea = new JTextArea(3, 20);
        beschreibungArea.setLineWrap(true);
        beschreibungArea.setWrapStyleWord(true);
        JScrollPane beschreibungScroll = new JScrollPane(beschreibungArea);
        panel.add(beschreibungScroll, gbc);
        
        // Profile Picture URL
        gbc.gridx = 0; gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Profilbild-URL:"), gbc);
        gbc.gridx = 1;
        profilePictureUrlField = new JTextField(20);
        panel.add(profilePictureUrlField, gbc);
        
        // Status Label
        gbc.gridx = 0; gbc.gridy = 11;
        gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, gbc);
        
        return panel;
    }
    
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        submitButton = new JButton("Schüler anlegen");
        submitButton.addActionListener(new SubmitActionListener());
        panel.add(submitButton);
        
        return panel;
    }
    
    private class SubmitActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Validierung
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty()) {
                statusLabel.setText("Bitte Benutzername eingeben!");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(
                    parent,
                    "Bitte geben Sie einen Benutzername ein!",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            if (password.isEmpty()) {
                statusLabel.setText("Bitte Passwort eingeben!");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(
                    parent,
                    "Bitte geben Sie ein Passwort ein!",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Erstelle Schueler-Objekt
            Schueler schueler = new Schueler(username, password);
            schueler.setFirstName(firstNameField.getText().trim());
            schueler.setLastName(lastNameField.getText().trim());
            
            String ageText = ageField.getText().trim();
            if (!ageText.isEmpty()) {
                try {
                    int age = Integer.parseInt(ageText);
                    schueler.setAge(age);
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Alter muss eine Zahl sein!");
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(
                        parent,
                        "Das Alter muss eine gültige Zahl sein!",
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }
            
            schueler.setSchoolClass(schoolClassField.getText().trim());
            schueler.setEmail(emailField.getText().trim());
            schueler.setPhoneNumber(phoneNumberField.getText().trim());
            schueler.setDisplayName(displayNameField.getText().trim());
            schueler.setBeschreibung(beschreibungArea.getText().trim());
            schueler.setProfilePictureUrl(profilePictureUrlField.getText().trim());
            
            // Sende an Server
            submitButton.setEnabled(false);
            statusLabel.setText("Wird gesendet...");
            statusLabel.setForeground(Color.BLUE);
            
            // In separatem Thread ausführen
            new Thread(() -> {
                try {
                    C2SPOSTSchuelerVorschlag request = new C2SPOSTSchuelerVorschlag(schueler);
                    S2CResponseSchuelerVorschlag response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                            request,
                            S2CResponseSchuelerVorschlag.class,
                            5,
                            TimeUnit.SECONDS
                        );
                    
                    // UI-Update im EDT
                    SwingUtilities.invokeLater(() -> {
                        submitButton.setEnabled(true);
                        if (response.isSuccess()) {
                            statusLabel.setText("Schüler erfolgreich angelegt!");
                            statusLabel.setForeground(new Color(0, 150, 0));
                            JOptionPane.showMessageDialog(
                                parent,
                                "Schüler wurde erfolgreich angelegt!",
                                "Erfolg",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            // Formular zurücksetzen
                            clearForm();
                        } else {
                            String errorMessage = response.getMessage();
                            statusLabel.setText("Fehler: " + errorMessage);
                            statusLabel.setForeground(Color.RED);
                            JOptionPane.showMessageDialog(
                                parent,
                                "Fehler beim Anlegen des Schülers:\n" + errorMessage,
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                    
                } catch (TimeoutException ex) {
                    logger.error("Timeout beim Senden des SchuelerVorschlags", ex);
                    SwingUtilities.invokeLater(() -> {
                        submitButton.setEnabled(true);
                        statusLabel.setText("Timeout: Keine Antwort vom Server.");
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(
                            parent,
                            "Timeout: Der Server hat nicht innerhalb der erwarteten Zeit geantwortet.\nBitte versuchen Sie es erneut.",
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                } catch (IOException ex) {
                    logger.error("Fehler beim Senden des SchuelerVorschlags", ex);
                    SwingUtilities.invokeLater(() -> {
                        submitButton.setEnabled(true);
                        String errorMsg = ex.getMessage();
                        statusLabel.setText("Fehler: " + errorMsg);
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(
                            parent,
                            "Netzwerk-Fehler beim Senden:\n" + (errorMsg != null ? errorMsg : "Unbekannter Fehler"),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.error("Unterbrochen beim Senden des SchuelerVorschlags", ex);
                    SwingUtilities.invokeLater(() -> {
                        submitButton.setEnabled(true);
                        statusLabel.setText("Vorgang unterbrochen.");
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(
                            parent,
                            "Der Vorgang wurde unterbrochen.\nBitte versuchen Sie es erneut.",
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                }
            }).start();
        }
    }
    
    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        ageField.setText("");
        schoolClassField.setText("");
        emailField.setText("");
        phoneNumberField.setText("");
        displayNameField.setText("");
        beschreibungArea.setText("");
        profilePictureUrlField.setText("");
    }
}
