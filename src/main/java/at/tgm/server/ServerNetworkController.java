package at.tgm.server;

import at.tgm.network.core.NetworkSystem;
import at.tgm.network.core.SocketClient;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNetworkController {

    public static SocketClient clients[];

    public static void start(int port) {
        NetworkSystem.init();


        clients = new SocketClient[0];

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[SERVER] Running on port " + port + "...");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("[SERVER] Client connected: " + client.getInetAddress());

                addClient(new ServerClient(client));


            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void removeClient(SocketClient client) {

        System.out.println("removing old client");

        for (int i = 0; i < clients.length; i++) {
            SocketClient c = clients[i];

            if (c == null) continue;

            if (c.getSocket().getRemoteSocketAddress()
                    .equals(client.getSocket().getRemoteSocketAddress())) {

                try {
                    c.getSocket().close();  // reicht völlig!
                } catch (IOException ignored) {}

                clients[i] = null;
                return;
            }
        }
    }


    public static void addClient(SocketClient client){
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        System.out.println("adding new client");

        // Suche nach freiem Platz im Array
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                clients[i] = client;
                return;
            }
        }

        // Kein freier Platz gefunden - Array vergrößern
        SocketClient[] clientsNeu = new SocketClient[clients.length + 1];
        System.arraycopy(clients, 0, clientsNeu, 0, clients.length);
        clientsNeu[clients.length] = client;
        clients = clientsNeu;
    }




}
