package at.tgm.server;

import at.tgm.network.core.SocketClient;
import at.tgm.network.packets.S2CPOSTQuiz;
import at.tgm.network.packets.S2CResultOfQuiz;
import at.tgm.objects.Distro;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;

import java.io.IOException;
import java.net.Socket;

public class ServerSchuelerClient extends SocketClient {
    private Quiz quiz;

    public ServerSchuelerClient(Socket socket) throws IOException {
        super(socket, Distro.SERVER);
    }

    public void startQuiz() throws IOException {

        //if (quiz == null){
            System.out.println("Quiz started for: " + this.getNutzer().getUsername());

            quiz = new Quiz(10, System.currentTimeMillis());


        //}
        this.send(new S2CPOSTQuiz(quiz.getCensoredItems()));
    }

    public void finishQuiz(FachbegriffItem[] fgs) {

        int totalPoints = 0;
        int maxPoints = 0;

        for (int i = 0; i < fgs.length; i++) {
            FachbegriffItem rightOne = this.quiz.getItems()[i];
            FachbegriffItem userOne = fgs[i];

            String correct = safe(rightOne.getWord());
            String user = safe(userOne.getWord());

            int full = rightOne.getPoints();
            maxPoints += full;

            int earned = 0;

            if (user.isEmpty()) {
                earned = 0;
            } else if (user.equals(correct)) {
                // exakt, inkl. Groß-/Kleinschreibung
                earned = full;
            } else if (user.equalsIgnoreCase(correct)) {
                // Buchstaben stimmen, aber Case falsch -> Teilpunkte
                earned = Math.max(1, full / 2);  // z.B. halbe Punkte, mind. 1
            } else {
                // komplett falsch
                earned = 0;
            }

            totalPoints += earned;

            // zurück zum Client:
            // - Punkte = erreichte Punkte
            // - Wort = KORREKTES Wort (für Vergleich-Ansicht am Client)
            userOne.setPoints(earned);
            userOne.setWord(rightOne.getWord());
        }

        S2CResultOfQuiz packet = new S2CResultOfQuiz(fgs, totalPoints, maxPoints);
        try {
            this.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }



}
