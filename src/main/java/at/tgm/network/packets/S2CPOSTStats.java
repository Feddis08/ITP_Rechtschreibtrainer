package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.client.GuiController;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CPOSTStats implements Packet {

    private Quiz[] quizzes;

    public S2CPOSTStats(Quiz[] quizzes) {
        this.quizzes = quizzes;
    }

    public S2CPOSTStats() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        // Null-Schutz
        if (quizzes == null) {
            out.writeInt(0);
            return;
        }

        // Länge schreiben
        out.writeInt(quizzes.length);

        // Items einzeln schreiben
        for (Quiz item : quizzes) {
            item.encode(out);
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        int size = in.readInt();

        quizzes = new Quiz[size];

        // Items einlesen
        for (int i = 0; i < size; i++) {
            quizzes[i] = FachbegriffItem.decode(in);
        }
    }

    @Override
    public void handle(NetworkContext ctx) {
        Client.GUI.showStats(quizzes);
    }
}
