package at.tgm.server;

import at.tgm.network.core.SocketClient;
import at.tgm.objects.Distro;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class ServerClient extends SocketClient {

    private static final Logger logger = LoggerFactory.getLogger(ServerClient.class);

    private ClientState state;

    public ServerClient(Socket socket) throws IOException {
        super(socket, Distro.SERVER);
        this.state = new UnauthenticatedState();
        logger.debug("Neuer ServerClient erstellt, initialer State: UnauthenticatedState");
    }

    public ClientState getState() {
        return state;
    }

    public void setState(ClientState state) {
        String oldState = this.state != null ? this.state.getClass().getSimpleName() : "null";
        String newState = state != null ? state.getClass().getSimpleName() : "null";
        logger.debug("State-Wechsel: {} -> {}", oldState, newState);
        this.state = state;
    }

    // Delegationsmethoden für rollenspezifische Funktionalität

    public void postAllSchueler(long requestId) throws IOException {
        logger.debug("postAllSchueler() aufgerufen mit Request-ID: {}", requestId);
        state.postAllSchueler(this, requestId);
    }

    public void addQuiz(Quiz q) {
        logger.debug("addQuiz() aufgerufen");
        state.addQuiz(this, q);
    }

    public void startQuiz(long templateId) throws IOException {
        logger.debug("startQuiz() aufgerufen mit Template-ID: {}", templateId);
        state.startQuiz(this, templateId);
    }

    public void finishQuiz(FachbegriffItem[] fgs) {
        logger.debug("finishQuiz() aufgerufen mit {} Items", fgs != null ? fgs.length : 0);
        state.finishQuiz(this, fgs);
    }

    public void postStats(long requestId) {
        logger.debug("postStats() aufgerufen mit Request-ID: {}", requestId);
        state.postStats(this, requestId);
    }

    public void addSchueler(at.tgm.objects.Schueler schueler, long requestId) throws IOException {
        logger.debug("addSchueler() aufgerufen mit Request-ID: {}", requestId);
        state.addSchueler(this, schueler, requestId);
    }

    public void postSchuelerStats(String schuelerUsername, long requestId) throws IOException {
        logger.debug("postSchuelerStats() aufgerufen für Schüler '{}' mit Request-ID: {}", schuelerUsername, requestId);
        state.postSchuelerStats(this, schuelerUsername, requestId);
    }

    public void toggleSchuelerStatus(String schuelerUsername, long requestId) throws IOException {
        logger.debug("toggleSchuelerStatus() aufgerufen für Schüler '{}' mit Request-ID: {}", schuelerUsername, requestId);
        state.toggleSchuelerStatus(this, schuelerUsername, requestId);
    }

    public void deleteSchueler(String schuelerUsername, long requestId) throws IOException {
        logger.debug("deleteSchueler() aufgerufen für Schüler '{}' mit Request-ID: {}", schuelerUsername, requestId);
        state.deleteSchueler(this, schuelerUsername, requestId);
    }

    public void setSchuelerNote(String schuelerUsername, at.tgm.objects.Note note, long requestId) throws IOException {
        logger.debug("setSchuelerNote() aufgerufen für Schüler '{}' mit Request-ID: {}", schuelerUsername, requestId);
        state.setSchuelerNote(this, schuelerUsername, note, requestId);
    }

    public void getOwnAccount(long requestId) throws IOException {
        logger.debug("getOwnAccount() aufgerufen mit Request-ID: {}", requestId);
        state.getOwnAccount(this, requestId);
    }

    // ======================================================
    // FachbegriffItem-Verwaltung
    // ======================================================

    public void getAllFachbegriffe(long requestId) throws IOException {
        logger.debug("getAllFachbegriffe() aufgerufen mit Request-ID: {}", requestId);
        state.getAllFachbegriffe(this, requestId);
    }

    public void createFachbegriff(FachbegriffItem item, long requestId) throws IOException {
        logger.debug("createFachbegriff() aufgerufen mit Request-ID: {}", requestId);
        state.createFachbegriff(this, item, requestId);
    }

    public void updateFachbegriff(long id, FachbegriffItem item, long requestId) throws IOException {
        logger.debug("updateFachbegriff() aufgerufen für ID {} mit Request-ID: {}", id, requestId);
        state.updateFachbegriff(this, id, item, requestId);
    }

    public void deleteFachbegriff(long id, long requestId) throws IOException {
        logger.debug("deleteFachbegriff() aufgerufen für ID {} mit Request-ID: {}", id, requestId);
        state.deleteFachbegriff(this, id, requestId);
    }

    // ======================================================
    // Quiz-Template-Verwaltung
    // ======================================================

    public void getAllQuizTemplates(long requestId) throws IOException {
        logger.debug("getAllQuizTemplates() aufgerufen mit Request-ID: {}", requestId);
        state.getAllQuizTemplates(this, requestId);
    }

    public void createQuizTemplate(Quiz quiz, long requestId) throws IOException {
        logger.debug("createQuizTemplate() aufgerufen mit Request-ID: {}", requestId);
        state.createQuizTemplate(this, quiz, requestId);
    }

    public void updateQuizTemplate(long id, Quiz quiz, long requestId) throws IOException {
        logger.debug("updateQuizTemplate() aufgerufen für ID {} mit Request-ID: {}", id, requestId);
        state.updateQuizTemplate(this, id, quiz, requestId);
    }

    public void deleteQuizTemplate(long id, long requestId) throws IOException {
        logger.debug("deleteQuizTemplate() aufgerufen für ID {} mit Request-ID: {}", id, requestId);
        state.deleteQuizTemplate(this, id, requestId);
    }

    // ======================================================
    // Quiz-Templates für Schüler
    // ======================================================

    public void getQuizTemplatesForSchueler(long requestId) throws IOException {
        logger.debug("getQuizTemplatesForSchueler() aufgerufen mit Request-ID: {}", requestId);
        state.getQuizTemplatesForSchueler(this, requestId);
    }
}
