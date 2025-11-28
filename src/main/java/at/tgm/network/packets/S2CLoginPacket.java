package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.Nutzer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CLoginPacket implements Packet {

    private Nutzer n;

    //TODO: some start information about the user for client display
    public S2CLoginPacket(){

    }
    public S2CLoginPacket(Nutzer nutzer){
        this.n = nutzer;

        n.setLastLoginTimestamp(System.currentTimeMillis());
    }
    @Override
    public void encode(DataOutputStream out) throws IOException {
        n.encode(out, false); // Passwort NICHT übertragen
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        this.n = Nutzer.decodeNutzer(in, false);
    }

    @Override
    public void handle(NetworkContext ctx) {

        System.out.println(n.getLastName());

        Client.login(n);
    }
}
