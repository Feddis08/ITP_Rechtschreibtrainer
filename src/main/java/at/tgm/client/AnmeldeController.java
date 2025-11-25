package at.tgm.client;

import at.tgm.server.Server;
import at.tgm.client.AnmeldeFrame;
import at.tgm.objects.Nutzer;

import javax.swing.*;
import java.awt.event.ActionListener;

public class AnmeldeController {
    AnmeldeFrame frame ;

    public AnmeldeController(){
        SwingUtilities.invokeLater(() -> {
            frame = new AnmeldeFrame(this);
            frame.setVisible(true);
        });
    }



    public ActionListener onLogin() {
        return e -> {
            String username = frame.getUsername();
            String password = frame.getPassword();

        };
    }

}
