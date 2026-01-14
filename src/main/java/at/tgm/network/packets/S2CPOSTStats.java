package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.ResponsePacket;
import at.tgm.objects.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CPOSTStats implements ResponsePacket {

    private static final Logger logger = LoggerFactory.getLogger(S2CPOSTStats.class);

    private long requestId;
    private Quiz[] quizzes;

    public S2CPOSTStats(Quiz[] quizzes) {
        this.quizzes = quizzes;
    }

    public S2CPOSTStats() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID ZUERST mitsenden
        
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
        requestId = in.readLong(); // Request-ID ZUERST lesen
        int size = in.readInt();

        quizzes = new Quiz[size];

        // Items einlesen
        for (int i = 0; i < size; i++) {
            quizzes[i] = Quiz.decode(in);
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
    
    public Quiz[] getQuizzes() {
        return quizzes;
    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.info("Statistik-Paket empfangen mit {} Quizzes", quizzes != null ? quizzes.length : 0);
        Client.GUI.showStats(quizzes);
    }
}
