package at.tgm.server;

import at.tgm.network.packets.S2CPOSTAllSchueler;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * State für authentifizierte Lehrer-Clients.
 * Implementiert Lehrer-spezifische Funktionalität.
 */
public class LehrerState implements ClientState {

    private static final Logger logger = LoggerFactory.getLogger(LehrerState.class);

    @Override
    public void postAllSchueler(ServerClient client, long requestId) throws IOException {
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
        logger.info("Sende {} Schüler an Lehrer '{}' (Request-ID: {})", 
                   schuelerArray.length, 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown",
                   requestId);
        S2CPOSTAllSchueler packet = new S2CPOSTAllSchueler(schuelerArray);
        packet.setRequestId(requestId); // WICHTIG: Request-ID übernehmen
        client.send(packet);
        logger.debug("Schülerliste erfolgreich gesendet");
    }

    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        logger.warn("Lehrer '{}' versuchte, Quiz hinzuzufügen (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("Lehrer können keine Quizzes hinzufügen");
    }

    @Override
    public void startQuiz(ServerClient client) throws IOException {
        logger.warn("Lehrer '{}' versuchte, Quiz zu starten (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("Lehrer können keine Quizzes starten");
    }

    @Override
    public void finishQuiz(ServerClient client, FachbegriffItem[] fgs) {
        logger.warn("Lehrer '{}' versuchte, Quiz zu beenden (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("Lehrer können keine Quizzes beenden");
    }

    @Override
    public void postStats(ServerClient client, long requestId) {
        logger.warn("Lehrer '{}' versuchte, Statistiken abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Lehrer können keine Statistiken abrufen");
    }
}
