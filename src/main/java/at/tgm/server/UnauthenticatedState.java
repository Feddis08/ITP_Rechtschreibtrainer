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
    public void startQuiz(ServerClient client, long templateId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Quiz zu starten: {}", 
                   client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void getQuizTemplatesForSchueler(ServerClient client, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Quiz-Templates abzurufen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
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

    @Override
    public void setSchuelerNote(ServerClient client, String schuelerUsername, at.tgm.objects.Note note, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Note zu setzen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void getOwnAccount(ServerClient client, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Account-Daten abzurufen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    // ======================================================
    // FachbegriffItem-Verwaltung (nur für Lehrer)
    // ======================================================

    @Override
    public void getAllFachbegriffe(ServerClient client, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Fachbegriffe abzurufen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void createFachbegriff(ServerClient client, FachbegriffItem item, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Fachbegriff zu erstellen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void updateFachbegriff(ServerClient client, long id, FachbegriffItem item, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Fachbegriff zu aktualisieren (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void deleteFachbegriff(ServerClient client, long id, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Fachbegriff zu löschen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    // ======================================================
    // Quiz-Template-Verwaltung (nur für Lehrer)
    // ======================================================

    @Override
    public void getAllQuizTemplates(ServerClient client, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Quiz-Templates abzurufen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void createQuizTemplate(ServerClient client, Quiz quiz, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, QuizTemplate zu erstellen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void updateQuizTemplate(ServerClient client, long id, Quiz quiz, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, QuizTemplate zu aktualisieren (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void deleteQuizTemplate(ServerClient client, long id, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, QuizTemplate zu löschen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    // ======================================================
    // Lehrer-Verwaltung (nur für SysAdmin)
    // ======================================================

    @Override
    public void postAllLehrer(ServerClient client, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Lehrerliste abzurufen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void addLehrer(ServerClient client, at.tgm.objects.Lehrer lehrer, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Lehrer hinzuzufügen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void toggleLehrerStatus(ServerClient client, String lehrerUsername, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Lehrer-Status zu ändern (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }

    @Override
    public void deleteLehrer(ServerClient client, String lehrerUsername, long requestId) throws IOException {
        logger.warn("Nicht authentifizierter Client versuchte, Lehrer zu löschen (Request-ID: {}): {}", 
                   requestId, client.getSocket().getRemoteSocketAddress());
        throw new UnsupportedOperationException("Client ist nicht authentifiziert");
    }
}
