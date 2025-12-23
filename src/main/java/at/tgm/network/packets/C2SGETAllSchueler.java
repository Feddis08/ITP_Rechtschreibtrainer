package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.network.core.SocketClient;
import at.tgm.objects.Lehrer;
import at.tgm.server.ServerLehrerClient;
import at.tgm.server.ServerSchuelerClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C2SGETAllSchueler implements Packet {
    @Override
    public void encode(DataOutputStream out) throws IOException {

    }

    @Override
    public void decode(DataInputStream in) throws IOException {

    }

    @Override
    public void handle(NetworkContext ctx) {

         SocketClient sc = ((SocketClient) ctx);

         if (sc.getNutzer() instanceof Lehrer){
             try {
                 ((ServerLehrerClient) ctx).postAllSchueler();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }

    }
}
