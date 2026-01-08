package at.tgm.network.core;

/**
 * Marker-Interface für Request-Pakete, die eine Response erwarten.
 * Pakete, die dieses Interface implementieren, können mit sendAndWait() verwendet werden.
 */
public interface RequestPacket extends Packet {
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
