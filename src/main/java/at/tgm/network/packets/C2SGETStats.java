package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SGETStats implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SGETStats.class);
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
        if (ctx instanceof ServerClient) {
            ServerClient serverClient = (ServerClient) ctx;
            String username = serverClient.getNutzer() != null ? serverClient.getNutzer().getUsername() : "unknown";
            logger.info("Statistik-Anfrage von: {} (Request-ID: {})", username, requestId);
            
            // Request-ID an postStats übergeben, damit sie in die Response übernommen wird
            ((ServerClient) ctx).postStats(requestId);
        } else {
            logger.warn("Statistik-Anfrage von nicht-ServerClient: {}", ctx.getClass().getSimpleName());
        }
    }
}
