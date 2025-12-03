package at.tgm.network.core;

import at.tgm.network.packets.*;

public class NetworkSystem {

    //Muss von Client und Server ausgefuehrt werden. Alle Packete muessen hier registriert sein, sonst gibt's mismatch
    public static void init(){
        PacketRegistry.registerPacket(C2SHelloPacket.class); //0
        PacketRegistry.registerPacket(C2SAuthenticationPacket.class); //1
        PacketRegistry.registerPacket(S2CLoginPacket.class); //2
        PacketRegistry.registerPacket(S2CLoginFailedPacket.class); //3
        PacketRegistry.registerPacket(C2SPlayQuiz.class); //4
        PacketRegistry.registerPacket(S2CPOSTQuiz.class); //5
        PacketRegistry.registerPacket(C2SPOSTQuizResults.class); //6
        PacketRegistry.registerPacket(S2CResultOfQuiz.class); //7
    }
}
