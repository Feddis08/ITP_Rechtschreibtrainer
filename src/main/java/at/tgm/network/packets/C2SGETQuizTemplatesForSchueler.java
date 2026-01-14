package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.Schueler;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SGETQuizTemplatesForSchueler implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SGETQuizTemplatesForSchueler.class);
    private long requestId;

    public C2SGETQuizTemplatesForSchueler() {
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
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof Schueler) {
            logger.info("Quiz-Templates-Anfrage von Schüler: {} (Request-ID: {})", username, requestId);
            try {
                ((ServerClient) sc).getQuizTemplatesForSchueler(requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Senden der Quiz-Templates an Schüler: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("Quiz-Templates-Anfrage von nicht-Schüler oder nicht-ServerClient: {}", username);
        }
    }
}
