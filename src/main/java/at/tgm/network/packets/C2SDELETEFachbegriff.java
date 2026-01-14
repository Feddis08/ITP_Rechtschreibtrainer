package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.Lehrer;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SDELETEFachbegriff implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SDELETEFachbegriff.class);
    private long requestId;
    private long id;

    public C2SDELETEFachbegriff() {
    }

    public C2SDELETEFachbegriff(long id) {
        this.id = id;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        out.writeLong(id);
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        id = in.readLong();
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof Lehrer) {
            logger.info("Fachbegriff-Löschung von Lehrer: {} (ID: {}, Request-ID: {})", username, id, requestId);
            try {
                ((ServerClient) sc).deleteFachbegriff(id, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Löschen des Fachbegriffs für Lehrer: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("Fachbegriff-Löschung von nicht-Lehrer oder nicht-ServerClient: {}", username);
        }
    }
}
