package at.tgm.client.anmeldung;

import at.tgm.client.Client;
import at.tgm.client.ClientNetworkController;
import at.tgm.client.FehlerFrame;
import at.tgm.network.packets.C2SAuthenticationPacket;
import at.tgm.objects.Nutzer;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AnmeldeController {
    public AnmeldeFrame frame ;
    public FehlerFrame ff;

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

            //TODO: Client Side validation von username und passwort

            C2SAuthenticationPacket packet = new C2SAuthenticationPacket(username, password);

            try {
                ClientNetworkController.socketClient.send(packet);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        };
    }

}
