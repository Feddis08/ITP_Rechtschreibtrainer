package at.tgm.network.core;

/**
 * Marker-Interface für Response-Pakete, die zu einem Request gehören.
 * Pakete, die dieses Interface implementieren, enthalten eine Request-ID.
 */
public interface ResponsePacket extends Packet {
    /**
     * Setzt die Request-ID für dieses Paket.
     * Wird intern vom NetworkChannel verwendet.
     */
    void setRequestId(long requestId);
    
    /**
     * Gibt die Request-ID zurück.
     */
    long getRequestId();
}
