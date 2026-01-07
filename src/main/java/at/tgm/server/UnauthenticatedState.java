package at.tgm.server;

import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;

import java.io.IOException;

/**
 * State für nicht authentifizierte Clients.
 * Alle Methoden werfen eine UnsupportedOperationException.
 */
public class UnauthenticatedState implements ClientState {

    @Override
    public void postAllSchueler(ServerClient client) throws IOException {
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void startQuiz(ServerClient client) throws IOException {
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void finishQuiz(ServerClient client, FachbegriffItem[] fgs) {
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void postStats(ServerClient client) {
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }
}
