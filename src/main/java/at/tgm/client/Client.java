package at.tgm.client;

import at.tgm.client.anmeldung.AnmeldeController;
import at.tgm.client.dashboard.DashboardFrame;
import at.tgm.network.packets.C2SGETStats;
import at.tgm.network.packets.C2SHelloPacket;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static GuiController GUI = new GuiController();

    // für bestehenden Code weiterhin verfügbar:
    public static AnmeldeController ac;
    public static DashboardFrame dashboardFrame;
    public static Nutzer nutzer;

    public static void main(String[] args) throws IOException {
        logger.info("Client wird gestartet...");

        ClientNetworkController.connect();
        logger.debug("Verbindung zum Server hergestellt");

        // Login-GUI starten
        logger.debug("Zeige Login-GUI");
        GUI.showLogin();

        // Hallo-Paket an den Server
        logger.debug("Sende Hello-Paket an Server");
        ClientNetworkController.socketClient.send(new C2SHelloPacket("MAC_OS"));
    }

    // Wird vom Netzwerkcode aufgerufen
    public static void failedLogin() {
        logger.warn("Login fehlgeschlagen");
        GUI.loginFailed("Failed Login", "Check Username or Password!");
    }

    // Wird vom Netzwerkcode aufgerufen
    public static void login(Nutzer n) {
        logger.info("Login erfolgreich für Benutzer: {}", n != null ? n.getUsername() : "unknown");
        Client.nutzer = n;
        GUI.loginSuccessful(n);
    }

    // Wird vom Netzwerkcode aufgerufen, sobald Fachbegriffe fürs Quiz da sind
    public static void startQuiz(FachbegriffItem[] items) {
        logger.info("Quiz gestartet mit {} Items", items != null ? items.length : 0);
        GUI.showQuiz(items);
    }
}
