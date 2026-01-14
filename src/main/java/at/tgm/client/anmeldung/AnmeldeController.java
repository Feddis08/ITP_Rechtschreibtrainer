package at.tgm.client.anmeldung;

import at.tgm.client.ClientNetworkController;
import at.tgm.client.FehlerFrame;
import at.tgm.network.core.ResponsePacket;
import at.tgm.network.packets.C2SAuthenticationPacket;
import at.tgm.network.packets.S2CLoginFailedPacket;
import at.tgm.network.packets.S2CLoginPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

            // In separatem Thread ausführen, um UI nicht zu blockieren
            new Thread(() -> {
                try {
                    C2SAuthenticationPacket request = new C2SAuthenticationPacket(username, password);
                    ResponsePacket response = ClientNetworkController.socketClient
                        .getChannel()
                        .sendAndWait(
                            request,
                            ResponsePacket.class, // Basis-Typ, da es zwei mögliche Responses gibt
                            5,
                            TimeUnit.SECONDS
                        );
                    
                    // Prüfen, welche Response es ist
                    if (response instanceof S2CLoginPacket loginResponse) {
                        // Login erfolgreich
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            at.tgm.client.Client.login(loginResponse.getNutzer());
                        });
                    } else if (response instanceof S2CLoginFailedPacket) {
                        // Login fehlgeschlagen
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            at.tgm.client.Client.failedLogin();
                        });
                    } else {
                        logger.error("Unerwartete Response-Klasse: {}", response.getClass().getSimpleName());
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            showError("Fehler", "Unerwartete Antwort vom Server");
                        });
                    }
                    
                } catch (TimeoutException e1) {
                    logger.error("Timeout beim Login", e1);
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        showError("Timeout", "Keine Antwort vom Server (Timeout)");
                    });
                } catch (IOException e1) {
                    logger.error("Fehler beim Login", e1);
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        showError("Fehler", "Fehler beim Senden des Login-Requests: " + e1.getMessage());
                    });
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    logger.error("Unterbrochen beim Login", e1);
                }
            }).start();
        };
    }
}
