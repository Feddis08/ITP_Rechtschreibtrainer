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

public class S2CPOSTOwnAccount implements ResponsePacket {

    private static final Logger logger = LoggerFactory.getLogger(S2CPOSTOwnAccount.class);

    private long requestId;
    private Nutzer nutzer;

    public S2CPOSTOwnAccount() {}

    public S2CPOSTOwnAccount(Nutzer nutzer) {
        this.nutzer = nutzer;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID ZUERST mitsenden
        nutzer.encode(out);  // ALLES automatisch
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID ZUERST lesen
        this.nutzer = SendableObject.decode(in); // richtige Subklasse automatisch!
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
        return nutzer;
    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.debug("S2CPOSTOwnAccount empfangen für: {}", nutzer != null ? nutzer.getUsername() : "null");
        // Response wird normalerweise vom Client verarbeitet
        if (Client.GUI != null && nutzer != null) {
            Client.GUI.updateOwnAccount(nutzer);
        }
    }
}
