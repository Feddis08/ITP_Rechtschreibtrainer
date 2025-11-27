package at.tgm.network.core;

import at.tgm.network.packets.C2SAuthenticationPacket;
import at.tgm.network.packets.C2SHelloPacket;
import at.tgm.network.packets.S2CLoginFailedPacket;
import at.tgm.network.packets.S2CLoginPacket;

public class NetworkSystem {

    //Muss von Client und Server ausgefuehrt werden. Alle Packete muessen hier registriert sein, sonst gibt's mismatch
    public static void init(){
        PacketRegistry.registerPacket(C2SHelloPacket.class); //0
        PacketRegistry.registerPacket(C2SAuthenticationPacket.class); //1
        PacketRegistry.registerPacket(S2CLoginPacket.class); //2
        PacketRegistry.registerPacket(S2CLoginFailedPacket.class); //3
    }
}
