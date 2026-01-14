package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.Lehrer;
import at.tgm.objects.SysAdmin;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SPOSTLehrerVorschlag implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SPOSTLehrerVorschlag.class);
    private long requestId;
    private Lehrer lehrer;

    public C2SPOSTLehrerVorschlag() {
    }

    public C2SPOSTLehrerVorschlag(Lehrer lehrer) {
        this.lehrer = lehrer;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        if (lehrer != null) {
            lehrer.encode(out);
        } else {
            throw new IOException("Lehrer darf nicht null sein");
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        lehrer = Lehrer.decode(in);
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    public Lehrer getLehrer() {
        return lehrer;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof SysAdmin) {
            logger.info("LehrerVorschlag-Anfrage von SysAdmin: {} (Request-ID: {})", username, requestId);
            try {
                ((ServerClient) sc).addLehrer(lehrer, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Verarbeiten des LehrerVorschlags für SysAdmin: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("LehrerVorschlag-Anfrage von nicht-SysAdmin oder nicht-ServerClient: {}", username);
        }
    }
}
