package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CPOSTAllSchueler implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(S2CPOSTAllSchueler.class);
    private Schueler[] s;

    public S2CPOSTAllSchueler(Schueler[] s) {
        this.s = s;
    }

    public S2CPOSTAllSchueler() {
    }

    public void encode(DataOutputStream out) throws IOException {
        // Null-Schutz
        if (s == null) {
            out.writeInt(0);
            return;
        }

        // Länge schreiben
        out.writeInt(s.length);

        // Items einzeln schreiben
        for (Schueler item : s) {
            item.encode(out);
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        int size = in.readInt();

        s = new Schueler[size];

        // Items einlesen
        for (int i = 0; i < size; i++) {
            s[i] = Schueler.decode(in);
        }

    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.info("Schülerliste-Paket empfangen mit {} Schülern", s != null ? s.length : 0);
        Client.dashboardFrame.showSchuelerList(s);
    }
}
