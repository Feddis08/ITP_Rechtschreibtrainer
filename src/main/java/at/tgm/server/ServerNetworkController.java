package at.tgm.server;

import at.tgm.network.NetworkSystem;
import at.tgm.objects.Nutzer;

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

                addClient(new SocketClient(client));


            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addClient(SocketClient client){

            System.out.println("adding new client");

            int i = 0;
            for (SocketClient c : clients){
                if (c == null){
                    clients[i] = client;
                    return;
                }
                i ++;
            }

            i++;
            SocketClient[] clientsNeu = new SocketClient[i];
            i = 0;
            for (SocketClient c : clients){
                clientsNeu[i] = clients[i];
                i ++;
            }

            clientsNeu[i] = client;
            clients = clientsNeu;
    }

}
