package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.Schueler;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SSTARTQuizWithTemplate implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(C2SSTARTQuizWithTemplate.class);

    private long templateId; // 0 = zufälliges Quiz (Legacy)

    public C2SSTARTQuizWithTemplate() {
    }

    public C2SSTARTQuizWithTemplate(long templateId) {
        this.templateId = templateId;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(templateId);
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        templateId = in.readLong();
    }

    @Override
    public void handle(NetworkContext ctx) {
        if (ctx instanceof ServerClient) {
            ServerClient serverClient = (ServerClient) ctx;
            String username = serverClient.getNutzer() != null ? serverClient.getNutzer().getUsername() : "unknown";
            logger.info("Quiz-Start-Anfrage von: {} (Template-ID: {})", username, templateId);
            try {
                ((ServerClient) ctx).startQuiz(templateId);
            } catch (IOException e) {
                logger.error("Fehler beim Starten des Quiz für: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("C2SSTARTQuizWithTemplate von nicht-ServerClient empfangen: {}", ctx.getClass().getSimpleName());
        }
    }

    public long getTemplateId() {
        return templateId;
    }
}
