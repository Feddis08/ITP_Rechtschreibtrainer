package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.ResponsePacket;
import at.tgm.objects.Lehrer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CPOSTAllLehrer implements ResponsePacket {

    private static final Logger logger = LoggerFactory.getLogger(S2CPOSTAllLehrer.class);
    private long requestId;
    private Lehrer[] l;

    public S2CPOSTAllLehrer(Lehrer[] l) {
        this.l = l;
    }

    public S2CPOSTAllLehrer() {
    }

    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID ZUERST mitsenden
        
        // Null-Schutz
        if (l == null) {
            out.writeInt(0);
            return;
        }

        // Länge schreiben
        out.writeInt(l.length);

        // Items einzeln schreiben
        for (Lehrer item : l) {
            item.encode(out);
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID ZUERST lesen
        int size = in.readInt();

        l = new Lehrer[size];

        // Items einlesen
        for (int i = 0; i < size; i++) {
            l[i] = Lehrer.decode(in);
        }

    }
    
    @Override
    public long getRequestId() {
        return requestId;
    }
    
    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }
    
    public Lehrer[] getLehrer() {
        return l;
    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.info("Lehrerliste-Paket empfangen mit {} Lehrern", l != null ? l.length : 0);
        at.tgm.client.Client.onLehrerListReceived(l);
    }
}
