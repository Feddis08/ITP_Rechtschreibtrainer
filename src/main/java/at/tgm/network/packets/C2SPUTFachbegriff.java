package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Lehrer;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SPUTFachbegriff implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SPUTFachbegriff.class);
    private long requestId;
    private long id;
    private FachbegriffItem fachbegriff;

    public C2SPUTFachbegriff() {
    }

    public C2SPUTFachbegriff(long id, FachbegriffItem fachbegriff) {
        this.id = id;
        this.fachbegriff = fachbegriff;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        out.writeLong(id);
        if (fachbegriff != null) {
            fachbegriff.encode(out);
        } else {
            throw new IOException("FachbegriffItem darf nicht null sein");
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        id = in.readLong();
        fachbegriff = FachbegriffItem.decode(in);
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

    public FachbegriffItem getFachbegriff() {
        return fachbegriff;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof Lehrer) {
            logger.info("Fachbegriff-Aktualisierung von Lehrer: {} (ID: {}, Request-ID: {})", username, id, requestId);
            try {
                ((ServerClient) sc).updateFachbegriff(id, fachbegriff, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Aktualisieren des Fachbegriffs für Lehrer: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("Fachbegriff-Aktualisierung von nicht-Lehrer oder nicht-ServerClient: {}", username);
        }
    }
}
