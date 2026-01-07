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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class C2SAuthenticationPacket implements Packet {

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
            System.err.println("WARN: NetworkContext ist null in C2SAuthenticationPacket");
            return;
        }

        if (this.username == null || this.username.isEmpty()) {
            System.err.println("WARN: Username ist null oder leer");
            try {
                if (ctx instanceof SocketClient) {
                    ((SocketClient) ctx).send(new S2CLoginFailedPacket());
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Senden des Login-Failed-Packets: " + e.getMessage());
            }
            return;
        }

        SocketClient client = (SocketClient) ctx;
        Nutzer n = Server.findNutzerByUsername(this.username);

        System.out.println("Neue Anmeldung: " + this.username);
        try {
            if (n != null && n.checkPassword(this.password)){

                // Client bleibt derselbe - nur State ändern
                if (client instanceof ServerClient) {
                    ServerClient serverClient = (ServerClient) client;
                    
                    // State basierend auf Nutzertyp setzen
                    if (n instanceof Schueler) {
                        serverClient.setState(new SchuelerState());
                    } else if (n instanceof Lehrer) {
                        serverClient.setState(new LehrerState());
                    } else {
                        System.err.println("WARN: Unbekannter Nutzertyp: " + n.getClass().getName());
                        client.send(new S2CLoginFailedPacket());
                        return;
                    }
                    
                    serverClient.setNutzer(n);
                    serverClient.send(new S2CLoginPacket(n));

                    System.out.println("Login erfolgreich für: " + this.username);
                } else {
                    client.send(new S2CLoginFailedPacket());
                    System.out.println("Invalid client type");
                }

            } else {
                client.send(new S2CLoginFailedPacket());
                System.out.println("Login fehlgeschlagen für: " + this.username);
            }
        } catch (IOException e) {
            System.err.println("Fehler während der Authentifizierung: " + e.getMessage());
            e.printStackTrace();
            // IOException wird nicht weitergeworfen, da handle() keine IOException deklariert
        }
    }
}
