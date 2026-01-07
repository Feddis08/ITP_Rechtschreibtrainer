package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CResultOfQuiz implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(S2CResultOfQuiz.class);
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
        logger.info("Quiz-Ergebnis empfangen: {}/{} Punkte ({} Items)", points, maxPoints, 
                   fgs != null ? fgs.length : 0);
        
        // Use Client callback for test mode
        Client.onQuizResult(fgs, points, maxPoints);
        
        // Also try to show in GUI if available (for normal operation)
        SwingUtilities.invokeLater(() -> {
            if (Client.dashboardFrame != null && Client.dashboardFrame.getQuizPanel() != null) {
                Client.dashboardFrame.getQuizPanel().showResults(fgs, points, maxPoints);
                logger.debug("Quiz-Ergebnis im UI angezeigt");
            }
        });
    }
}
