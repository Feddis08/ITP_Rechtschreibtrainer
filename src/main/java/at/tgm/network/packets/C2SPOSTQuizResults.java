package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.FachbegriffItem;
import at.tgm.server.ServerSchuelerClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SPOSTQuizResults implements Packet {

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

        ServerSchuelerClient sc = (ServerSchuelerClient) ctx;

        sc.finishQuiz(fachbegriffItems);

    }
}
