package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.RequestPacket;
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


public class C2SAuthenticationPacket implements RequestPacket {

    private static final Logger logger = LoggerFactory.getLogger(C2SAuthenticationPacket.class);

    private long requestId;
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
        out.writeLong(requestId); // Request-ID mitsenden
        out.writeUTF(username);
        out.writeUTF(password);
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        requestId = in.readLong(); // Request-ID lesen
        this.username = in.readUTF();
        this.password = in.readUTF();
    }
    
    @Override
    public long getRequestId() {
        return requestId;
    }
    
    @Override
    public void setRequestId(long id) {
        this.requestId = id;
    }

    @Override
    public void handle(NetworkContext ctx) {
        if (ctx == null) {
            logger.error("NetworkContext ist null in C2SAuthenticationPacket");
            return;
        }

        SocketClient client = (SocketClient) ctx;
        
        if (this.username == null || this.username.isEmpty()) {
            logger.warn("Authentifizierungsversuch mit leerem oder null Username (Request-ID: {})", requestId);
            try {
                if (ctx instanceof SocketClient) {
                    S2CLoginFailedPacket response = new S2CLoginFailedPacket();
                    response.setRequestId(requestId);
                    ((SocketClient) ctx).send(response);
                }
            } catch (IOException e) {
                logger.error("Fehler beim Senden des Login-Failed-Pakets", e);
            }
            return;
        }

        Nutzer n = Server.findNutzerByUsername(this.username);

        logger.info("Neue Anmeldung: {} (Request-ID: {})", this.username, requestId);
        try {
            // Prüfe ob Account deaktiviert ist
            if (n != null && n.isDeactivated()) {
                logger.warn("Login-Versuch für deaktivierten Account: {} (Request-ID: {})", this.username, requestId);
                S2CLoginFailedPacket response = new S2CLoginFailedPacket();
                response.setRequestId(requestId);
                client.send(response);
                return;
            }
            
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
                        S2CLoginFailedPacket response = new S2CLoginFailedPacket();
                        response.setRequestId(requestId);
                        client.send(response);
                        return;
                    }
                    
                    serverClient.setNutzer(n);
                    n.setStatus(at.tgm.objects.NutzerStatus.ONLINE);
                    S2CLoginPacket response = new S2CLoginPacket(n);
                    response.setRequestId(requestId); // WICHTIG: Request-ID übernehmen
                    serverClient.send(response);

                    logger.info("Login erfolgreich für: {} (Typ: {}), Status auf ONLINE gesetzt", this.username, n.getClass().getSimpleName());
                } else {
                    logger.warn("Invalid client type für Authentifizierung: {}", client.getClass().getSimpleName());
                    S2CLoginFailedPacket response = new S2CLoginFailedPacket();
                    response.setRequestId(requestId);
                    client.send(response);
                }

            } else {
                logger.warn("Login fehlgeschlagen für: {} (Benutzer nicht gefunden oder falsches Passwort)", this.username);
                S2CLoginFailedPacket response = new S2CLoginFailedPacket();
                response.setRequestId(requestId);
                client.send(response);
            }
        } catch (IOException e) {
            logger.error("Fehler während der Authentifizierung für: {}", this.username, e);
            // IOException wird nicht weitergeworfen, da handle() keine IOException deklariert
        }
    }
}
