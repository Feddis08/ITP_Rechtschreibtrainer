package at.tgm.client;

import javax.swing.*;

public class FehlerFrame extends JFrame {

    public FehlerFrame(String message, String messageType){

        setTitle(messageType);
        setSize(350, 50);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // zentrieren

        JLabel textField  = new JLabel();
        textField.setText(message);

        this.add(textField);
        this.setResizable(false);


        this.setVisible(true);
    }
}
