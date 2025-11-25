package at.tgm.client;

import at.tgm.network.C2SHelloPacket;

import java.io.IOException;

public class Client {

    public static AnmeldeController ac;
    public static void main(String[] args) throws IOException {

        ClientNetworkController.connect();

        ac = new AnmeldeController();

        ClientNetworkController.networkChannel.send(new C2SHelloPacket("test"));
    }
}
