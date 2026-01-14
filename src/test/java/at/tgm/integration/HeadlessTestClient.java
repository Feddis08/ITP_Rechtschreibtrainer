package at.tgm.integration;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkSystem;
import at.tgm.network.core.SocketClient;
import at.tgm.network.packets.*;
import at.tgm.objects.Distro;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Schueler;
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
    
    // Quiz state
    private final AtomicReference<FachbegriffItem[]> quizItems = new AtomicReference<>(null);
    private final AtomicReference<FachbegriffItem[]> quizResultItems = new AtomicReference<>(null);
    private final AtomicReference<Integer> quizPoints = new AtomicReference<>(null);
    private final AtomicReference<Integer> quizMaxPoints = new AtomicReference<>(null);
    private CountDownLatch quizStartLatch = new CountDownLatch(1);
    private CountDownLatch quizResultLatch = new CountDownLatch(1);
    
    // Schülerliste state
    private final AtomicReference<Schueler[]> schuelerList = new AtomicReference<>(null);
    private CountDownLatch schuelerListLatch = new CountDownLatch(1);
    
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
        
        // Register test callbacks with Client to intercept events without GUI
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
        
        Client.setTestQuizCallback(new Client.TestQuizCallback() {
            @Override
            public void onQuizStarted(FachbegriffItem[] items) {
                HeadlessTestClient.this.onQuizStarted(items);
            }
            
            @Override
            public void onQuizResult(FachbegriffItem[] items, int points, int maxPoints) {
                HeadlessTestClient.this.onQuizResult(items, points, maxPoints);
            }
        });
        
        Client.setTestSchuelerListCallback(new Client.TestSchuelerListCallback() {
            @Override
            public void onSchuelerListReceived(Schueler[] schueler) {
                HeadlessTestClient.this.onSchuelerListReceived(schueler);
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
     * Called when quiz starts.
     */
    public void onQuizStarted(FachbegriffItem[] items) {
        logger.info("Quiz gestartet mit {} Items", items != null ? items.length : 0);
        quizItems.set(items);
        quizStartLatch.countDown();
    }
    
    /**
     * Called when quiz result is received.
     */
    public void onQuizResult(FachbegriffItem[] items, int points, int maxPoints) {
        logger.info("Quiz-Ergebnis erhalten: {}/{} Punkte", points, maxPoints);
        quizResultItems.set(items);
        quizPoints.set(points);
        quizMaxPoints.set(maxPoints);
        quizResultLatch.countDown();
    }
    
    /**
     * Called when Schülerliste is received.
     */
    public void onSchuelerListReceived(Schueler[] schueler) {
        logger.info("Schülerliste erhalten: {} Schüler", schueler != null ? schueler.length : 0);
        schuelerList.set(schueler);
        schuelerListLatch.countDown();
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
        
        // Remove test callbacks
        Client.setTestCallback(null);
        Client.setTestQuizCallback(null);
        Client.setTestSchuelerListCallback(null);
        
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
    
    /**
     * Requests the Schülerliste (only works for Lehrer).
     * 
     * @param timeoutSeconds Timeout in seconds to wait for response
     * @return Array of Schueler, or null if failed/timed out
     * @throws TimeoutException if no response received within timeout
     * @throws IOException if packet sending fails
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public Schueler[] requestSchuelerList(int timeoutSeconds) 
            throws TimeoutException, IOException, InterruptedException {
        if (!connected) {
            throw new IllegalStateException("Client ist nicht verbunden");
        }
        
        logger.info("Frage Schülerliste an...");
        
        // Reset state
        schuelerList.set(null);
        schuelerListLatch = new CountDownLatch(1);
        
        // Send request packet
        C2SGETAllSchueler packet = new C2SGETAllSchueler();
        socketClient.send(packet);
        logger.debug("Schülerliste-Anfrage gesendet");
        
        // Wait for response with timeout
        boolean received = schuelerListLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        
        if (!received) {
            logger.error("Timeout beim Warten auf Schülerliste");
            throw new TimeoutException("Keine Antwort vom Server innerhalb von " + timeoutSeconds + " Sekunden");
        }
        
        Schueler[] result = schuelerList.get();
        logger.info("Schülerliste erhalten: {} Schüler", result != null ? result.length : 0);
        return result;
    }
    
    /**
     * Starts a quiz (only works for Schüler).
     * 
     * @param timeoutSeconds Timeout in seconds to wait for quiz to start
     * @return Array of FachbegriffItem (censored, without word), or null if failed
     * @throws TimeoutException if no response received within timeout
     * @throws IOException if packet sending fails
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public FachbegriffItem[] startQuiz(int timeoutSeconds) 
            throws TimeoutException, IOException, InterruptedException {
        if (!connected) {
            throw new IllegalStateException("Client ist nicht verbunden");
        }
        
        logger.info("Starte Quiz...");
        
        // Reset state
        quizItems.set(null);
        quizStartLatch = new CountDownLatch(1);
        
        // Send quiz init packet
        C2SINITQuiz packet = new C2SINITQuiz();
        socketClient.send(packet);
        logger.debug("Quiz-Initialisierungs-Paket gesendet");
        
        // Wait for response with timeout
        boolean received = quizStartLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        
        if (!received) {
            logger.error("Timeout beim Warten auf Quiz-Start");
            throw new TimeoutException("Keine Antwort vom Server innerhalb von " + timeoutSeconds + " Sekunden");
        }
        
        FachbegriffItem[] result = quizItems.get();
        logger.info("Quiz gestartet mit {} Items", result != null ? result.length : 0);
        return result;
    }
    
    /**
     * Submits quiz answers and waits for the result.
     * 
     * @param answers Array of FachbegriffItem with user answers (word field should contain the answer)
     * @param timeoutSeconds Timeout in seconds to wait for result
     * @return Quiz result containing items, points, and maxPoints
     * @throws TimeoutException if no response received within timeout
     * @throws IOException if packet sending fails
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public QuizResult submitQuizAnswers(FachbegriffItem[] answers, int timeoutSeconds) 
            throws TimeoutException, IOException, InterruptedException {
        if (!connected) {
            throw new IllegalStateException("Client ist nicht verbunden");
        }
        
        if (answers == null || answers.length == 0) {
            throw new IllegalArgumentException("Antworten-Array darf nicht null oder leer sein");
        }
        
        logger.info("Sende Quiz-Antworten ({} Items)...", answers.length);
        
        // Reset state
        quizResultItems.set(null);
        quizPoints.set(null);
        quizMaxPoints.set(null);
        quizResultLatch = new CountDownLatch(1);
        
        // Send quiz results packet
        C2SPOSTQuizResults packet = new C2SPOSTQuizResults(answers);
        socketClient.send(packet);
        logger.debug("Quiz-Antworten gesendet");
        
        // Wait for response with timeout
        boolean received = quizResultLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        
        if (!received) {
            logger.error("Timeout beim Warten auf Quiz-Ergebnis");
            throw new TimeoutException("Keine Antwort vom Server innerhalb von " + timeoutSeconds + " Sekunden");
        }
        
        FachbegriffItem[] resultItems = quizResultItems.get();
        Integer points = quizPoints.get();
        Integer maxPoints = quizMaxPoints.get();
        
        if (resultItems == null || points == null || maxPoints == null) {
            logger.error("Unerwarteter Zustand: Quiz-Ergebnis unvollständig");
            return null;
        }
        
        logger.info("Quiz-Ergebnis erhalten: {}/{} Punkte", points, maxPoints);
        return new QuizResult(resultItems, points, maxPoints);
    }
    
    /**
     * Result class for quiz submissions.
     */
    public static class QuizResult {
        public final FachbegriffItem[] items;
        public final int points;
        public final int maxPoints;
        
        public QuizResult(FachbegriffItem[] items, int points, int maxPoints) {
            this.items = items;
            this.points = points;
            this.maxPoints = maxPoints;
        }
    }
}
