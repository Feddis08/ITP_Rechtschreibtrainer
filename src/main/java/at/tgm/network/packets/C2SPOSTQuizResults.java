package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.FachbegriffItem;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SPOSTQuizResults implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(C2SPOSTQuizResults.class);

    private FachbegriffItem[] fachbegriffItems;

    public C2SPOSTQuizResults(){

    }
    public C2SPOSTQuizResults(FachbegriffItem[] fachbegriffItems) {
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
        if (ctx instanceof ServerClient) {
            ServerClient serverClient = (ServerClient) ctx;
            String username = serverClient.getNutzer() != null ? serverClient.getNutzer().getUsername() : "unknown";
            logger.info("Quiz-Ergebnisse empfangen von: {} ({} Items)", username, 
                       fachbegriffItems != null ? fachbegriffItems.length : 0);
            ((ServerClient) ctx).finishQuiz(fachbegriffItems);
        } else {
            logger.warn("Quiz-Ergebnisse von nicht-ServerClient empfangen: {}", ctx.getClass().getSimpleName());
        }
    }
}
