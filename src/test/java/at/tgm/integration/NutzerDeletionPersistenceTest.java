package at.tgm.integration;

import at.tgm.objects.Nutzer;
import at.tgm.objects.Schueler;
import at.tgm.server.DatabaseManager;
import at.tgm.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test für Nutzer-Löschung und Persistenz.
 * 
 * Test:
 * - Nutzer wird erstellt
 * - Nutzer wird gelöscht
 * - Server wird neu gestartet
 * - Gelöschter Nutzer sollte NICHT mehr vorhanden sein
 */
public class NutzerDeletionPersistenceTest {

    private static final Logger logger = LoggerFactory.getLogger(NutzerDeletionPersistenceTest.class);
    private static int serverPort;

    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("=== Setup: Starte Test-Server ===");
        
        // Ensure database is initialized
        if (!DatabaseManager.getInstance().isInitialized()) {
            DatabaseManager.getInstance().initialize();
            at.tgm.server.DatabaseSchema.createTables();
        }
        
        // Clear test data before starting
        clearTestNutzer();
        
        serverPort = TestServerManager.startServer();
        Thread.sleep(500);
        assertTrue(TestServerManager.isRunning(), "Server sollte laufen");
        logger.info("Test-Server läuft auf Port {}", serverPort);
    }

    @AfterAll
    public static void tearDownServer() {
        logger.info("=== Teardown: Stoppe Test-Server ===");
        clearTestNutzer(); // Clean up test data
        TestServerManager.stopServer();
    }

    /**
     * Löscht Test-Nutzer aus der Datenbank.
     */
    private static void clearTestNutzer() {
        if (!DatabaseManager.getInstance().isInitialized()) {
            return;
        }
        
        try {
            java.sql.Connection conn = DatabaseManager.getConnection();
            try {
                String sql = "DELETE FROM nutzer WHERE username LIKE 'TEST_%'";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
                conn.commit();
                logger.debug("Test-Nutzer aus Datenbank gelöscht");
            } catch (Exception e) {
                conn.rollback();
                logger.warn("Fehler beim Löschen von Test-Nutzer: {}", e.getMessage());
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.warn("Fehler beim Zugriff auf Datenbank: {}", e.getMessage());
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testDeletedNutzerDoesNotReappearAfterRestart() throws Exception {
        logger.info(">>> Test: Gelöschter Nutzer erscheint nicht nach Server-Neustart <<<");
        
        // Schritt 1: Erstelle Test-Nutzer
        logger.info("Schritt 1: Erstelle Test-Nutzer...");
        Schueler testSchueler = new Schueler("TEST_SCHUELER_" + System.currentTimeMillis(), "123");
        testSchueler.setFirstName("Test");
        testSchueler.setLastName("Schueler");
        testSchueler.setAge(17);
        testSchueler.setSchoolClass("3BHIT");
        testSchueler.setDisplayName("Test S.");
        
        Server.addNutzer(testSchueler);
        Thread.sleep(500); // Warte auf DB-Schreibvorgang
        
        String username = testSchueler.getUsername();
        logger.info("✓ Test-Nutzer '{}' erstellt", username);
        
        // Schritt 2: Verifiziere, dass Nutzer existiert
        logger.info("Schritt 2: Verifiziere, dass Nutzer existiert...");
        Nutzer found = Server.findNutzerByUsername(username);
        assertNotNull(found, "Nutzer sollte im Server-Array vorhanden sein");
        assertTrue(isNutzerInDatabase(username), "Nutzer sollte in Datenbank vorhanden sein");
        logger.info("✓ Nutzer '{}' existiert im Server und in DB", username);
        
        // Schritt 3: Lösche Nutzer
        logger.info("Schritt 3: Lösche Nutzer '{}'...", username);
        Server.removeNutzer(testSchueler);
        Thread.sleep(500); // Warte auf DB-Löschvorgang
        
        // Verifiziere, dass Nutzer aus Server-Array entfernt wurde
        found = Server.findNutzerByUsername(username);
        assertNull(found, "Nutzer sollte nicht mehr im Server-Array sein");
        assertFalse(isNutzerInDatabase(username), "Nutzer sollte nicht mehr in Datenbank sein");
        logger.info("✓ Nutzer '{}' erfolgreich gelöscht", username);
        
        // Schritt 4: Stoppe Server
        logger.info("Schritt 4: Stoppe Server...");
        TestServerManager.stopServer();
        Thread.sleep(1000);
        logger.info("✓ Server gestoppt");
        
        // Schritt 5: Starte Server neu
        logger.info("Schritt 5: Starte Server neu...");
        serverPort = TestServerManager.startServer();
        Thread.sleep(1000);
        assertTrue(TestServerManager.isRunning(), "Server sollte wieder laufen");
        logger.info("✓ Server neu gestartet");
        
        // Schritt 6: Prüfe, dass gelöschter Nutzer NICHT wieder da ist
        logger.info("Schritt 6: Prüfe, dass gelöschter Nutzer NICHT wieder da ist...");
        found = Server.findNutzerByUsername(username);
        assertNull(found, "Gelöschter Nutzer sollte NICHT nach Server-Neustart wieder vorhanden sein");
        assertFalse(isNutzerInDatabase(username), "Gelöschter Nutzer sollte NICHT in Datenbank sein");
        logger.info("✓ Gelöschter Nutzer '{}' ist nach Neustart NICHT vorhanden", username);
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }

    /**
     * Prüft, ob ein Nutzer in der Datenbank vorhanden ist.
     */
    private boolean isNutzerInDatabase(String username) {
        if (!DatabaseManager.getInstance().isInitialized()) {
            return false;
        }
        
        try {
            java.sql.Connection conn = DatabaseManager.getConnection();
            try {
                String sql = "SELECT 1 FROM nutzer WHERE username = ? LIMIT 1";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Prüfen von Nutzer in DB: {}", e.getMessage());
        }
        return false;
    }
}
