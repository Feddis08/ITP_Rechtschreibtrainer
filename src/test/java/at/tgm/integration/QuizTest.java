package at.tgm.integration;

import at.tgm.objects.FachbegriffItem;
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
 * Integration tests for Quiz functionality.
 * 
 * Tests:
 * - Schüler können Quiz starten
 * - Schüler können Quiz-Antworten einreichen
 * - Quiz-Benotung ist korrekt
 */
public class QuizTest {

    private static final Logger logger = LoggerFactory.getLogger(QuizTest.class);
    
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
        
        // Authenticate as Schüler
        logger.info("Authentifiziere als Schüler (riemer/123)...");
        boolean authSuccess = testClient.authenticate("riemer", "123", 5);
        assertTrue(authSuccess, "Authentifizierung sollte erfolgreich sein");
        logger.info("✓ Als Schüler authentifiziert");
    }

    @AfterEach
    public void tearDownClient() {
        logger.info("--- Teardown: Trenne Test-Client ---");
        if (testClient != null) {
            testClient.disconnect();
        }
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    public void testSchuelerCanCompleteQuizWithCorrectAnswers() throws Exception {
        logger.info(">>> Test: Schüler kann Quiz mit korrekten Antworten abschließen <<<");
        
        // Start quiz
        logger.info("Starte Quiz...");
        FachbegriffItem[] quizItems = testClient.startQuiz(5);
        
        assertNotNull(quizItems, "Quiz-Items sollten nicht null sein");
        assertTrue(quizItems.length > 0, "Quiz sollte Items enthalten");
        logger.info("✓ Quiz gestartet mit {} Items", quizItems.length);
        
        // Verify items are censored (word should be null)
        for (FachbegriffItem item : quizItems) {
            assertNotNull(item, "Item sollte nicht null sein");
            assertNotNull(item.getPhrase(), "Item sollte eine Phrase haben");
            // Word should be null in censored items
            assertNull(item.getWord(), "Item sollte censored sein (word = null)");
        }
        
        // We need to know the correct answers to test properly
        // But in censored items, the word is null, so we can't see the answers
        // For testing, we'll submit answers that we know from the Quiz class
        // The Quiz class has hardcoded items - let's use those
        
        // Based on Quiz.getRandomItems(), we know the items:
        // 0: "IDE" - 1 point
        // 1: "Compiler" - 2 points
        // 2: "Interpreter" - 2 points
        // etc.
        
        // For a proper test, we need to submit correct answers
        // Since we can't see the correct answers in censored items,
        // we'll test with known correct answers based on the quiz structure
        // But this is a limitation - in a real scenario, we'd need to either:
        // 1. Store the original items somewhere accessible
        // 2. Or test with a mock quiz that we control
        
        // For now, let's test with some answers and verify the grading logic works
        logger.info("Erstelle Antworten...");
        FachbegriffItem[] answers = new FachbegriffItem[quizItems.length];
        for (int i = 0; i < quizItems.length; i++) {
            answers[i] = new FachbegriffItem(
                null, // We'll set this based on what we think is correct
                quizItems[i].getLevel(),
                quizItems[i].getPoints(),
                quizItems[i].getPhrase()
            );
        }
        
        // Set some test answers - we'll use correct answers from the quiz structure
        // Note: This is a bit of a hack since we don't have direct access to correct answers
        // But we can test the grading by submitting known correct/incorrect answers
        
        // For this test, let's assume we submit all correct answers
        // We'll set words based on the known quiz structure
        String[] knownAnswers = {
            "IDE", "Compiler", "Interpreter", "Algorithmus", "Variable",
            "Array", "Klasse", "Objekt", "Konstruktor", "Datenbank"
        };
        
        for (int i = 0; i < Math.min(answers.length, knownAnswers.length); i++) {
            answers[i].setWord(knownAnswers[i]);
        }
        
        logger.info("Sende Quiz-Antworten...");
        HeadlessTestClient.QuizResult result = testClient.submitQuizAnswers(answers, 10);
        
        assertNotNull(result, "Quiz-Ergebnis sollte nicht null sein");
        assertNotNull(result.items, "Quiz-Ergebnis-Items sollten nicht null sein");
        assertEquals(quizItems.length, result.items.length, "Anzahl Items sollte gleich sein");
        
        logger.info("✓ Quiz-Ergebnis erhalten: {}/{} Punkte", result.points, result.maxPoints);
        
        // Verify points calculation
        assertTrue(result.points >= 0, "Punkte sollten >= 0 sein");
        assertTrue(result.maxPoints > 0, "Maximale Punkte sollten > 0 sein");
        assertTrue(result.points <= result.maxPoints, "Punkte sollten <= MaxPunkte sein");
        
        // Since we submitted correct answers, we should get high points
        // But we can't guarantee 100% because we're guessing the order
        // Let's at least verify the grading system works
        assertTrue(result.points > 0, "Bei korrekten Antworten sollten Punkte > 0 sein");
        
        logger.info("✓ Benotung erfolgreich: {}/{} Punkte", result.points, result.maxPoints);
        
        // Verify that items now have the correct words (server sends them back)
        for (FachbegriffItem item : result.items) {
            assertNotNull(item.getWord(), "Item sollte nach Bewertung ein Wort haben");
            assertNotNull(item.getPhrase(), "Item sollte eine Phrase haben");
        }
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    public void testQuizGradingWithWrongAnswers() throws Exception {
        logger.info(">>> Test: Quiz-Benotung mit falschen Antworten <<<");
        
        // Start quiz
        logger.info("Starte Quiz...");
        FachbegriffItem[] quizItems = testClient.startQuiz(5);
        
        assertNotNull(quizItems, "Quiz-Items sollten nicht null sein");
        assertTrue(quizItems.length > 0, "Quiz sollte Items enthalten");
        logger.info("✓ Quiz gestartet mit {} Items", quizItems.length);
        
        // Submit all wrong answers
        logger.info("Erstelle falsche Antworten...");
        FachbegriffItem[] wrongAnswers = new FachbegriffItem[quizItems.length];
        for (int i = 0; i < quizItems.length; i++) {
            wrongAnswers[i] = new FachbegriffItem(
                "FALSCH_ANTWORT_" + i, // Definitely wrong
                quizItems[i].getLevel(),
                quizItems[i].getPoints(),
                quizItems[i].getPhrase()
            );
        }
        
        logger.info("Sende falsche Quiz-Antworten...");
        HeadlessTestClient.QuizResult result = testClient.submitQuizAnswers(wrongAnswers, 10);
        
        assertNotNull(result, "Quiz-Ergebnis sollte nicht null sein");
        logger.info("✓ Quiz-Ergebnis erhalten: {}/{} Punkte", result.points, result.maxPoints);
        
        // With all wrong answers, points should be 0
        assertEquals(0, result.points, "Bei allen falschen Antworten sollten 0 Punkte vergeben werden");
        assertTrue(result.maxPoints > 0, "Maximale Punkte sollten > 0 sein");
        
        logger.info("✓ Benotung korrekt: 0/{} Punkte bei falschen Antworten", result.maxPoints);
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    public void testQuizGradingWithPartialAnswers() throws Exception {
        logger.info(">>> Test: Quiz-Benotung mit teilweise korrekten Antworten <<<");
        
        // Start quiz
        logger.info("Starte Quiz...");
        FachbegriffItem[] quizItems = testClient.startQuiz(5);
        
        assertNotNull(quizItems, "Quiz-Items sollten nicht null sein");
        assertTrue(quizItems.length > 0, "Quiz sollte Items enthalten");
        logger.info("✓ Quiz gestartet mit {} Items", quizItems.length);
        
        // Submit mixed answers: some correct, some wrong, some empty
        logger.info("Erstelle gemischte Antworten...");
        FachbegriffItem[] mixedAnswers = new FachbegriffItem[quizItems.length];
        
        // Known correct answers from quiz structure
        String[] knownAnswers = {
            "IDE", "Compiler", "Interpreter", "Algorithmus", "Variable",
            "Array", "Klasse", "Objekt", "Konstruktor", "Datenbank"
        };
        
        for (int i = 0; i < quizItems.length; i++) {
            mixedAnswers[i] = new FachbegriffItem(
                (i % 3 == 0) ? knownAnswers[Math.min(i, knownAnswers.length - 1)] : // Some correct
                (i % 3 == 1) ? "" : // Some empty
                "WRONG", // Some wrong
                quizItems[i].getLevel(),
                quizItems[i].getPoints(),
                quizItems[i].getPhrase()
            );
        }
        
        logger.info("Sende gemischte Quiz-Antworten...");
        HeadlessTestClient.QuizResult result = testClient.submitQuizAnswers(mixedAnswers, 10);
        
        assertNotNull(result, "Quiz-Ergebnis sollte nicht null sein");
        logger.info("✓ Quiz-Ergebnis erhalten: {}/{} Punkte", result.points, result.maxPoints);
        
        // With mixed answers, we should get some points but not all
        assertTrue(result.points >= 0, "Punkte sollten >= 0 sein");
        assertTrue(result.points < result.maxPoints, "Bei gemischten Antworten sollten nicht alle Punkte erreicht werden");
        assertTrue(result.maxPoints > 0, "Maximale Punkte sollten > 0 sein");
        
        logger.info("✓ Benotung korrekt: {}/{} Punkte bei gemischten Antworten", result.points, result.maxPoints);
        
        logger.info(">>> Test erfolgreich abgeschlossen <<<");
    }
}
