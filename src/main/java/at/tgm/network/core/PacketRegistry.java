package at.tgm.network.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    private static final Logger logger = LoggerFactory.getLogger(PacketRegistry.class);

    private static final Map<Integer, Class<? extends Packet>> packets = new HashMap<>();
    private static int nextId = 0;

    public static void registerPacket(Class<? extends Packet> packetClass) {
        int id = nextId++;
        packets.put(id, packetClass);
        logger.trace("Paket registriert: {} mit ID {}", packetClass.getSimpleName(), id);
    }

    public static Class<? extends Packet> getPacketClass(int id) {
        Class<? extends Packet> packetClass = packets.get(id);
        if (packetClass == null) {
            logger.warn("Paket mit ID {} nicht gefunden", id);
        }
        return packetClass;
    }

    public static int getPacketId(Class<? extends Packet> packetClass) {
        int id = packets.entrySet()
                .stream()
                .filter(e -> e.getValue() == packetClass)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
        if (id < 0) {
            logger.warn("Paket-ID für {} nicht gefunden", packetClass.getSimpleName());
        }
        return id;
    }
}
