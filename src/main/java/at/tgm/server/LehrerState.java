package at.tgm.server;

import at.tgm.network.packets.S2CPOSTAllSchueler;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;

import java.io.IOException;

/**
 * State für authentifizierte Lehrer-Clients.
 * Implementiert Lehrer-spezifische Funktionalität.
 */
public class LehrerState implements ClientState {

    @Override
    public void postAllSchueler(ServerClient client) throws IOException {
        // Zuerst zählen, wie viele Schüler es gibt
        int schuelerCount = 0;
        for (Nutzer nutzer : Server.nutzers) {
            if (nutzer instanceof Schueler) {
                schuelerCount++;
            }
        }

        // Array mit der richtigen Größe erstellen
        Schueler[] s = new Schueler[schuelerCount];
        int i = 0;
        for (Nutzer nutzer : Server.nutzers) {
            if (nutzer instanceof Schueler) {
                s[i] = (Schueler) nutzer;
                i++;
            }
        }

        S2CPOSTAllSchueler packet = new S2CPOSTAllSchueler(s);
        client.send(packet);
    }

    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        throw new UnsupportedOperationException("Lehrer können keine Quizzes hinzufügen");
    }

    @Override
    public void startQuiz(ServerClient client) throws IOException {
        throw new UnsupportedOperationException("Lehrer können keine Quizzes starten");
    }

    @Override
    public void finishQuiz(ServerClient client, FachbegriffItem[] fgs) {
        throw new UnsupportedOperationException("Lehrer können keine Quizzes beenden");
    }

    @Override
    public void postStats(ServerClient client) {
        throw new UnsupportedOperationException("Lehrer können keine Statistiken abrufen");
    }
}
