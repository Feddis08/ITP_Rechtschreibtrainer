package at.tgm.network.packets;

import at.tgm.client.Client;
import at.tgm.network.core.NetworkContext;
import at.tgm.network.core.Packet;
import at.tgm.objects.Nutzer;
import at.tgm.objects.SendableObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S2CLoginPacket implements Packet {

    private Nutzer n;

    public S2CLoginPacket() {}

    public S2CLoginPacket(Nutzer nutzer) {
        this.n = nutzer;
    }

    @Override
    public void encode(DataOutputStream out) throws IOException {
        n.encode(out);  // ALLES automatisch
    }

    @Override
    public void decode(DataInputStream in) throws IOException {
        this.n = SendableObject.decode(in); // richtige Subklasse automatisch!
    }

    @Override
    public void handle(NetworkContext ctx) {

        Client.login(n);
    }
}
