package at.tgm.client;

import at.tgm.client.anmeldung.AnmeldeController;
import at.tgm.client.dashboard.DashboardFrame;
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

    // Test callback interface - allows tests to intercept login events without GUI
    private static TestLoginCallback testCallback = null;
    private static TestQuizCallback testQuizCallback = null;
    private static TestSchuelerListCallback testSchuelerListCallback = null;
    
    /**
     * Sets a test callback for headless testing. Should only be used in test code.
     * @param callback The test callback, or null to disable test mode
     */
    public static void setTestCallback(TestLoginCallback callback) {
        testCallback = callback;
    }
    
    public static void setTestQuizCallback(TestQuizCallback callback) {
        testQuizCallback = callback;
    }
    
    public static void setTestSchuelerListCallback(TestSchuelerListCallback callback) {
        testSchuelerListCallback = callback;
    }
    
    public interface TestLoginCallback {
        void onLogin(Nutzer n);
        void onLoginFailed();
    }
    
    public interface TestQuizCallback {
        void onQuizStarted(at.tgm.objects.FachbegriffItem[] items);
        void onQuizResult(at.tgm.objects.FachbegriffItem[] items, int points, int maxPoints);
    }
    
    public interface TestSchuelerListCallback {
        void onSchuelerListReceived(at.tgm.objects.Schueler[] schueler);
    }

    // Wird vom Netzwerkcode aufgerufen
    public static void failedLogin() {
        logger.warn("Login fehlgeschlagen");
        
        // If in test mode, route to test callback instead of GUI
        if (testCallback != null) {
            testCallback.onLoginFailed();
            return;
        }
        
        // GUI-Operationen müssen im EDT (Event Dispatch Thread) ausgeführt werden
        javax.swing.SwingUtilities.invokeLater(() -> {
            GUI.loginFailed("Failed Login", "Check Username or Password!");
        });
    }

    // Wird vom Netzwerkcode aufgerufen
    public static void login(Nutzer n) {
        logger.info("Login erfolgreich für Benutzer: {}", n != null ? n.getUsername() : "unknown");
        Client.nutzer = n;
        
        // If in test mode, route to test callback instead of GUI
        if (testCallback != null) {
            testCallback.onLogin(n);
            return;
        }
        
        // GUI-Operationen müssen im EDT (Event Dispatch Thread) ausgeführt werden
        javax.swing.SwingUtilities.invokeLater(() -> {
            GUI.loginSuccessful(n);
        });
    }

    // Wird vom Netzwerkcode aufgerufen, sobald Fachbegriffe fürs Quiz da sind
    public static void startQuiz(FachbegriffItem[] items) {
        logger.info("Quiz gestartet mit {} Items", items != null ? items.length : 0);
        
        // If in test mode, route to test callback instead of GUI
        if (testQuizCallback != null) {
            testQuizCallback.onQuizStarted(items);
            return;
        }
        
        // GUI-Operationen müssen im EDT (Event Dispatch Thread) ausgeführt werden
        javax.swing.SwingUtilities.invokeLater(() -> {
            GUI.showQuiz(items);
        });
    }
    
    // Wird vom Netzwerkcode aufgerufen, wenn Quiz-Ergebnis kommt
    public static void onQuizResult(at.tgm.objects.FachbegriffItem[] items, int points, int maxPoints) {
        logger.info("Quiz-Ergebnis erhalten: {}/{} Punkte ({} Items)", points, maxPoints, 
                   items != null ? items.length : 0);
        
        // If in test mode, route to test callback instead of GUI
        if (testQuizCallback != null) {
            testQuizCallback.onQuizResult(items, points, maxPoints);
            return;
        }
        
        // Normalerweise würde hier die GUI aufgerufen werden, aber das machen wir über S2CResultOfQuiz
    }
    
    // Wird vom Netzwerkcode aufgerufen, wenn Schülerliste kommt
    public static void onSchuelerListReceived(at.tgm.objects.Schueler[] schueler) {
        logger.info("Schülerliste erhalten: {} Schüler", schueler != null ? schueler.length : 0);
        
        // If in test mode, route to test callback instead of GUI
        if (testSchuelerListCallback != null) {
            testSchuelerListCallback.onSchuelerListReceived(schueler);
            return;
        }
        
        // GUI-Operationen müssen im EDT (Event Dispatch Thread) ausgeführt werden
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (dashboardFrame != null) {
                dashboardFrame.showSchuelerList(schueler);
            }
        });
    }

    // Wird vom Netzwerkcode aufgerufen, wenn die Verbindung verloren geht
    public static void connectionLost() {
        logger.error("Verbindung zum Server verloren - Client wird beendet");
        
        // Zeige Fehlermeldung im EDT und beende nach Anzeige
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(
                null,
                "Verbindung zum Server verloren.\nDie Anwendung wird beendet.",
                "Verbindungsfehler",
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
            
            // Beende die Anwendung
            System.exit(1);
        });
    }
}
