package at.tgm.network.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Mappt Request-Paket-Typen zu ihren entsprechenden Response-Paket-Typen.
 * Wird verwendet, um zu bestimmen, welche Response zu einem Request gehört.
 */
public class RequestResponseMapper {
    
    private static final Map<Class<? extends RequestPacket>, Class<? extends ResponsePacket>> mappings = new HashMap<>();
    
    /**
     * Registriert ein Request-Response-Paar.
     * 
     * @param requestClass Die Klasse des Request-Pakets
     * @param responseClass Die Klasse des Response-Pakets
     */
    public static void registerPair(Class<? extends RequestPacket> requestClass, 
                                   Class<? extends ResponsePacket> responseClass) {
        mappings.put(requestClass, responseClass);
    }
    
    /**
     * Gibt die Response-Klasse für einen Request zurück.
     * 
     * @param requestClass Die Klasse des Request-Pakets
     * @return Die Response-Klasse oder null, wenn nicht registriert
     */
    public static Class<? extends ResponsePacket> getResponseClass(Class<? extends RequestPacket> requestClass) {
        return mappings.get(requestClass);
    }
    
    /**
     * Prüft, ob ein Paket eine Response zu einem Request ist.
     * 
     * @param responseClass Die Klasse des Response-Pakets
     * @param requestClass Die Klasse des Request-Pakets
     * @return true, wenn das Response-Paket zu dem Request gehört
     */
    public static boolean isResponseFor(Class<? extends Packet> responseClass, 
                                       Class<? extends RequestPacket> requestClass) {
        Class<? extends ResponsePacket> expectedResponse = mappings.get(requestClass);
        return expectedResponse != null && expectedResponse.isAssignableFrom(responseClass);
    }
}
