package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SINITQuiz implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(C2SINITQuiz.class);

    public C2SINITQuiz() {
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {

    }

    @Override
    public void decode(DataInputStream in) throws IOException {

    }

    @Override
    public void handle(NetworkContext ctx) {
        if (ctx instanceof ServerClient) {
            ServerClient serverClient = (ServerClient) ctx;
            String username = serverClient.getNutzer() != null ? serverClient.getNutzer().getUsername() : "unknown";
            logger.info("Quiz-Initialisierungsanfrage von: {}", username);
            try {
                ((ServerClient) ctx).startQuiz();
            } catch (IOException e) {
                logger.error("Fehler beim Starten des Quiz für: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("C2SINITQuiz von nicht-ServerClient empfangen: {}", ctx.getClass().getSimpleName());
        }
    }
}
