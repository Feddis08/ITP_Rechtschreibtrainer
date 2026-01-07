package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CLoginFailedPacket implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(S2CLoginFailedPacket.class);
    @Override
    public void encode(DataOutputStream out) throws IOException {

    }

    @Override
    public void decode(DataInputStream in) throws IOException {

    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.warn("Login-Failed-Paket empfangen");
        Client.failedLogin();
    }
}
