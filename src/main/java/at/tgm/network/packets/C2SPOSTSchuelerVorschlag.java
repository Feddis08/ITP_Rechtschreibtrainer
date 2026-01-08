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

public class C2SPOSTSchuelerVorschlag implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SPOSTSchuelerVorschlag.class);
    private long requestId;
    private Schueler schueler;

    public C2SPOSTSchuelerVorschlag() {
    }

    public C2SPOSTSchuelerVorschlag(Schueler schueler) {
        this.schueler = schueler;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        if (schueler != null) {
            schueler.encode(out);
        } else {
            throw new IOException("Schueler darf nicht null sein");
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        schueler = Schueler.decode(in);
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    public Schueler getSchueler() {
        return schueler;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof at.tgm.objects.Lehrer) {
            logger.info("SchuelerVorschlag-Anfrage von Lehrer: {} (Request-ID: {})", username, requestId);
            try {
                ((ServerClient) sc).addSchueler(schueler, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Verarbeiten des SchuelerVorschlags für Lehrer: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("SchuelerVorschlag-Anfrage von nicht-Lehrer oder nicht-ServerClient: {}", username);
        }
    }
}
