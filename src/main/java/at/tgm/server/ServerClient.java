package at.tgm.server;

import at.tgm.network.core.SocketClient;
import at.tgm.objects.Distro;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;

import java.io.IOException;
import java.net.Socket;

public class ServerClient extends SocketClient {

    private ClientState state;

    public ServerClient(Socket socket) throws IOException {
        super(socket, Distro.SERVER);
        this.state = new UnauthenticatedState();
    }

    public ClientState getState() {
        return state;
    }

    public void setState(ClientState state) {
        this.state = state;
    }

    // Delegationsmethoden für rollenspezifische Funktionalität

    public void postAllSchueler() throws IOException {
        state.postAllSchueler(this);
    }

    public void addQuiz(Quiz q) {
        state.addQuiz(this, q);
    }

    public void startQuiz() throws IOException {
        state.startQuiz(this);
    }

    public void finishQuiz(FachbegriffItem[] fgs) {
        state.finishQuiz(this, fgs);
    }

    public void postStats() {
        state.postStats(this);
    }
}
