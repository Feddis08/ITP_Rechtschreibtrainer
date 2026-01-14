package at.tgm.client.dashboard;

import at.tgm.client.ClientNetworkController;
import at.tgm.client.GuiController;
import at.tgm.network.packets.C2SPOSTLehrerVorschlag;
import at.tgm.network.packets.S2CResponseLehrerVorschlag;
import at.tgm.objects.Lehrer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LehrerAnlegenPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(LehrerAnlegenPanel.class);
    
    private final DashboardFrame parent;
    
    // Formularfelder
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneNumberField;
    private JTextField displayNameField;
    private JTextArea beschreibungArea;
    private JTextField profilePictureUrlField;
    
    private JButton submitButton;
    private JLabel statusLabel;

    public LehrerAnlegenPanel(DashboardFrame parent, GuiController controller) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        
        JLabel header = new JLabel("Lehrer anlegen");
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
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("E-Mail:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Phone Number
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Telefonnummer:"), gbc);
        gbc.gridx = 1;
        phoneNumberField = new JTextField(20);
        panel.add(phoneNumberField, gbc);
        
        // Display Name
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Anzeigename:"), gbc);
        gbc.gridx = 1;
        displayNameField = new JTextField(20);
        panel.add(displayNameField, gbc);
        
        // Beschreibung
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Beschreibung:"), gbc);
        gbc.gridx = 1;
        beschreibungArea = new JTextArea(3, 20);
        beschreibungArea.setLineWrap(true);
        beschreibungArea.setWrapStyleWord(true);
        JScrollPane beschreibungScroll = new JScrollPane(beschreibungArea);
        panel.add(beschreibungScroll, gbc);
        
        // Profile Picture URL
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Profilbild-URL:"), gbc);
        gbc.gridx = 1;
        profilePictureUrlField = new JTextField(20);
        panel.add(profilePictureUrlField, gbc);
        
        // Status Label
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, gbc);
        
        return panel;
    }
    
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        submitButton = new JButton("Lehrer anlegen");
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
            
            // Erstelle Lehrer-Objekt
            Lehrer lehrer = new Lehrer(username, password);
            lehrer.setFirstName(firstNameField.getText().trim());
            lehrer.setLastName(lastNameField.getText().trim());
            lehrer.setEmail(emailField.getText().trim());
            lehrer.setPhoneNumber(phoneNumberField.getText().trim());
            lehrer.setDisplayName(displayNameField.getText().trim());
            lehrer.setBeschreibung(beschreibungArea.getText().trim());
            lehrer.setProfilePictureUrl(profilePictureUrlField.getText().trim());
            
            // Sende an Server
            submitButton.setEnabled(false);
            statusLabel.setText("Wird gesendet...");
            statusLabel.setForeground(Color.BLUE);
            
            // In separatem Thread ausführen
            new Thread(() -> {
                try {
                    C2SPOSTLehrerVorschlag request = new C2SPOSTLehrerVorschlag(lehrer);
                    S2CResponseLehrerVorschlag response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                            request,
                            S2CResponseLehrerVorschlag.class,
                            5,
                            TimeUnit.SECONDS
                        );
                    
                    // UI-Update im EDT
                    SwingUtilities.invokeLater(() -> {
                        submitButton.setEnabled(true);
                        if (response.isSuccess()) {
                            statusLabel.setText("Lehrer erfolgreich angelegt!");
                            statusLabel.setForeground(new Color(0, 150, 0));
                            JOptionPane.showMessageDialog(
                                parent,
                                "Lehrer wurde erfolgreich angelegt!",
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
                                "Fehler beim Anlegen des Lehrers:\n" + errorMessage,
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                    
                } catch (TimeoutException ex) {
                    logger.error("Timeout beim Senden des LehrerVorschlags", ex);
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
                    logger.error("Fehler beim Senden des LehrerVorschlags", ex);
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
                    logger.error("Unterbrochen beim Senden des LehrerVorschlags", ex);
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
        emailField.setText("");
        phoneNumberField.setText("");
        displayNameField.setText("");
        beschreibungArea.setText("");
        profilePictureUrlField.setText("");
    }
}
