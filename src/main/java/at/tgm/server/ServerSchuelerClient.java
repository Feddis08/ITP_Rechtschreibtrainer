package at.tgm.server;

import at.tgm.network.core.SocketClient;
import at.tgm.network.packets.S2CPOSTQuiz;
import at.tgm.network.packets.S2CPOSTStats;
import at.tgm.network.packets.S2CResultOfQuiz;
import at.tgm.objects.Distro;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;

import java.io.IOException;
import java.net.Socket;

public class ServerSchuelerClient extends ServerClient {

    public ServerSchuelerClient(Socket socket) throws IOException {
        super(socket);
    }

    /**
     * Fügt ein Quiz dauerhaft zum Schüler hinzu.
     */
    public void addQuiz(Quiz q) {
        if (q == null) return;

        Schueler s = (Schueler) this.getNutzer();
        Quiz[] quizzes = s.getQuizzes();

        if (quizzes == null) {
            // Erstes Quiz überhaupt
            s.setQuizzes(new Quiz[]{ q });
            return;
        }

        Quiz[] newArr = new Quiz[quizzes.length + 1];
        System.arraycopy(quizzes, 0, newArr, 0, quizzes.length);
        newArr[quizzes.length] = q;

        s.setQuizzes(newArr);  // ← WICHTIG! Sonst wird NICHTS gespeichert
    }


    /**
     * Startet ein neues Quiz.
     */
    public void startQuiz() throws IOException {
        System.out.println("Quiz started for: " + this.getNutzer().getUsername());

        Quiz quiz = new Quiz(10, System.currentTimeMillis());
        ((Schueler) this.getNutzer()).setQuiz(quiz);

        this.send(new S2CPOSTQuiz(quiz.getCensoredItems()));
    }

    /**
     * Bewertet das Quiz, vergibt Punkte und speichert es.
     */
    public void finishQuiz(FachbegriffItem[] fgs) {

        Schueler s = (Schueler) this.getNutzer();
        Quiz quiz = s.getQuiz();

        if (quiz == null) {
            System.err.println("WARN: finishQuiz() ohne aktives Quiz!");
            return;
        }

        int totalPoints = 0;
        int maxPoints = 0;

        FachbegriffItem[] correctItems = quiz.getItems();

        for (int i = 0; i < fgs.length; i++) {

            FachbegriffItem rightOne = correctItems[i];
            FachbegriffItem userOne = fgs[i];

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

        addQuiz(quiz);        // ← jetzt speichert es auch wirklich

        s.setQuiz(null);      // Quiz als "abgeschlossen" entfernen

        try {
            this.send(new S2CResultOfQuiz(fgs, totalPoints, maxPoints));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Sendet alle bisherigen Quizzes des Schülers.
     */
    public void postStats() {
        Schueler s = (Schueler) this.getNutzer();
        Quiz[] quizzes = s.getQuizzes();

        if (quizzes == null)
            quizzes = new Quiz[0];

        try {
            this.send(new S2CPOSTStats(quizzes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
