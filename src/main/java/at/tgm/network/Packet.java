package at.tgm.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet {
    void encode(DataOutputStream out) throws IOException;
    void decode(DataInputStream in) throws IOException;
    void handle(NetworkContext ctx);
}
