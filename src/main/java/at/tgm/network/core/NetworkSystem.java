package at.tgm.network.core;

import at.tgm.network.packets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkSystem {

    private static final Logger logger = LoggerFactory.getLogger(NetworkSystem.class);

    //Muss von Client und Server ausgefuehrt werden. Alle Packete muessen hier registriert sein, sonst gibt's mismatch
    public static void init(){
        logger.info("Initialisiere NetworkSystem und registriere Pakete");
        PacketRegistry.registerPacket(C2SHelloPacket.class); //0
        logger.debug("Paket registriert: C2SHelloPacket (ID: 0)");
        PacketRegistry.registerPacket(C2SAuthenticationPacket.class); //1
        logger.debug("Paket registriert: C2SAuthenticationPacket (ID: 1)");
        PacketRegistry.registerPacket(S2CLoginPacket.class); //2
        logger.debug("Paket registriert: S2CLoginPacket (ID: 2)");
        PacketRegistry.registerPacket(S2CLoginFailedPacket.class); //3
        logger.debug("Paket registriert: S2CLoginFailedPacket (ID: 3)");
        PacketRegistry.registerPacket(C2SINITQuiz.class); //4
        logger.debug("Paket registriert: C2SINITQuiz (ID: 4)");
        PacketRegistry.registerPacket(S2CPOSTQuiz.class); //5
        logger.debug("Paket registriert: S2CPOSTQuiz (ID: 5)");
        PacketRegistry.registerPacket(C2SPOSTQuizResults.class); //6
        logger.debug("Paket registriert: C2SPOSTQuizResults (ID: 6)");
        PacketRegistry.registerPacket(S2CResultOfQuiz.class); //7
        logger.debug("Paket registriert: S2CResultOfQuiz (ID: 7)");
        // Packet ID 8 war ein Duplikat von S2CPOSTQuiz - entfernt
        PacketRegistry.registerPacket(C2SGETStats.class); //8
        logger.debug("Paket registriert: C2SGETStats (ID: 8)");
        PacketRegistry.registerPacket(S2CPOSTStats.class); //9
        logger.debug("Paket registriert: S2CPOSTStats (ID: 9)");
        PacketRegistry.registerPacket(C2SGETAllSchueler.class); //10
        logger.debug("Paket registriert: C2SGETAllSchueler (ID: 10)");
        PacketRegistry.registerPacket(S2CPOSTAllSchueler.class); //11
        logger.debug("Paket registriert: S2CPOSTAllSchueler (ID: 11)");
        PacketRegistry.registerPacket(C2SPOSTSchuelerVorschlag.class); //12
        logger.debug("Paket registriert: C2SPOSTSchuelerVorschlag (ID: 12)");
        PacketRegistry.registerPacket(S2CResponseSchuelerVorschlag.class); //13
        logger.debug("Paket registriert: S2CResponseSchuelerVorschlag (ID: 13)");
        PacketRegistry.registerPacket(C2SGETSchuelerStats.class); //14
        logger.debug("Paket registriert: C2SGETSchuelerStats (ID: 14)");
        PacketRegistry.registerPacket(C2SToggleSchuelerStatus.class); //15
        logger.debug("Paket registriert: C2SToggleSchuelerStatus (ID: 15)");
        PacketRegistry.registerPacket(C2SDeleteSchueler.class); //16
        logger.debug("Paket registriert: C2SDeleteSchueler (ID: 16)");
        PacketRegistry.registerPacket(S2CResponseSchuelerOperation.class); //17
        logger.debug("Paket registriert: S2CResponseSchuelerOperation (ID: 17)");
        PacketRegistry.registerPacket(C2SSetSchuelerNote.class); //18
        logger.debug("Paket registriert: C2SSetSchuelerNote (ID: 18)");
        PacketRegistry.registerPacket(C2SGETOwnAccount.class); //19
        logger.debug("Paket registriert: C2SGETOwnAccount (ID: 19)");
        PacketRegistry.registerPacket(S2CPOSTOwnAccount.class); //20
        logger.debug("Paket registriert: S2CPOSTOwnAccount (ID: 20)");
        logger.info("NetworkSystem erfolgreich initialisiert, {} Pakete registriert", 21);
    }
}
