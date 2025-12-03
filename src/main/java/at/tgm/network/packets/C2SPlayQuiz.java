package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.server.ServerClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SPlayQuiz implements Packet {

    public C2SPlayQuiz() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {

    }

    @Override
    public void decode(DataInputStream in) throws IOException {

    }

    @Override
    public void handle(NetworkContext ctx) {
        ServerClient sc = (ServerClient) ctx;

        try {
            sc.startQuiz();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
