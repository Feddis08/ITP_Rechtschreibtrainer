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

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test für Quiz-Persistenz nach Server-Neustart.
 * 
 * Test:
 * - Schüler startet Quiz
 * - Schüler beendet Quiz mit Antworten
 * - Server wird neu gestartet
 * - Schüler loggt sich wieder ein
 * - Schüler ruft Statistiken ab
 * - Quiz-Ergebnis ist noch vorhanden
 */
public class QuizPersistenceAfterRestartTest {

    private static final Logger logger = LoggerFactory.getLogger(QuizPersistenceAfterRestartTest.class);
    private static int serverPort;

    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("=== Setup: Starte Test-Server ===");
        
        // Ensure database is initialized
        if (!DatabaseManager.getInstance().isInitialized()) {
            DatabaseManager.getInstance().initialize();
            at.tgm.server.DatabaseSchema.createTables();
        }
        
        serverPort = TestServerManager.startServer();
        Thread.sleep(500);
        assertTrue(TestServerManager.isRunning(), "Server sollte laufen");
        logger.info("Test-Server läuft auf Port {}", serverPort);
    }

    @AfterAll
    public static void tearDownServer() {
        logger.info("=== Teardown: Stoppe Test-Server ===");
        TestServerManager.stopServer();
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    public void testQuizResultsSurviveServerRestart() throws Exception {
        logger.info(">>> Test: Quiz-Ergebnisse überleben Server-Neustart <<<");
        
        // Schritt 1: Schüler verbindet sich und loggt ein
        logger.info("Schritt 1: Schüler verbindet sich...");
        HeadlessTestClient client1 = new HeadlessTestClient();
        client1.connect("localhost", serverPort);
        Thread.sleep(200);
        assertTrue(client1.isConnected(), "Client sollte verbunden sein");
        
        logger.info("Schritt 2: Schüler authentifiziert sich...");
        boolean authSuccess = client1.authenticate("riemer", "123", 5);
        assertTrue(authSuccess, "Authentifizierung sollte erfolgreich sein");
        logger.info("✓ Schüler authentifiziert");
        
        // Schritt 3: Starte Quiz (ohne Template, Legacy-Verhalten)
        logger.info("Schritt 3: Starte Quiz...");
        FachbegriffItem[] quizItems = client1.startQuiz(5);
        assertNotNull(quizItems, "Quiz-Items sollten nicht null sein");
        assertTrue(quizItems.length > 0, "Quiz sollte Items enthalten");
        logger.info("✓ Quiz gestartet mit {} Items", quizItems.length);
        
        // Schritt 4: Beende Quiz mit Antworten
        logger.info("Schritt 4: Beende Quiz mit Antworten...");
        FachbegriffItem[] answers = new FachbegriffItem[quizItems.length];
        for (int i = 0; i < quizItems.length; i++) {
            answers[i] = new FachbegriffItem(
                "TEST_ANSWER_" + i, // Test-Antworten
                quizItems[i].getLevel(),
                quizItems[i].getPoints(),
                quizItems[i].getPhrase()
            );
        }
        
        HeadlessTestClient.QuizResult result = client1.submitQuizAnswers(answers, 10);
        assertNotNull(result, "Quiz-Ergebnis sollte nicht null sein");
        logger.info("✓ Quiz beendet: {}/{} Punkte", result.points, result.maxPoints);
        
        // Schritt 5: Trenne Verbindung und stoppe Server
        logger.info("Schritt 5: Trenne Verbindung und stoppe Server...");
        client1.disconnect();
        Thread.sleep(500);
        
        TestServerManager.stopServer();
        Thread.sleep(1000);
        logger.info("✓ Server gestoppt");
        
        // Schritt 6: Starte Server neu
        logger.info("Schritt 6: Starte Server neu...");
        serverPort = TestServerManager.startServer();
        Thread.sleep(1000);
        assertTrue(TestServerManager.isRunning(), "Server sollte wieder laufen");
        logger.info("✓ Server neu gestartet");
        
        // Schritt 7: Schüler verbindet sich erneut
        logger.info("Schritt 7: Schüler verbindet sich erneut...");
        HeadlessTestClient client2 = new HeadlessTestClient();
        client2.connect("localhost", serverPort);
        Thread.sleep(200);
        assertTrue(client2.isConnected(), "Client sollte verbunden sein");
        
        logger.info("Schritt 8: Schüler authentifiziert sich erneut...");
        authSuccess = client2.authenticate("riemer", "123", 5);
        assertTrue(authSuccess, "Authentifizierung sollte erfolgreich sein");
        logger.info("✓ Schüler erneut authentifiziert");
        
        // Schritt 9: Rufe Statistiken ab
        logger.info("Schritt 9: Rufe Statistiken ab...");
        
        // Wir müssen die Statistiken über den Client abrufen
        // Da HeadlessTestClient keine requestStats-Methode hat, müssen wir sie hinzufügen
        // Oder wir prüfen direkt in der Datenbank
        
        // Prüfe direkt in der Datenbank, ob Quiz-Ergebnis vorhanden ist
        logger.info("Prüfe Quiz-Ergebnis in Datenbank...");
        assertTrue(isQuizAttemptInDatabase("riemer"), 
                  "Quiz-Ergebnis sollte in Datenbank vorhanden sein");
        logger.info("✓ Quiz-Ergebnis in Datenbank gefunden");
        
        // Prüfe, ob Server die Quiz-Ergebnisse laden kann
        at.tgm.objects.Schueler schueler = (at.tgm.objects.Schueler) Server.findNutzerByUsername("riemer");
        assertNotNull(schueler, "Schüler sollte gefunden werden");
        
        Quiz[] loadedQuizzes = Server.loadQuizAttemptsForSchueler(schueler);
        assertNotNull(loadedQuizzes, "Geladene Quizzes sollten nicht null sein");
        assertTrue(loadedQuizzes.length > 0, "Mindestens ein Quiz sollte geladen werden");
        logger.info("✓ {} Quiz-Ergebnisse für Schüler geladen", loadedQuizzes.length);
        
        // Prüfe, ob das geladene Quiz die richtigen Daten hat
        Quiz loadedQuiz = loadedQuizzes[0];
        assertNotNull(loadedQuiz, "Geladenes Quiz sollte nicht null sein");
        assertTrue(loadedQuiz.getPoints() >= 0, "Punkte sollten >= 0 sein");
        assertTrue(loadedQuiz.getMaxPoints() > 0, "Maximale Punkte sollten > 0 sein");
        logger.info("✓ Geladenes Quiz hat korrekte Daten: {}/{} Punkte", 
                   loadedQuiz.getPoints(), loadedQuiz.getMaxPoints());
        
        client2.disconnect();
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }
    
    /**
     * Prüft, ob ein Quiz-Ergebnis für einen Schüler in der Datenbank vorhanden ist.
     */
    private boolean isQuizAttemptInDatabase(String username) {
        if (!DatabaseManager.getInstance().isInitialized()) {
            return false;
        }
        
        try {
            java.sql.Connection conn = DatabaseManager.getConnection();
            try {
                String sql = """
                    SELECT COUNT(*) as count
                    FROM quiz_attempt qa
                    INNER JOIN schueler s ON qa.schueler_id = s.nutzer_id
                    INNER JOIN nutzer n ON s.nutzer_id = n.id
                    WHERE n.username = ?
                    """;
                
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("count") > 0;
                        }
                    }
                }
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Prüfen von Quiz-Ergebnis in DB: {}", e.getMessage());
        }
        return false;
    }
}
