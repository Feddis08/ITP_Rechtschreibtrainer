package at.tgm.network.core;

import java.net.Socket;

public abstract class NetworkContext {

    protected final Socket socket;

    public NetworkContext(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
