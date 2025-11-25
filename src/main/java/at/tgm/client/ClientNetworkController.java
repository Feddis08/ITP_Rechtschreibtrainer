package at.tgm.client;

import at.tgm.network.NetworkChannel;
import at.tgm.network.NetworkSystem;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientNetworkController {

    public static NetworkChannel networkChannel;
    public static void connect() {
        NetworkSystem.init();
        String host = "localhost";
        int port = 5123;

        try {
            Socket socket = new Socket(host, port);
            System.out.println("[CLIENT] Connected to server!");
            networkChannel = new NetworkChannel(socket);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
