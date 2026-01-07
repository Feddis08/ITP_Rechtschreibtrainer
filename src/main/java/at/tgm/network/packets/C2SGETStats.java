package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.server.ServerClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SGETStats implements Packet {
    @Override
    public void encode(DataOutputStream out) throws IOException {

    }

    @Override
    public void decode(DataInputStream in) throws IOException {

    }

    @Override
    public void handle(NetworkContext ctx) {
        if (ctx instanceof ServerClient) {
            ((ServerClient) ctx).postStats();
        }
    }
}
