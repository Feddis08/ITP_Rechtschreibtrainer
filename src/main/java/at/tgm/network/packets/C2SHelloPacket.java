package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SHelloPacket implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(C2SHelloPacket.class);

    private String systemInfo;

    public C2SHelloPacket(String systemInfo) {
        this.systemInfo = systemInfo;
    }
    public C2SHelloPacket() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeUTF(this.systemInfo);
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        this.systemInfo = in.readUTF();
    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.info("Hello-Paket empfangen vom Client: {}", this.systemInfo);
    }
}
