package at.tgm.network;

public class NetworkSystem {

    //Muss von Client und Server ausgefuehrt werden. Alle Packete muessen hier registriert sein, sonst gibt's mismatch
    public static void init(){
        PacketRegistry.registerPacket(C2SHelloPacket.class);
    }
}
