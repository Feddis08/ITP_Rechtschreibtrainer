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

public class C2SPUTQuizTemplate implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SPUTQuizTemplate.class);
    private long requestId;
    private long id;
    private Quiz quiz;

    public C2SPUTQuizTemplate() {
    }

    public C2SPUTQuizTemplate(long id, Quiz quiz) {
        this.id = id;
        this.quiz = quiz;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        out.writeLong(id);
        if (quiz != null) {
            quiz.encode(out);
        } else {
            throw new IOException("QuizTemplate darf nicht null sein");
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        id = in.readLong();
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

    public long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof Lehrer) {
            logger.info("QuizTemplate-Aktualisierung von Lehrer: {} (ID: {}, Request-ID: {})", username, id, requestId);
            try {
                ((ServerClient) sc).updateQuizTemplate(id, quiz, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Aktualisieren des QuizTemplates für Lehrer: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("QuizTemplate-Aktualisierung von nicht-Lehrer oder nicht-ServerClient: {}", username);
        }
    }
}
