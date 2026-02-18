package at.tgm.integration;

import at.tgm.objects.Lehrer;
import at.tgm.objects.Nutzer;
import at.tgm.objects.NutzerStatus;
import at.tgm.objects.Schueler;
import at.tgm.server.Server;
import at.tgm.server.ServerNetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;

/**
 * Manages the test server lifecycle.
 * Starts and stops the server for integration tests.
 */
public class TestServerManager {

    private static final Logger logger = LoggerFactory.getLogger(TestServerManager.class);
    
    private static Thread serverThread;
    private static int serverPort = -1;
    private static volatile boolean serverRunning = false;

    /**
     * Starts the test server on an available port.
     * Initializes test users and starts the server in a separate thread.
     * 
     * @return The port number the server is running on
     * @throws Exception if server fails to start
     */
    public static int startServer() throws Exception {
        if (serverRunning) {
            logger.warn("Server ist bereits gestartet auf Port {}", serverPort);
            return serverPort;
        }

        logger.info("Starte Test-Server...");
        
        // Initialize NetworkSystem
        at.tgm.network.core.NetworkSystem.init();
        
        // Initialize database (important for persistence tests)
        logger.info("Initialisiere Datenbank für Tests...");
        try {
            at.tgm.server.DatabaseManager.getInstance().initialize();
            at.tgm.server.DatabaseSchema.createTables();
            logger.info("✓ Datenbank erfolgreich initialisiert");
        } catch (Exception e) {
            logger.warn("Fehler beim Initialisieren der Datenbank (Tests können trotzdem laufen): {}", e.getMessage());
        }
        
        // Initialize test users (similar to Server.main())
        Server.nutzers = new Nutzer[1];
        
        // Create test Schüler
        Schueler felix = new Schueler("riemer", "123");
        felix.setFirstName("Felix");
        felix.setLastName("Riemer");
        felix.setAge(17);
        felix.setSchoolClass("3BHIT");
        felix.setDisplayName("Felix R.");
        felix.setBeschreibung("Tech enthusiast");
        felix.setStatus(NutzerStatus.ONLINE);
        felix.setEmail("felix@test.com");
        felix.setPhoneNumber("+43 660 1234567");
        felix.setProfilePictureUrl("https://example.com/pic.jpg");
        Server.addNutzer(felix);
        logger.debug("Test-Schüler 'riemer' hinzugefügt");
        
        // Create test Lehrer
        Lehrer lehrer = new Lehrer("l", "123");
        Server.addNutzer(lehrer);
        logger.debug("Test-Lehrer 'l' hinzugefügt");

        // Find available port
        try (ServerSocket testSocket = new ServerSocket(0)) {
            serverPort = testSocket.getLocalPort();
        }
        
        logger.info("Starte Server auf Port {}", serverPort);
        
        // Start server in separate thread (ServerNetworkController.start() is blocking)
        serverThread = new Thread(() -> {
            try {
                serverRunning = true;
                logger.debug("Server-Thread startet ServerNetworkController");
                ServerNetworkController.start(serverPort);
            } catch (Exception e) {
                logger.error("Fehler beim Starten des Test-Servers", e);
                serverRunning = false;
            }
        });
        serverThread.setDaemon(true);
        serverThread.setName("TestServerThread");
        serverThread.start();
        
        // Wait a bit for server to start and bind to port
        Thread.sleep(500);
        
        if (!serverRunning) {
            throw new Exception("Server konnte nicht gestartet werden");
        }
        
        logger.info("Test-Server erfolgreich gestartet auf Port {}", serverPort);
        return serverPort;
    }

    /**
     * Stops the test server and cleans up resources.
     */
    public static void stopServer() {
        if (!serverRunning) {
            logger.debug("Server läuft nicht, nichts zu stoppen");
            return;
        }
        
        logger.info("Stoppe Test-Server...");
        serverRunning = false;
        
        // Close all client connections
        if (ServerNetworkController.clients != null) {
            for (at.tgm.network.core.SocketClient client : ServerNetworkController.clients) {
                if (client != null) {
                    try {
                        if (!client.getSocket().isClosed()) {
                            client.getSocket().close();
                        }
                    } catch (Exception e) {
                        logger.warn("Fehler beim Schließen eines Client-Sockets", e);
                    }
                }
            }
        }
        
        // Reset server state
        Server.nutzers = new Nutzer[1];
        ServerNetworkController.clients = new at.tgm.network.core.SocketClient[0];
        
        logger.info("Test-Server gestoppt");
    }

    /**
     * Gets the port the server is running on.
     * 
     * @return The server port, or -1 if server is not running
     */
    public static int getPort() {
        return serverPort;
    }

    /**
     * Checks if the server is currently running.
     * 
     * @return true if server is running
     */
    public static boolean isRunning() {
        return serverRunning;
    }
}
