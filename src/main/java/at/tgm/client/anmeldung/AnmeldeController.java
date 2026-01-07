package at.tgm.client.anmeldung;

import at.tgm.client.ClientNetworkController;
import at.tgm.client.FehlerFrame;
import at.tgm.network.packets.C2SAuthenticationPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AnmeldeController {

    private static final Logger logger = LoggerFactory.getLogger(AnmeldeController.class);
    public AnmeldeFrame frame;
    public FehlerFrame ff;

    public AnmeldeController() {
        logger.debug("Erstelle AnmeldeController");
        SwingUtilities.invokeLater(() -> {
            frame = new AnmeldeFrame(this);
            frame.setVisible(true);
            logger.debug("AnmeldeFrame erstellt und angezeigt");
        });
    }

    public void show() {
        logger.debug("Zeige AnmeldeFrame");
        if (frame != null) {
            frame.setVisible(true);
        }
    }

    public void hide() {
        logger.debug("Verberge AnmeldeFrame");
        if (frame != null) {
            frame.setVisible(false);
        }
    }

    public void showError(String title, String message) {
        logger.warn("Zeige Fehler-Dialog: {} - {}", title, message);
        ff = new FehlerFrame(title, message);
    }

    public ActionListener onLogin() {
        return e -> {
            String username = frame.getUsername();
            String password = frame.getPassword();
            logger.info("Login-Versuch für Benutzer: {}", username);
            // TODO: clientseitige Validierung

            C2SAuthenticationPacket packet = new C2SAuthenticationPacket(username, password);
            try {
                ClientNetworkController.socketClient.send(packet);
                logger.debug("Authentifizierungs-Paket gesendet");
            } catch (IOException ex) {
                logger.error("Fehler beim Senden des Authentifizierungs-Pakets", ex);
                throw new RuntimeException(ex);
            }
        };
    }
}
