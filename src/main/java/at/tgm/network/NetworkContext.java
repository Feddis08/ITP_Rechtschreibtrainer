package at.tgm.network;

import java.io.IOException;
import java.net.Socket;

public class NetworkContext {

    private final Socket socket;
    private final NetworkChannel channel;

    public NetworkContext(Socket socket) throws IOException {
        this.socket = socket;
        this.channel = new NetworkChannel(socket); // erlaubt Antworten senden
    }

    public Socket getSocket() {
        return socket;
    }

    public void send(Packet packet) {
        try {
            channel.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
