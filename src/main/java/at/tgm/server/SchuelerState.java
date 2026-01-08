package at.tgm.server;

import at.tgm.network.packets.S2CPOSTQuiz;
import at.tgm.network.packets.S2CPOSTStats;
import at.tgm.network.packets.S2CResultOfQuiz;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * State für authentifizierte Schüler-Clients.
 * Implementiert Schüler-spezifische Funktionalität.
 */
public class SchuelerState implements ClientState {

    private static final Logger logger = LoggerFactory.getLogger(SchuelerState.class);

    @Override
    public void postAllSchueler(ServerClient client, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Schülerliste abzurufen (nicht erlaubt, Request-ID: {})", 
                    client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Schülerliste abrufen");
    }

    /**
     * Fügt ein Quiz dauerhaft zum Schüler hinzu.
     */
    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        if (q == null) {
            logger.warn("Versuch, null-Quiz hinzuzufügen");
            return;
        }

        Schueler s = (Schueler) client.getNutzer();
        Quiz[] quizzes = s.getQuizzes();

        if (quizzes == null) {
            // Erstes Quiz überhaupt
            s.setQuizzes(new Quiz[]{q});
            logger.info("Erstes Quiz für Schüler '{}' hinzugefügt", s.getUsername());
            return;
        }

        Quiz[] newArr = new Quiz[quizzes.length + 1];
        System.arraycopy(quizzes, 0, newArr, 0, quizzes.length);
        newArr[quizzes.length] = q;

        s.setQuizzes(newArr);
        logger.info("Quiz für Schüler '{}' hinzugefügt (Gesamt: {})", s.getUsername(), newArr.length);
    }

    /**
     * Startet ein neues Quiz.
     */
    @Override
    public void startQuiz(ServerClient client) throws IOException {
        String username = client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown";
        logger.info("Starte Quiz für Schüler: {}", username);

        Quiz quiz = new Quiz(10, System.currentTimeMillis());
        ((Schueler) client.getNutzer()).setQuiz(quiz);
        logger.debug("Quiz erstellt mit {} Items", quiz.getCensoredItems() != null ? quiz.getCensoredItems().length : 0);

        client.send(new S2CPOSTQuiz(quiz.getCensoredItems()));
        logger.info("Quiz-Paket an Schüler '{}' gesendet", username);
    }

    /**
     * Bewertet das Quiz, vergibt Punkte und speichert es.
     */
    @Override
    public void finishQuiz(ServerClient client, FachbegriffItem[] fgs) {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (fgs == null) {
            throw new IllegalArgumentException("FachbegriffItem-Array darf nicht null sein");
        }

        Schueler s = (Schueler) client.getNutzer();
        Quiz quiz = s.getQuiz();

        if (quiz == null) {
            logger.warn("finishQuiz() ohne aktives Quiz für Schüler '{}'", s.getUsername());
            return;
        }

        FachbegriffItem[] correctItems = quiz.getItems();
        if (correctItems == null) {
            logger.warn("Quiz hat keine Items für Schüler '{}'", s.getUsername());
            return;
        }

        // Validierung: Arrays müssen die gleiche Länge haben
        if (fgs.length != correctItems.length) {
            logger.warn("Ungleiche Array-Längen für Schüler '{}': Erwartet {}, Erhalten {}", 
                       s.getUsername(), correctItems.length, fgs.length);
            return;
        }

        logger.info("Bewerte Quiz für Schüler '{}' ({} Items)", s.getUsername(), fgs.length);

        int totalPoints = 0;
        int maxPoints = 0;

        for (int i = 0; i < fgs.length; i++) {
            FachbegriffItem rightOne = correctItems[i];
            FachbegriffItem userOne = fgs[i];
            
            if (rightOne == null || userOne == null) {
                logger.warn("Null-Item an Index {} für Schüler '{}'", i, s.getUsername());
                continue;
            }

            String correct = safe(rightOne.getWord());
            String user = safe(userOne.getWord());

            int full = rightOne.getPoints();
            maxPoints += full;

            int earned = 0;

            if (user.isEmpty()) {
                earned = 0;
            } else if (user.equals(correct)) {
                earned = full;
            } else if (user.equalsIgnoreCase(correct)) {
                earned = Math.max(1, full / 2);
            } else {
                earned = 0;
            }

            totalPoints += earned;

            // Dem Client das richtige Wort + erreichte Punkte + maxPoints zurückgeben
            userOne.setPoints(earned);
            userOne.setMaxPoints(full);
            userOne.setWord(rightOne.getWord());
        }

        quiz.setUserItems(fgs);
        quiz.setPoints(totalPoints);
        quiz.setMaxPoints(maxPoints);
        quiz.setTimeEnded(System.currentTimeMillis());

        logger.info("Quiz bewertet für Schüler '{}': {}/{} Punkte", s.getUsername(), totalPoints, maxPoints);

        addQuiz(client, quiz);        // Quiz speichern
        s.setQuiz(null);              // Quiz als "abgeschlossen" entfernen

        try {
            client.send(new S2CResultOfQuiz(fgs, totalPoints, maxPoints));
            logger.debug("Quiz-Ergebnis an Schüler '{}' gesendet", s.getUsername());
        } catch (IOException e) {
            logger.error("Fehler beim Senden des Quiz-Ergebnisses an Schüler '{}'", s.getUsername(), e);
            // IOException wird nicht weitergeworfen, da die Methode keine IOException deklariert
        }
    }

    /**
     * Sendet alle bisherigen Quizzes des Schülers.
     */
    @Override
    public void postStats(ServerClient client, long requestId) {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        Schueler s = (Schueler) client.getNutzer();
        Quiz[] quizzes = s.getQuizzes();

        if (quizzes == null)
            quizzes = new Quiz[0];

        logger.info("Sende Statistiken an Schüler '{}' ({} Quizzes, Request-ID: {})", s.getUsername(), quizzes.length, requestId);

        try {
            S2CPOSTStats response = new S2CPOSTStats(quizzes);
            response.setRequestId(requestId); // WICHTIG: Request-ID übernehmen
            client.send(response);
            logger.debug("Statistiken erfolgreich an Schüler '{}' gesendet", s.getUsername());
        } catch (IOException e) {
            logger.error("Fehler beim Senden der Statistiken an Schüler '{}'", s.getUsername(), e);
            // IOException wird nicht weitergeworfen, da die Methode keine IOException deklariert
        }
    }

    @Override
    public void addSchueler(ServerClient client, Schueler schueler, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Schueler hinzuzufügen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Schueler hinzufügen");
    }

    @Override
    public void postSchuelerStats(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, SchuelerStats abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine SchuelerStats abrufen");
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
