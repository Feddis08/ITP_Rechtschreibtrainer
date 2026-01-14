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

public class C2SGETSchuelerStats implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SGETSchuelerStats.class);
    private long requestId;
    private String schuelerUsername;

    public C2SGETSchuelerStats() {
    }

    public C2SGETSchuelerStats(String schuelerUsername) {
        this.schuelerUsername = schuelerUsername;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        out.writeUTF(schuelerUsername != null ? schuelerUsername : "");
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        schuelerUsername = in.readUTF();
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    public String getSchuelerUsername() {
        return schuelerUsername;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof Lehrer) {
            logger.info("SchuelerStats-Anfrage von Lehrer: {} für Schüler: {} (Request-ID: {})", 
                       username, schuelerUsername, requestId);
            try {
                ((ServerClient) sc).postSchuelerStats(schuelerUsername, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Verarbeiten der SchuelerStats-Anfrage für Lehrer: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("SchuelerStats-Anfrage von nicht-Lehrer oder nicht-ServerClient: {}", username);
        }
    }
}
