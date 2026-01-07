package at.tgm.integration;

import at.tgm.objects.Lehrer;
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
 * Integration tests for Schülerliste functionality.
 * 
 * Tests:
 * - Schüler können NICHT die Schülerliste abrufen
 * - Lehrer können die Schülerliste abrufen
 */
public class SchuelerListTest {

    private static final Logger logger = LoggerFactory.getLogger(SchuelerListTest.class);
    
    private HeadlessTestClient testClient;
    private static int serverPort;

    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("=== Setup: Starte Test-Server ===");
        serverPort = TestServerManager.startServer();
        Thread.sleep(200);
        assertTrue(TestServerManager.isRunning(), "Server sollte laufen");
        logger.info("Test-Server läuft auf Port {}", serverPort);
    }

    @AfterAll
    public static void tearDownServer() {
        logger.info("=== Teardown: Stoppe Test-Server ===");
        TestServerManager.stopServer();
    }

    @BeforeEach
    public void setUpClient() throws Exception {
        logger.info("--- Setup: Erstelle neuen Test-Client ---");
        testClient = new HeadlessTestClient();
        testClient.connect("localhost", serverPort);
        Thread.sleep(100);
        assertTrue(testClient.isConnected(), "Client sollte verbunden sein");
    }

    @AfterEach
    public void tearDownClient() {
        logger.info("--- Teardown: Trenne Test-Client ---");
        if (testClient != null) {
            testClient.disconnect();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testSchuelerCannotGetSchuelerList() throws Exception {
        logger.info(">>> Test: Schüler kann NICHT Schülerliste abrufen <<<");
        
        // Authenticate as Schüler
        logger.info("Authentifiziere als Schüler (riemer/123)...");
        boolean authSuccess = testClient.authenticate("riemer", "123", 5);
        assertTrue(authSuccess, "Authentifizierung sollte erfolgreich sein");
        
        Schueler schueler = (Schueler) testClient.getAuthenticatedUser();
        assertNotNull(schueler, "Authentifizierter Benutzer sollte ein Schüler sein");
        assertEquals("riemer", schueler.getUsername());
        logger.info("✓ Als Schüler authentifiziert");
        
        // Try to request Schülerliste - should fail
        logger.info("Versuche Schülerliste abzurufen...");
        try {
            Schueler[] result = testClient.requestSchuelerList(3);
            
            // If we get here, the request didn't throw an exception
            // But the server should reject it - check if result is null or empty
            // Actually, the server throws UnsupportedOperationException which should 
            // be caught and logged, but the client won't receive a response
            // So we expect a timeout or null result
            
            // Since the server throws an exception internally, we might not get a response
            // The test should timeout or we should verify the exception was thrown server-side
            fail("Schüler sollte NICHT die Schülerliste abrufen können - Request sollte fehlschlagen");
            
        } catch (TimeoutException e) {
            // Expected: Server rejects the request and doesn't send a response
            logger.info("✓ Timeout wie erwartet - Server hat Anfrage abgelehnt");
            // This is the expected behavior
        } catch (Exception e) {
            // Other exceptions might also be acceptable
            logger.info("✓ Exception wie erwartet: {}", e.getClass().getSimpleName());
            // Verify it's a rejection
            assertTrue(e.getMessage() != null && 
                      (e.getMessage().contains("nicht erlaubt") || 
                       e.getMessage().contains("UnsupportedOperationException") ||
                       e instanceof TimeoutException),
                      "Exception sollte eine Ablehnung anzeigen");
        }
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testLehrerCanGetSchuelerList() throws Exception {
        logger.info(">>> Test: Lehrer kann Schülerliste abrufen <<<");
        
        // Authenticate as Lehrer
        logger.info("Authentifiziere als Lehrer (l/123)...");
        boolean authSuccess = testClient.authenticate("l", "123", 5);
        assertTrue(authSuccess, "Authentifizierung sollte erfolgreich sein");
        
        Lehrer lehrer = (Lehrer) testClient.getAuthenticatedUser();
        assertNotNull(lehrer, "Authentifizierter Benutzer sollte ein Lehrer sein");
        assertEquals("l", lehrer.getUsername());
        logger.info("✓ Als Lehrer authentifiziert");
        
        // Request Schülerliste - should succeed
        logger.info("Frage Schülerliste an...");
        Schueler[] schuelerList = testClient.requestSchuelerList(5);
        
        assertNotNull(schuelerList, "Schülerliste sollte nicht null sein");
        assertTrue(schuelerList.length > 0, "Schülerliste sollte mindestens einen Schüler enthalten");
        
        logger.info("✓ Schülerliste erhalten mit {} Schülern", schuelerList.length);
        
        // Verify the list contains expected students
        boolean foundRiemer = false;
        for (Schueler s : schuelerList) {
            if (s != null && "riemer".equals(s.getUsername())) {
                foundRiemer = true;
                logger.info("✓ Schüler 'riemer' in Liste gefunden");
                break;
            }
        }
        assertTrue(foundRiemer, "Schülerliste sollte 'riemer' enthalten");
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }
}
