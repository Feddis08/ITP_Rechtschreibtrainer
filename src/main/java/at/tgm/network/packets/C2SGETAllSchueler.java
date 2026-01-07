package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.Lehrer;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SGETAllSchueler implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(C2SGETAllSchueler.class);
    @Override
    public void encode(DataOutputStream out) throws IOException {

    }

    @Override
    public void decode(DataInputStream in) throws IOException {

    }

    @Override
    public void handle(NetworkContext ctx) {

         SocketClient sc = ((SocketClient) ctx);
         String username = sc.getNutzer() != null ? sc.getNutzer().getUsername() : "unknown";

         if (sc instanceof ServerClient && sc.getNutzer() instanceof Lehrer){
             logger.info("Schülerliste-Anfrage von Lehrer: {}", username);
             try {
                 ((ServerClient) sc).postAllSchueler();
             } catch (IOException e) {
                 logger.error("Fehler beim Senden der Schülerliste an Lehrer: {}", username, e);
                 throw new RuntimeException(e);
             }
         } else {
             logger.warn("Schülerliste-Anfrage von nicht-Lehrer oder nicht-ServerClient: {}", username);
         }

    }
}
