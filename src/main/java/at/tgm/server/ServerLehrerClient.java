package at.tgm.server;

import at.tgm.network.core.Packet;
import at.tgm.network.core.SocketClient;
import at.tgm.network.packets.S2CPOSTAllSchueler;
import at.tgm.objects.Distro;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Schueler;

import java.io.IOException;
import java.net.Socket;

public class ServerLehrerClient extends ServerClient {
    public ServerLehrerClient(Socket socket) throws IOException {
        super(socket);
    }

    public void postAllSchueler() throws IOException {
        Schueler[] s = new Schueler[Server.nutzers.length];
        int i = 0;
        for (Nutzer nutzer : Server.nutzers){
            if (nutzer instanceof Schueler)
                s[i] = (Schueler) nutzer;

            i++;
        }

        S2CPOSTAllSchueler packet = new S2CPOSTAllSchueler(s);
        send(packet);
    }

}
