package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.ResponsePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CLoginFailedPacket implements ResponsePacket {

    private static final Logger logger = LoggerFactory.getLogger(S2CLoginFailedPacket.class);
    private long requestId;
    
    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
    }
    
    @Override
    public long getRequestId() {
        return requestId;
    }
    
    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.warn("Login-Failed-Paket empfangen");
        Client.failedLogin();
    }
}
