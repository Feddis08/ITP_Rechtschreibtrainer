package at.tgm.server;

import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * State für nicht authentifizierte Clients.
 * Alle Methoden werfen eine UnsupportedOperationException.
 */
public class UnauthenticatedState implements ClientState {

    private static final Logger logger = LoggerFactory.getLogger(UnauthenticatedState.class);

    @Override
    public void postAllSchueler(ServerClient client, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Schülerliste abzurufen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        logger.warn("Nicht authentifizierter Client versuchte, Quiz hinzuzufügen: {}", 
                   client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void startQuiz(ServerClient client) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Quiz zu starten: {}", 
                   client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void finishQuiz(ServerClient client, FachbegriffItem[] fgs) {
        logger.warn("Nicht authentifizierter Client versuchte, Quiz zu beenden: {}", 
                   client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void postStats(ServerClient client, long requestId) {
        logger.warn("Nicht authentifizierter Client versuchte, Statistiken abzurufen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void addSchueler(ServerClient client, Schueler schueler, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Schueler hinzuzufügen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void postSchuelerStats(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, SchuelerStats abzurufen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void toggleSchuelerStatus(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Schueler-Status zu ändern (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void deleteSchueler(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Schueler zu löschen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }
}
