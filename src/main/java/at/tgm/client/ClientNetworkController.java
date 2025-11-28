package at.tgm.client;

import at.tgm.network.core.NetworkSystem;
import at.tgm.objects.Distro;
import at.tgm.objects.SocketClient;

import java.io.*;
import java.net.Socket;

public class ClientNetworkController {

    public static SocketClient socketClient;
    public static void connect() {
        NetworkSystem.init();
        String host = "localhost";
        int port = 5123;

        try {
            Socket socket = new Socket(host, port);
            System.out.println("[CLIENT] Connected to server!");
            socketClient = new SocketClient(socket, Distro.CLIENT);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
