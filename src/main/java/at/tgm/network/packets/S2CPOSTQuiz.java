package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.FachbegriffItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CPOSTQuiz implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(S2CPOSTQuiz.class);

    private FachbegriffItem[] fachbegriffItems;

    public S2CPOSTQuiz(){

    }
    public S2CPOSTQuiz(FachbegriffItem[] fachbegriffItems) {
        this.fachbegriffItems = fachbegriffItems;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        // Null-Schutz
        if (fachbegriffItems == null) {
            out.writeInt(0);
            return;
        }

        // Länge schreiben
        out.writeInt(fachbegriffItems.length);

        // Items einzeln schreiben
        for (FachbegriffItem item : fachbegriffItems) {
            item.encode(out);
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        int size = in.readInt();

        fachbegriffItems = new FachbegriffItem[size];

        // Items einlesen
        for (int i = 0; i < size; i++) {
            fachbegriffItems[i] = FachbegriffItem.decode(in);
        }
    }


    @Override
    public void handle(NetworkContext ctx) {
        logger.info("Quiz-Paket empfangen mit {} Items", 
                   fachbegriffItems != null ? fachbegriffItems.length : 0);
        Client.startQuiz(this.fachbegriffItems);
    }
}
