package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CResultOfQuiz implements Packet {
    FachbegriffItem[] fgs;
    int points;
    int maxPoints;

    public S2CResultOfQuiz(FachbegriffItem[] fgs, int points, int maxPoints) {
        this.fgs = fgs;
        this.points = points;
        this.maxPoints = maxPoints;
    }

    public S2CResultOfQuiz() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        // Null-Schutz
        if (fgs == null) {
            out.writeInt(0);
            return;
        }

        // Länge schreiben
        out.writeInt(fgs.length);

        // Items einzeln schreiben
        for (FachbegriffItem item : fgs) {
            item.encode(out);
        }

        out.writeInt(points);
        out.writeInt(maxPoints);
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        int size = in.readInt();

        fgs = new FachbegriffItem[size];

        // Items einlesen
        for (int i = 0; i < size; i++) {
            fgs[i] = FachbegriffItem.decode(in);
        }

        this.points = in.readInt();
        this.maxPoints = in.readInt();
    }

    @Override
    public void handle(NetworkContext ctx) {
        SwingUtilities.invokeLater(() -> {
            if (Client.dashboardFrame != null && Client.dashboardFrame.getQuizPanel() != null) {
                Client.dashboardFrame.getQuizPanel().showResults(fgs, points, maxPoints);
            }
        });
    }
}
