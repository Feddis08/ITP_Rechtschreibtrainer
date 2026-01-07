package at.tgm.server;

import at.tgm.network.packets.S2CPOSTQuiz;
import at.tgm.network.packets.S2CPOSTStats;
import at.tgm.network.packets.S2CResultOfQuiz;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;

import java.io.IOException;

/**
 * State für authentifizierte Schüler-Clients.
 * Implementiert Schüler-spezifische Funktionalität.
 */
public class SchuelerState implements ClientState {

    @Override
    public void postAllSchueler(ServerClient client) throws IOException {
        throw new UnsupportedOperationException("Schüler können keine Schülerliste abrufen");
    }

    /**
     * Fügt ein Quiz dauerhaft zum Schüler hinzu.
     */
    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        if (q == null) return;

        Schueler s = (Schueler) client.getNutzer();
        Quiz[] quizzes = s.getQuizzes();

        if (quizzes == null) {
            // Erstes Quiz überhaupt
            s.setQuizzes(new Quiz[]{q});
            return;
        }

        Quiz[] newArr = new Quiz[quizzes.length + 1];
        System.arraycopy(quizzes, 0, newArr, 0, quizzes.length);
        newArr[quizzes.length] = q;

        s.setQuizzes(newArr);
    }

    /**
     * Startet ein neues Quiz.
     */
    @Override
    public void startQuiz(ServerClient client) throws IOException {
        System.out.println("Quiz started for: " + client.getNutzer().getUsername());

        Quiz quiz = new Quiz(10, System.currentTimeMillis());
        ((Schueler) client.getNutzer()).setQuiz(quiz);

        client.send(new S2CPOSTQuiz(quiz.getCensoredItems()));
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
            System.err.println("WARN: finishQuiz() ohne aktives Quiz!");
            return;
        }

        FachbegriffItem[] correctItems = quiz.getItems();
        if (correctItems == null) {
            System.err.println("WARN: Quiz hat keine Items!");
            return;
        }

        // Validierung: Arrays müssen die gleiche Länge haben
        if (fgs.length != correctItems.length) {
            System.err.println("WARN: Ungleiche Array-Längen! Erwartet: " + correctItems.length + ", Erhalten: " + fgs.length);
            return;
        }

        int totalPoints = 0;
        int maxPoints = 0;

        for (int i = 0; i < fgs.length; i++) {
            FachbegriffItem rightOne = correctItems[i];
            FachbegriffItem userOne = fgs[i];
            
            if (rightOne == null || userOne == null) {
                System.err.println("WARN: Null-Item an Index " + i);
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

            // Dem Client das richtige Wort + erreichte Punkte zurückgeben
            userOne.setPoints(earned);
            userOne.setWord(rightOne.getWord());
        }

        quiz.setUserItems(fgs);
        quiz.setPoints(totalPoints);
        quiz.setMaxPoints(maxPoints);
        quiz.setTimeEnded(System.currentTimeMillis());

        addQuiz(client, quiz);        // Quiz speichern
        s.setQuiz(null);              // Quiz als "abgeschlossen" entfernen

        try {
            client.send(new S2CResultOfQuiz(fgs, totalPoints, maxPoints));
        } catch (IOException e) {
            System.err.println("Fehler beim Senden des Quiz-Ergebnisses: " + e.getMessage());
            e.printStackTrace();
            // IOException wird nicht weitergeworfen, da die Methode keine IOException deklariert
        }
    }

    /**
     * Sendet alle bisherigen Quizzes des Schülers.
     */
    @Override
    public void postStats(ServerClient client) {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        Schueler s = (Schueler) client.getNutzer();
        Quiz[] quizzes = s.getQuizzes();

        if (quizzes == null)
            quizzes = new Quiz[0];

        try {
            client.send(new S2CPOSTStats(quizzes));
        } catch (IOException e) {
            System.err.println("Fehler beim Senden der Statistiken: " + e.getMessage());
            e.printStackTrace();
            // IOException wird nicht weitergeworfen, da die Methode keine IOException deklariert
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
