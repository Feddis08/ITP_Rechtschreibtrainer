package at.tgm.network.core;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    private static final Map<Integer, Class<? extends Packet>> packets = new HashMap<>();
    private static int nextId = 0;

    public static void registerPacket(Class<? extends Packet> packetClass) {
        packets.put(nextId++, packetClass);
    }

    public static Class<? extends Packet> getPacketClass(int id) {
        return packets.get(id);
    }

    public static int getPacketId(Class<? extends Packet> packetClass) {
        return packets.entrySet()
                .stream()
                .filter(e -> e.getValue() == packetClass)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }
}
