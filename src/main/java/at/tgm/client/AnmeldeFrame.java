package at.tgm.client;

import javax.swing.*;
import java.awt.*;

public class AnmeldeFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private AnmeldeController ac;

    public AnmeldeFrame(AnmeldeController ac) {
        this.ac = ac;

        setTitle("Anmeldung");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // zentrieren
        setLayout(new BorderLayout());

        // ====== Center Panel (Form) ======
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(2, 2, 10, 10));

        formPanel.add(new JLabel("Benutzername:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Passwort:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        add(formPanel, BorderLayout.CENTER);

        // ====== Bottom Panel (Button) ======
        JButton loginButton = new JButton("Anmelden");
        loginButton.addActionListener(this.ac.onLogin());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public String getUsername(){
        return usernameField.getText();
    }
    public String getPassword(){
        return new String(passwordField.getPassword());
    }

}
