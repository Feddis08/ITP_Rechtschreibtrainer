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

public class C2SGETAllLehrer implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SGETAllLehrer.class);
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

         SocketClient sc = ((SocketClient) ctx);
         String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

         if (sc instanceof ServerClient && sc.getNutzer() instanceof SysAdmin){
             logger.info("Lehrerliste-Anfrage von SysAdmin: {} (Request-ID: {})", username, requestId);
             try {
                 ((ServerClient) sc).postAllLehrer(requestId);
             } catch (IOException e) {
                 logger.error("Fehler beim Senden der Lehrerliste an SysAdmin: {}", username, e);
                 throw new RuntimeException(e);
             }
         } else {
             logger.warn("Lehrerliste-Anfrage von nicht-SysAdmin oder nicht-ServerClient: {}", username);
         }

    }
}
