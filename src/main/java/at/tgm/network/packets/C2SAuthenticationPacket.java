package at.tgm.network.packets;

import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.Nutzer;
import at.tgm.server.Server;
import at.tgm.server.SocketClient;

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
        SocketClient client = (SocketClient) ctx;

        Nutzer n = Server.findNutzerByUsername(this.username);

        System.out.println("Neue Anmeldung: " + this.username);
        try {
            if (n != null && n.checkPassword(this.password)){
               client.setNutzer(n);
               client.send(new S2CLoginPacket(n));
                System.out.println("Login Packet");
            }else{
               client.send(new S2CLoginFailedPacket());
                System.out.println("Failed Packet");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
