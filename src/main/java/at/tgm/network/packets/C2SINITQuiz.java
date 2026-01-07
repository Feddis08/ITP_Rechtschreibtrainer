package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.server.ServerClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SINITQuiz implements Packet {

    public C2SINITQuiz() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {

    }

    @Override
    public void decode(DataInputStream in) throws IOException {

    }

    @Override
    public void handle(NetworkContext ctx) {
        if (ctx instanceof ServerClient) {
            try {
                ((ServerClient) ctx).startQuiz();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
