package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.ResponsePacket;
import at.tgm.objects.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CPOSTAllQuizTemplates implements ResponsePacket {

    private static final Logger logger = LoggerFactory.getLogger(S2CPOSTAllQuizTemplates.class);
    private long requestId;
    private Quiz[] quizTemplates;

    public S2CPOSTAllQuizTemplates(Quiz[] quizTemplates) {
        this.quizTemplates = quizTemplates;
    }

    public S2CPOSTAllQuizTemplates() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID ZUERST mitsenden

        // Null-Schutz
        if (quizTemplates == null) {
            out.writeInt(0);
            return;
        }

        // Länge schreiben
        out.writeInt(quizTemplates.length);

        // Items einzeln schreiben
        for (Quiz quiz : quizTemplates) {
            quiz.encode(out);
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID ZUERST lesen
        int size = in.readInt();

        quizTemplates = new Quiz[size];

        // Items einlesen
        for (int i = 0; i < size; i++) {
            quizTemplates[i] = Quiz.decode(in);
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

    public Quiz[] getQuizTemplates() {
        return quizTemplates;
    }

    @Override
    public void handle(NetworkContext ctx) {
        logger.debug("Quiz-Templates-Paket empfangen mit {} Templates", quizTemplates != null ? quizTemplates.length : 0);
        // Wird vom Client verarbeitet
    }
}
