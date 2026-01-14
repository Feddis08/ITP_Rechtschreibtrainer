package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.Note;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SSetSchuelerNote implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SSetSchuelerNote.class);
    private long requestId;
    private String schuelerUsername;
    private Note note;

    public C2SSetSchuelerNote() {
    }

    public C2SSetSchuelerNote(String schuelerUsername, Note note) {
        this.schuelerUsername = schuelerUsername;
        this.note = note;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        out.writeUTF(schuelerUsername != null ? schuelerUsername : "");
        if (note != null) {
            note.encode(out);
        } else {
            throw new IOException("Note darf nicht null sein");
        }
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        schuelerUsername = in.readUTF();
        note = at.tgm.objects.Note.decode(in);
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    public String getSchuelerUsername() {
        return schuelerUsername;
    }

    public Note getNote() {
        return note;
    }

    @Override
    public void handle(NetworkContext ctx) {
        SocketClient sc = ((SocketClient) ctx);
        String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

        if (sc instanceof ServerClient && sc.getNutzer() instanceof at.tgm.objects.Lehrer) {
            logger.info("SetSchuelerNote-Anfrage von Lehrer: {} für Schüler: {} (Request-ID: {})", 
                       username, schuelerUsername, requestId);
            try {
                ((ServerClient) sc).setSchuelerNote(schuelerUsername, note, requestId);
            } catch (IOException e) {
                logger.error("Fehler beim Verarbeiten des SetSchuelerNote für Lehrer: {}", username, e);
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("SetSchuelerNote-Anfrage von nicht-Lehrer oder nicht-ServerClient: {}", username);
        }
    }
}
