package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.ResponsePacket;
import at.tgm.objects.Nutzer;
import at.tgm.objects.SendableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CLoginPacket implements ResponsePacket {

    private static final Logger logger = LoggerFactory.getLogger(S2CLoginPacket.class);

    private long requestId;
    private Nutzer n;

    public S2CLoginPacket() {}

    public S2CLoginPacket(Nutzer nutzer) {
        this.n = nutzer;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID ZUERST mitsenden
        n.encode(out);  // ALLES automatisch
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID ZUERST lesen
        this.n = SendableObject.decode(in); // richtige Subklasse automatisch!
    }
    
    @Override
    public long getRequestId() {
        return requestId;
    }
    
    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }
    
    public Nutzer getNutzer() {
        return n;
    }

    @Override
    public void handle(NetworkContext ctx) {
        String username = n != null ? n.getUsername() : "unknown";
        logger.info("Login-Paket empfangen für: {}", username);
        Client.login(n);
    }
}
