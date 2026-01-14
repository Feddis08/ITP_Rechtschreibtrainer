package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.SysAdmin;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SDeleteLehrer implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SDeleteLehrer.class);
    private long requestId;
    private String lehrerUsername;

    public C2SDeleteLehrer() {
    }

    public C2SDeleteLehrer(String lehrerUsername) {
        this.lehrerUsername = lehrerUsername;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        out.writeUTF(lehrerUsername != null ? lehrerUsername : "");
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        lehrerUsername = in.readUTF();
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    public String getLehrerUsername() {
        return lehrerUsername;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof SysAdmin) {
            logger.info("DeleteLehrer-Anfrage von SysAdmin: {} für Lehrer: {} (Request-ID: {})", 
                       username, lehrerUsername, requestId);
            try {
                ((ServerClient) sc).deleteLehrer(lehrerUsername, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Verarbeiten des DeleteLehrer für SysAdmin: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("DeleteLehrer-Anfrage von nicht-SysAdmin oder nicht-ServerClient: {}", username);
        }
    }
}
