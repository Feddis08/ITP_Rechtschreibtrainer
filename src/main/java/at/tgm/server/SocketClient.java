package at.tgm.server;

import at.tgm.network.NetworkChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {

    Socket client;

    NetworkChannel networkChannel;

    public SocketClient(Socket client) throws IOException {
        this.client = client;
        this.networkChannel = new NetworkChannel(client);
    }

    public Socket getClient() {
        return client;
    }



}
