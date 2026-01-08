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

    public void startQuiz() throws IOException {
        logger.debug("startQuiz() aufgerufen");
        state.startQuiz(this);
    }

    public void finishQuiz(FachbegriffItem[] fgs) {
        logger.debug("finishQuiz() aufgerufen mit {} Items", fgs != null ? fgs.length : 0);
        state.finishQuiz(this, fgs);
    }

    public void postStats(long requestId) {
        logger.debug("postStats() aufgerufen mit Request-ID: {}", requestId);
        state.postStats(this, requestId);
    }
}
