package at.tgm.integration;

import at.tgm.objects.Nutzer;
import at.tgm.objects.Schueler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for authentication functionality.
 * 
 * These tests:
 * - Start a test server instance
 * - Create a headless client (bot) that connects to the server
 * - Test authentication with wrong and correct credentials
 */
public class AuthenticationTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationTest.class);
    
    private HeadlessTestClient testClient;
    private static int serverPort;

    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("=== Setup: Starte Test-Server ===");
        serverPort = TestServerManager.startServer();
        
        // Give server a moment to fully start
        Thread.sleep(200);
        
        assertTrue(TestServerManager.isRunning(), "Server sollte laufen");
        assertTrue(serverPort > 0, "Server-Port sollte gesetzt sein");
        logger.info("Test-Server läuft auf Port {}", serverPort);
    }

    @AfterAll
    public static void tearDownServer() {
        logger.info("=== Teardown: Stoppe Test-Server ===");
        TestServerManager.stopServer();
        logger.info("Test-Server gestoppt");
    }

    @BeforeEach
    public void setUpClient() throws Exception {
        logger.info("--- Setup: Erstelle neuen Test-Client ---");
        testClient = new HeadlessTestClient();
        testClient.connect("localhost", serverPort);
        
        // Give connection a moment to establish
        Thread.sleep(100);
        
        assertTrue(testClient.isConnected(), "Client sollte verbunden sein");
        logger.info("Test-Client erfolgreich verbunden");
    }

    @AfterEach
    public void tearDownClient() {
        logger.info("--- Teardown: Trenne Test-Client ---");
        if (testClient != null) {
            testClient.disconnect();
        }
        logger.info("Test-Client getrennt");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testAuthenticationWithWrongCredentials() throws Exception {
        logger.info(">>> Test: Authentifizierung mit falschen Credentials <<<");
        
        // Test 1: Wrong username
        logger.info("Versuche Login mit falschem Benutzernamen...");
        try {
            boolean result = testClient.authenticate("nonexistent_user", "123", 5);
            assertFalse(result, "Authentifizierung sollte mit falschem Benutzernamen fehlschlagen");
            assertNull(testClient.getAuthenticatedUser(), "Kein Benutzer sollte authentifiziert sein");
            logger.info("✓ Login mit falschem Benutzernamen korrekt abgelehnt");
        } catch (TimeoutException e) {
            fail("Server hat nicht innerhalb des Timeouts geantwortet: " + e.getMessage());
        }
        
        // Test 2: Wrong password
        logger.info("Versuche Login mit falschem Passwort...");
        // Need new client connection since previous one might be in a bad state
        testClient.disconnect();
        Thread.sleep(100);
        testClient = new HeadlessTestClient();
        testClient.connect("localhost", serverPort);
        Thread.sleep(100);
        
        try {
            boolean result = testClient.authenticate("riemer", "wrong_password", 5);
            assertFalse(result, "Authentifizierung sollte mit falschem Passwort fehlschlagen");
            assertNull(testClient.getAuthenticatedUser(), "Kein Benutzer sollte authentifiziert sein");
            logger.info("✓ Login mit falschem Passwort korrekt abgelehnt");
        } catch (TimeoutException e) {
            fail("Server hat nicht innerhalb des Timeouts geantwortet: " + e.getMessage());
        }
        
        // Test 3: Empty credentials
        logger.info("Versuche Login mit leeren Credentials...");
        testClient.disconnect();
        Thread.sleep(100);
        testClient = new HeadlessTestClient();
        testClient.connect("localhost", serverPort);
        Thread.sleep(100);
        
        try {
            boolean result = testClient.authenticate("", "", 5);
            assertFalse(result, "Authentifizierung sollte mit leeren Credentials fehlschlagen");
            assertNull(testClient.getAuthenticatedUser(), "Kein Benutzer sollte authentifiziert sein");
            logger.info("✓ Login mit leeren Credentials korrekt abgelehnt");
        } catch (TimeoutException e) {
            fail("Server hat nicht innerhalb des Timeouts geantwortet: " + e.getMessage());
        }
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testAuthenticationWithCorrectCredentials() throws Exception {
        logger.info(">>> Test: Authentifizierung mit korrekten Credentials <<<");
        
        // Test with correct credentials for Schüler
        logger.info("Versuche Login mit korrekten Credentials (Schüler: riemer/123)...");
        
        boolean result = false;
        try {
            result = testClient.authenticate("riemer", "123", 5);
        } catch (TimeoutException e) {
            fail("Server hat nicht innerhalb des Timeouts geantwortet: " + e.getMessage());
        }
        
        assertTrue(result, "Authentifizierung sollte mit korrekten Credentials erfolgreich sein");
        logger.info("✓ Authentifizierung erfolgreich");
        
        // Verify authenticated user
        Nutzer authenticatedUser = testClient.getAuthenticatedUser();
        assertNotNull(authenticatedUser, "Authentifizierter Benutzer sollte nicht null sein");
        assertEquals("riemer", authenticatedUser.getUsername(), "Benutzername sollte 'riemer' sein");
        assertInstanceOf(Schueler.class, authenticatedUser, "Benutzer sollte ein Schüler sein");
        
        Schueler schueler = (Schueler) authenticatedUser;
        assertEquals("Felix", schueler.getFirstName(), "Vorname sollte 'Felix' sein");
        assertEquals("Riemer", schueler.getLastName(), "Nachname sollte 'Riemer' sein");
        
        logger.info("✓ Authentifizierter Benutzer korrekt: {}", authenticatedUser.getUsername());
        
        // Verify connection is still alive
        assertTrue(testClient.isConnected(), "Verbindung sollte nach erfolgreicher Authentifizierung noch bestehen");
        logger.info("✓ Verbindung bleibt nach Authentifizierung bestehen");
        
        // Wait a bit and verify connection is still alive (for further testing)
        Thread.sleep(500);
        assertTrue(testClient.isConnected(), "Verbindung sollte auch nach Wartezeit noch bestehen");
        logger.info("✓ Verbindung bleibt stabil (bereit für weitere Tests)");
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }
}
