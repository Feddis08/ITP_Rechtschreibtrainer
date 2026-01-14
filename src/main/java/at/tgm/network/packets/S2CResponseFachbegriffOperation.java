package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.ResponsePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CResponseFachbegriffOperation implements ResponsePacket {

    private static final Logger logger = LoggerFactory.getLogger(S2CResponseFachbegriffOperation.class);
    private long requestId;
    private boolean success;
    private String message;

    public S2CResponseFachbegriffOperation() {
    }

    public S2CResponseFachbegriffOperation(boolean success, String message) {
        this.success = success;
        this.message = message != null ? message : "";
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeLong(requestId); // Request-ID mitsenden
        out.writeBoolean(success);
        out.writeUTF(message != null ? message : "");
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        success = in.readBoolean();
        message = in.readUTF();
    }

    @Override
    public void handle(NetworkContext ctx) {
        // Response-Packets werden normalerweise nicht auf dem Server gehandled,
        // sondern direkt vom Client verarbeitet
        logger.debug("S2CResponseFachbegriffOperation empfangen: success={}, message={}", success, message);
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
