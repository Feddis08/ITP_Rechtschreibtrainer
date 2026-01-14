package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SGETOwnAccount implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SGETOwnAccount.class);
    private long requestId;

    public C2SGETOwnAccount() {
    }

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
        at.tgm.network.core.SocketClient sc = ((at.tgm.network.core.SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof at.tgm.server.ServerClient) {
            logger.info("GETOwnAccount-Anfrage von: {} (Request-ID: {})", username, requestId);
            try {
                ((at.tgm.server.ServerClient) sc).getOwnAccount(requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Verarbeiten des GETOwnAccount für: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("GETOwnAccount-Anfrage von nicht-ServerClient: {}", username);
        }
    }
}
