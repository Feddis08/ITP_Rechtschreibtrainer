package at.tgm.client.anmeldung;

import at.tgm.client.ClientNetworkController;
import at.tgm.client.FehlerFrame;
import at.tgm.network.packets.C2SAuthenticationPacket;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AnmeldeController {
    public AnmeldeFrame frame;
    public FehlerFrame ff;

    public AnmeldeController() {
        SwingUtilities.invokeLater(() -> {
            frame = new AnmeldeFrame(this);
            frame.setVisible(true);
        });
    }

    public void show() {
        if (frame != null) {
            frame.setVisible(true);
        }
    }

    public void hide() {
        if (frame != null) {
            frame.setVisible(false);
        }
    }

    public void showError(String title, String message) {
        ff = new FehlerFrame(title, message);
    }

    public ActionListener onLogin() {
        return e -> {
            String username = frame.getUsername();
            String password = frame.getPassword();
            // TODO: clientseitige Validierung

            C2SAuthenticationPacket packet = new C2SAuthenticationPacket(username, password);
            try {
                ClientNetworkController.socketClient.send(packet);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
