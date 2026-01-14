package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.ResponsePacket;
import at.tgm.objects.FachbegriffItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CPOSTAllFachbegriffe implements ResponsePacket {

    private static final Logger logger = LoggerFactory.getLogger(S2CPOSTAllFachbegriffe.class);
    private long requestId;
    private FachbegriffItem[] fachbegriffe;

    public S2CPOSTAllFachbegriffe(FachbegriffItem[] fachbegriffe) {
        this.fachbegriffe = fachbegriffe;
    }

    public S2CPOSTAllFachbegriffe() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID ZUERST mitsenden

        // Null-Schutz
        if (fachbegriffe == null) {
            out.writeInt(0);
            return;
        }

        // Länge schreiben
        out.writeInt(fachbegriffe.length);

        // Items einzeln schreiben
        for (FachbegriffItem item : fachbegriffe) {
            item.encode(out);
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID ZUERST lesen
        int size = in.readInt();

        fachbegriffe = new FachbegriffItem[size];

        // Items einlesen
        for (int i = 0; i < size; i++) {
            fachbegriffe[i] = FachbegriffItem.decode(in);
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

    public FachbegriffItem[] getFachbegriffe() {
        return fachbegriffe;
    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.debug("Fachbegriffe-Paket empfangen mit {} Fachbegriffen", fachbegriffe != null ? fachbegriffe.length : 0);
        // Wird vom Client verarbeitet
    }
}
