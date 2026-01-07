package at.tgm.server;

import at.tgm.network.packets.S2CPOSTAllSchueler;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * State für authentifizierte Lehrer-Clients.
 * Implementiert Lehrer-spezifische Funktionalität.
 */
public class LehrerState implements ClientState {

    @Override
    public void postAllSchueler(ServerClient client) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        // Sammle alle Schüler in einem Array (effizienter als doppelte Iteration)
        List<Schueler> schuelerList = new ArrayList<>();
        if (Server.nutzers != null) {
            for (Nutzer nutzer : Server.nutzers) {
                if (nutzer instanceof Schueler) {
                    schuelerList.add((Schueler) nutzer);
                }
            }
        }

        Schueler[] schuelerArray = schuelerList.toArray(new Schueler[0]);
        S2CPOSTAllSchueler packet = new S2CPOSTAllSchueler(schuelerArray);
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
