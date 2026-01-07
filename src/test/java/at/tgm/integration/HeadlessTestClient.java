package at.tgm.integration;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkSystem;
import at.tgm.network.core.SocketClient;
import at.tgm.network.packets.C2SAuthenticationPacket;
import at.tgm.network.packets.C2SHelloPacket;
import at.tgm.objects.Distro;
import at.tgm.objects.Nutzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Headless test client (bot) for integration testing.
 * Connects to the server without GUI dependencies and handles authentication.
 * 
 * Note: This client uses a test-specific callback mechanism to intercept
 * authentication responses without triggering GUI operations.
 */
public class HeadlessTestClient {

    private static final Logger logger = LoggerFactory.getLogger(HeadlessTestClient.class);
    
    private SocketClient socketClient;
    private Socket socket;
    private volatile boolean connected = false;
    
    // Authentication state
    private final AtomicReference<Boolean> loginSuccess = new AtomicReference<>(null);
    private final AtomicReference<Nutzer> authenticatedUser = new AtomicReference<>(null);
    private CountDownLatch loginResponseLatch = new CountDownLatch(1);
    
    // Store original Client callbacks to restore later
    private static volatile HeadlessTestClient activeTestClient = null;

    /**
     * Connects to the server at the specified host and port.
     * 
     * @param host The server hostname
     * @param port The server port
     * @throws IOException if connection fails
     */
    public void connect(String host, int port) throws IOException {
        if (connected) {
            logger.warn("Client ist bereits verbunden");
            return;
        }
        
        logger.info("Verbinde mit Server {}:{}", host, port);
        
        // Initialize NetworkSystem if not already done
        NetworkSystem.init();
        
        socket = new Socket(host, port);
        socketClient = new SocketClient(socket, Distro.CLIENT);
        
        // Set this as the active test client for callback interception
        activeTestClient = this;
        
        // Register test callback with Client to intercept login events without GUI
        Client.setTestCallback(new Client.TestLoginCallback() {
            @Override
            public void onLogin(Nutzer n) {
                HeadlessTestClient.this.onLoginSuccess(n);
            }
            
            @Override
            public void onLoginFailed() {
                HeadlessTestClient.this.onLoginFailed();
            }
        });
        
        connected = true;
        logger.info("Erfolgreich mit Server verbunden");
        
        // Send hello packet (as real client does)
        try {
            socketClient.send(new C2SHelloPacket("TEST_BOT"));
            logger.debug("Hello-Paket gesendet");
        } catch (IOException e) {
            logger.error("Fehler beim Senden des Hello-Pakets", e);
        }
    }
    
    /**
     * Called by test-specific Client wrapper when login succeeds.
     * This bypasses GUI operations.
     */
    public void onLoginSuccess(Nutzer user) {
        logger.info("Login erfolgreich für Benutzer: {}", user != null ? user.getUsername() : "unknown");
        loginSuccess.set(true);
        authenticatedUser.set(user);
        loginResponseLatch.countDown();
    }
    
    /**
     * Called by test-specific Client wrapper when login fails.
     * This bypasses GUI operations.
     */
    public void onLoginFailed() {
        logger.info("Login fehlgeschlagen");
        loginSuccess.set(false);
        authenticatedUser.set(null);
        loginResponseLatch.countDown();
    }
    
    /**
     * Gets the active test client instance for callback interception.
     */
    public static HeadlessTestClient getActiveTestClient() {
        return activeTestClient;
    }

    /**
     * Attempts to authenticate with the given credentials.
     * 
     * @param username The username
     * @param password The password
     * @param timeoutSeconds Timeout in seconds to wait for response
     * @return true if authentication succeeded, false if it failed
     * @throws TimeoutException if no response received within timeout
     * @throws IOException if packet sending fails
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public boolean authenticate(String username, String password, int timeoutSeconds) 
            throws TimeoutException, IOException, InterruptedException {
        if (!connected) {
            throw new IllegalStateException("Client ist nicht verbunden");
        }
        
        logger.info("Versuche Authentifizierung mit Benutzer: {}", username);
        
        // Reset state
        loginSuccess.set(null);
        authenticatedUser.set(null);
        loginResponseLatch = new CountDownLatch(1);
        
        // Send authentication packet
        C2SAuthenticationPacket authPacket = new C2SAuthenticationPacket(username, password);
        socketClient.send(authPacket);
        logger.debug("Authentifizierungs-Paket gesendet");
        
        // Wait for response with timeout
        boolean received = loginResponseLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        
        if (!received) {
            logger.error("Timeout beim Warten auf Authentifizierungs-Antwort");
            throw new TimeoutException("Keine Antwort vom Server innerhalb von " + timeoutSeconds + " Sekunden");
        }
        
        Boolean result = loginSuccess.get();
        if (result == null) {
            logger.error("Unerwarteter Zustand: Login-Antwort erhalten, aber Ergebnis ist null");
            return false;
        }
        
        logger.info("Authentifizierung abgeschlossen: {}", result ? "ERFOLGREICH" : "FEHLGESCHLAGEN");
        return result;
    }

    /**
     * Gets the authenticated user, if authentication was successful.
     * 
     * @return The authenticated Nutzer object, or null if not authenticated
     */
    public Nutzer getAuthenticatedUser() {
        return authenticatedUser.get();
    }

    /**
     * Checks if the client is currently connected to the server.
     * 
     * @return true if connected
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed() && socket.isConnected();
    }

    /**
     * Disconnects from the server and cleans up resources.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        logger.info("Trenne Verbindung zum Server");
        connected = false;
        activeTestClient = null;
        
        // Remove test callback
        Client.setTestCallback(null);
        
        // Close socket
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.warn("Fehler beim Schließen des Sockets", e);
        }
        
        logger.info("Verbindung getrennt");
    }
}
