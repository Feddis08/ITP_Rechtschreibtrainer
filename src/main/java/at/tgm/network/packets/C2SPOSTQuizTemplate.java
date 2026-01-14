package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.Lehrer;
import at.tgm.objects.Quiz;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SPOSTQuizTemplate implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SPOSTQuizTemplate.class);
    private long requestId;
    private Quiz quiz;

    public C2SPOSTQuizTemplate() {
    }

    public C2SPOSTQuizTemplate(Quiz quiz) {
        this.quiz = quiz;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        if (quiz != null) {
            quiz.encode(out);
        } else {
            throw new IOException("QuizTemplate darf nicht null sein");
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        quiz = Quiz.decode(in);
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof Lehrer) {
            logger.info("QuizTemplate-Erstellung von Lehrer: {} (Request-ID: {})", username, requestId);
            try {
                ((ServerClient) sc).createQuizTemplate(quiz, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Erstellen des QuizTemplates für Lehrer: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("QuizTemplate-Erstellung von nicht-Lehrer oder nicht-ServerClient: {}", username);
        }
    }
}
