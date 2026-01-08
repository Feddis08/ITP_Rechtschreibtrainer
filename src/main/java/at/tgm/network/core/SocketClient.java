package at.tgm.network.core;

import at.tgm.objects.Distro;
import at.tgm.objects.Nutzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class SocketClient extends NetworkContext {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final NetworkChannel channel;

    private Nutzer nutzer;//user objekt

    private Distro distro;

    public SocketClient(Socket socket, Distro distro) throws IOException {
        super(socket);

        this.distro = distro;
        this.channel = new NetworkChannel(socket, this);
        logger.debug("SocketClient erstellt: Distro={}, Remote={}", distro, socket.getRemoteSocketAddress());
    }

    public Distro getDistro() {
        return distro;
    }

    public Nutzer getNutzer() {
        return nutzer;
    }

    public void setNutzer(Nutzer nutzer) {
        String username = nutzer != null ? nutzer.getUsername() : "null";
        logger.debug("Nutzer gesetzt: {}", username);
        this.nutzer = nutzer;
    }

    public NetworkChannel getChannel() {
        return channel;
    }

    public void send(Packet packet) throws IOException {
        logger.debug("Sende Paket über Channel: {}", packet.getClass().getSimpleName());
        channel.send(packet);
    }

}
