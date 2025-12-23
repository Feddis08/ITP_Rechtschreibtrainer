package at.tgm.server;

import at.tgm.network.core.SocketClient;
import at.tgm.objects.Distro;

import java.io.IOException;
import java.net.Socket;

public class ServerClient extends SocketClient {

    public ServerClient(Socket socket) throws IOException {
        super(socket, Distro.SERVER);
    }

}
