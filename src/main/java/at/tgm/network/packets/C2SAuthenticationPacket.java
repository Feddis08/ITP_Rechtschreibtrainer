package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.Lehrer;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Schueler;
import at.tgm.server.Server;
import at.tgm.network.core.SocketClient;
import at.tgm.server.LehrerState;
import at.tgm.server.SchuelerState;
import at.tgm.server.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class C2SAuthenticationPacket implements Packet {

    private static final Logger logger = LoggerFactory.getLogger(C2SAuthenticationPacket.class);

    private String username;
    private String password;
    public C2SAuthenticationPacket() {
    }

    public C2SAuthenticationPacket(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        out.writeUTF(username);
        out.writeUTF(password);
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        this.username = in.readUTF();
        this.password = in.readUTF();
    }

    @Override
    public void handle(NetworkContext ctx) {
        if (ctx == null) {
            logger.error("NetworkContext ist null in C2SAuthenticationPacket");
            return;
        }

        if (this.username == null || this.username.isEmpty()) {
            logger.warn("Authentifizierungsversuch mit leerem oder null Username");
            try {
                if (ctx instanceof SocketClient) {
                    ((SocketClient) ctx).send(new S2CLoginFailedPacket());
                }
            } catch (IOException e) {
                logger.error("Fehler beim Senden des Login-Failed-Pakets", e);
            }
            return;
        }

        SocketClient client = (SocketClient) ctx;
        Nutzer n = Server.findNutzerByUsername(this.username);

        logger.info("Neue Anmeldung: {}", this.username);
        try {
            if (n != null && n.checkPassword(this.password)){

                // Client bleibt derselbe - nur State ändern
                if (client instanceof ServerClient) {
                    ServerClient serverClient = (ServerClient) client;
                    
                    // State basierend auf Nutzertyp setzen
                    if (n instanceof Schueler) {
                        serverClient.setState(new SchuelerState());
                        logger.debug("SchuelerState gesetzt für: {}", this.username);
                    } else if (n instanceof Lehrer) {
                        serverClient.setState(new LehrerState());
                        logger.debug("LehrerState gesetzt für: {}", this.username);
                    } else {
                        logger.warn("Unbekannter Nutzertyp: {} für Benutzer: {}", n.getClass().getName(), this.username);
                        client.send(new S2CLoginFailedPacket());
                        return;
                    }
                    
                    serverClient.setNutzer(n);
                    n.setStatus(at.tgm.objects.NutzerStatus.ONLINE);
                    serverClient.send(new S2CLoginPacket(n));

                    logger.info("Login erfolgreich für: {} (Typ: {}), Status auf ONLINE gesetzt", this.username, n.getClass().getSimpleName());
                } else {
                    logger.warn("Invalid client type für Authentifizierung: {}", client.getClass().getSimpleName());
                    client.send(new S2CLoginFailedPacket());
                }

            } else {
                logger.warn("Login fehlgeschlagen für: {} (Benutzer nicht gefunden oder falsches Passwort)", this.username);
                client.send(new S2CLoginFailedPacket());
            }
        } catch (IOException e) {
            logger.error("Fehler während der Authentifizierung für: {}", this.username, e);
            // IOException wird nicht weitergeworfen, da handle() keine IOException deklariert
        }
    }
}
