package at.tgm.integration;

import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import at.tgm.server.DatabaseManager;
import at.tgm.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for database persistence.
 * 
 * Tests:
 * - Fachbegriffe werden in Datenbank gespeichert
 * - Quiz-Templates werden in Datenbank gespeichert
 * - Daten werden nach Server-Neustart geladen
 * - Updates werden in Datenbank gespeichert
 * - Deletes werden aus Datenbank entfernt
 */
public class DatabasePersistenceTest {

    private static final Logger logger = LoggerFactory.getLogger(DatabasePersistenceTest.class);
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
        clearTestData();
        
        serverPort = TestServerManager.startServer();
        Thread.sleep(500);
        assertTrue(TestServerManager.isRunning(), "Server sollte laufen");
        logger.info("Test-Server läuft auf Port {}", serverPort);
    }

    @AfterAll
    public static void tearDownServer() {
        logger.info("=== Teardown: Stoppe Test-Server ===");
        clearTestData(); // Clean up test data
        TestServerManager.stopServer();
    }

    /**
     * Clears test data from database.
     */
    private static void clearTestData() {
        if (!DatabaseManager.getInstance().isInitialized()) {
            return;
        }
        
        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                // Delete quiz template items first (foreign key constraint)
                try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM quiz_template_items WHERE quiz_template_id LIKE 'TEST_%' OR quiz_template_id > 9999999999999")) {
                    stmt.executeUpdate();
                }
                
                // Delete quiz templates
                try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM quiz_template WHERE id LIKE 'TEST_%' OR id > 9999999999999")) {
                    stmt.executeUpdate();
                }
                
                // Delete test Fachbegriffe
                try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM fachbegriff_item WHERE id LIKE 'TEST_%' OR id > 9999999999999")) {
                    stmt.executeUpdate();
                }
                
                conn.commit();
                logger.debug("Test-Daten aus Datenbank gelöscht");
            } catch (Exception e) {
                conn.rollback();
                logger.warn("Fehler beim Löschen von Test-Daten: {}", e.getMessage());
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.warn("Fehler beim Zugriff auf Datenbank: {}", e.getMessage());
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testFachbegriffPersistence() throws Exception {
        logger.info(">>> Test: Fachbegriff-Persistenz <<<");
        
        // Create a test Fachbegriff
        long testId = System.currentTimeMillis();
        FachbegriffItem testItem = new FachbegriffItem(
            testId,
            "TEST_WORD_" + testId,
            1,
            1,
            2,
            "Test phrase for persistence"
        );
        
        logger.info("Erstelle Test-Fachbegriff: {}", testItem.getWord());
        Server.addFachbegriff(testItem);
        
        // Wait a bit for database write
        Thread.sleep(500);
        
        // Verify it's in database
        logger.info("Prüfe ob Fachbegriff in Datenbank gespeichert wurde...");
        assertTrue(isFachbegriffInDatabase(testId), 
                  "Fachbegriff sollte in Datenbank gespeichert sein");
        logger.info("✓ Fachbegriff in Datenbank gefunden");
        
        // Verify data integrity
        FachbegriffItem loaded = loadFachbegriffFromDatabase(testId);
        assertNotNull(loaded, "Fachbegriff sollte aus Datenbank geladen werden können");
        assertEquals(testItem.getWord(), loaded.getWord(), "Word sollte übereinstimmen");
        assertEquals(testItem.getLevel(), loaded.getLevel(), "Level sollte übereinstimmen");
        assertEquals(testItem.getPhrase(), loaded.getPhrase(), "Phrase sollte übereinstimmen");
        logger.info("✓ Datenintegrität bestätigt");
        
        // Test update
        logger.info("Teste Update...");
        FachbegriffItem updated = new FachbegriffItem(
            testId,
            "UPDATED_WORD_" + testId,
            2,
            2,
            3,
            "Updated phrase"
        );
        Server.updateFachbegriff(testId, updated);
        Thread.sleep(500);
        
        FachbegriffItem loadedUpdated = loadFachbegriffFromDatabase(testId);
        assertNotNull(loadedUpdated, "Aktualisierter Fachbegriff sollte geladen werden können");
        assertEquals("UPDATED_WORD_" + testId, loadedUpdated.getWord(), "Word sollte aktualisiert sein");
        logger.info("✓ Update erfolgreich");
        
        // Test delete
        logger.info("Teste Delete...");
        Server.removeFachbegriff(testId);
        Thread.sleep(500);
        
        assertFalse(isFachbegriffInDatabase(testId), 
                   "Fachbegriff sollte aus Datenbank gelöscht sein");
        logger.info("✓ Delete erfolgreich");
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testQuizTemplatePersistence() throws Exception {
        logger.info(">>> Test: Quiz-Template-Persistenz <<<");
        
        // Create test Fachbegriffe first
        long fachbegriffId1 = System.currentTimeMillis() + 1000000;
        long fachbegriffId2 = System.currentTimeMillis() + 2000000;
        
        FachbegriffItem item1 = new FachbegriffItem(
            fachbegriffId1,
            "TEST_ITEM_1",
            1,
            1,
            2,
            "Test item 1"
        );
        FachbegriffItem item2 = new FachbegriffItem(
            fachbegriffId2,
            "TEST_ITEM_2",
            1,
            1,
            2,
            "Test item 2"
        );
        
        Server.addFachbegriff(item1);
        Server.addFachbegriff(item2);
        Thread.sleep(500);
        
        // Create test Quiz Template
        long templateId = System.currentTimeMillis();
        Quiz testTemplate = new Quiz(
            "TEST_TEMPLATE_" + templateId,
            new FachbegriffItem[]{item1, item2}
        );
        testTemplate.setId(templateId);
        
        logger.info("Erstelle Test-Quiz-Template: {} (ID: {})", testTemplate.getName(), templateId);
        Server.addQuizTemplate(testTemplate);
        
        // Wait a bit for database write
        Thread.sleep(500);
        
        // Verify it's in database
        logger.info("Prüfe ob Quiz-Template in Datenbank gespeichert wurde...");
        assertTrue(isQuizTemplateInDatabase(templateId), 
                  "Quiz-Template sollte in Datenbank gespeichert sein");
        logger.info("✓ Quiz-Template in Datenbank gefunden");
        
        // Verify items are linked
        assertTrue(hasQuizTemplateItems(templateId, 2), 
                   "Quiz-Template sollte 2 Items haben");
        logger.info("✓ Quiz-Template-Items verifiziert");
        
        // Test update
        logger.info("Teste Update...");
        FachbegriffItem item3 = new FachbegriffItem(
            System.currentTimeMillis() + 3000000,
            "TEST_ITEM_3",
            1,
            1,
            2,
            "Test item 3"
        );
        Server.addFachbegriff(item3);
        Thread.sleep(500);
        
        Quiz updated = new Quiz(
            "UPDATED_TEMPLATE_" + templateId,
            new FachbegriffItem[]{item1, item2, item3}
        );
        updated.setId(templateId);
        Server.updateQuizTemplate(templateId, updated);
        Thread.sleep(500);
        
        assertTrue(hasQuizTemplateItems(templateId, 3), 
                   "Aktualisiertes Quiz-Template sollte 3 Items haben");
        logger.info("✓ Update erfolgreich");
        
        // Test delete
        logger.info("Teste Delete...");
        Server.removeQuizTemplate(templateId);
        Thread.sleep(500);
        
        assertFalse(isQuizTemplateInDatabase(templateId), 
                   "Quiz-Template sollte aus Datenbank gelöscht sein");
        logger.info("✓ Delete erfolgreich");
        
        // Clean up test Fachbegriffe
        Server.removeFachbegriff(fachbegriffId1);
        Server.removeFachbegriff(fachbegriffId2);
        Server.removeFachbegriff(item3.getId());
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testDataSurvivesServerRestart() throws Exception {
        logger.info(">>> Test: Daten überleben Server-Neustart <<<");
        
        // Create test data
        long testId = System.currentTimeMillis();
        FachbegriffItem testItem = new FachbegriffItem(
            testId,
            "SURVIVAL_TEST_" + testId,
            1,
            1,
            2,
            "Should survive restart"
        );
        
        logger.info("Erstelle Test-Fachbegriff: {}", testItem.getWord());
        Server.addFachbegriff(testItem);
        Thread.sleep(500);
        
        // Verify it's in database
        assertTrue(isFachbegriffInDatabase(testId), 
                  "Fachbegriff sollte in Datenbank sein");
        
        // Simulate server restart by reloading from database
        logger.info("Simuliere Server-Neustart (lade Daten neu)...");
        
        // Clear in-memory array
        Server.fachbegriffe = new FachbegriffItem[1];
        
        // Reload from database (simulating ServerInitializer behavior)
        FachbegriffItem[] loaded = at.tgm.objects.FachbegriffItem.loadAll();
        assertNotNull(loaded, "Geladene Fachbegriffe sollten nicht null sein");
        
        // Find our test item
        boolean found = false;
        for (FachbegriffItem item : loaded) {
            if (item != null && item.getId() == testId) {
                found = true;
                assertEquals(testItem.getWord(), item.getWord(), "Word sollte übereinstimmen");
                assertEquals(testItem.getPhrase(), item.getPhrase(), "Phrase sollte übereinstimmen");
                logger.info("✓ Test-Fachbegriff nach Neustart gefunden und verifiziert");
                break;
            }
        }
        
        assertTrue(found, "Test-Fachbegriff sollte nach Neustart gefunden werden");
        
        // Clean up - delete directly from database since it's not in Server array anymore
        logger.info("Lösche Test-Fachbegriff aus Datenbank...");
        if (DatabaseManager.getInstance().isInitialized()) {
            try {
                Connection conn = DatabaseManager.getConnection();
                try {
                    String sql = "DELETE FROM fachbegriff_item WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setLong(1, testId);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    logger.warn("Fehler beim Löschen: {}", e.getMessage());
                } finally {
                    DatabaseManager.returnConnection(conn);
                }
            } catch (Exception e) {
                logger.warn("Fehler beim Zugriff auf Datenbank: {}", e.getMessage());
            }
        }
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }

    // Helper methods

    private boolean isFachbegriffInDatabase(long id) {
        if (!DatabaseManager.getInstance().isInitialized()) {
            return false;
        }
        
        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                String sql = "SELECT 1 FROM fachbegriff_item WHERE id = ? LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Prüfen von Fachbegriff in DB: {}", e.getMessage());
            return false;
        }
    }

    private FachbegriffItem loadFachbegriffFromDatabase(long id) {
        if (!DatabaseManager.getInstance().isInitialized()) {
            return null;
        }
        
        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                String sql = "SELECT id, word, level, points, max_points, phrase FROM fachbegriff_item WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return new FachbegriffItem(
                                rs.getLong("id"),
                                rs.getString("word"),
                                rs.getInt("level"),
                                rs.getInt("points"),
                                rs.getInt("max_points"),
                                rs.getString("phrase")
                            );
                        }
                    }
                }
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Laden von Fachbegriff aus DB: {}", e.getMessage());
        }
        return null;
    }

    private boolean isQuizTemplateInDatabase(long id) {
        if (!DatabaseManager.getInstance().isInitialized()) {
            return false;
        }
        
        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                String sql = "SELECT 1 FROM quiz_template WHERE id = ? LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Prüfen von Quiz-Template in DB: {}", e.getMessage());
            return false;
        }
    }

    private boolean hasQuizTemplateItems(long templateId, int expectedCount) {
        if (!DatabaseManager.getInstance().isInitialized()) {
            return false;
        }
        
        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                String sql = "SELECT COUNT(*) as count FROM quiz_template_items WHERE quiz_template_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, templateId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("count") == expectedCount;
                        }
                    }
                }
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Prüfen von Quiz-Template-Items in DB: {}", e.getMessage());
        }
        return false;
    }
}
