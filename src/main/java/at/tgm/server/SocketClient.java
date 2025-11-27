package at.tgm.server;

import at.tgm.network.core.NetworkChannel;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.Nutzer;

import java.io.IOException;
import java.net.Socket;

public class SocketClient extends NetworkContext {

    private final NetworkChannel channel;

    private Nutzer nutzer; // dein User-Objekt

    public SocketClient(Socket socket) throws IOException {
        super(socket);
        this.channel = new NetworkChannel(socket, this);
    }

    public Nutzer getNutzer() {
        return nutzer;
    }

    public void setNutzer(Nutzer nutzer) {
        this.nutzer = nutzer;
    }

    public NetworkChannel getChannel() {
        return channel;
    }

    public void send(Packet packet) throws IOException {
        channel.send(packet);
    }
}
