package at.tgm.network.core;

import at.tgm.objects.Distro;
import at.tgm.server.ServerNetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NetworkChannel {

    private static final Logger logger = LoggerFactory.getLogger(NetworkChannel.class);

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final NetworkContext context;
    
    // Request-ID Generator (thread-safe)
    private long nextRequestId = 1;
    private final Object requestIdLock = new Object();
    
    // Map für wartende Requests: Request-ID -> CompletableFuture<ResponsePacket>
    private final Map<Long, CompletableFuture<ResponsePacket>> pendingRequests = new ConcurrentHashMap<>();


    public NetworkChannel(Socket socket, NetworkContext context) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
        this.context = context;

        logger.info("Neuer NetworkChannel erstellt für: {}", socket.getRemoteSocketAddress());
        listenAsync();
    }

    public void send(Packet packet) throws IOException {
        if (socket.isClosed() || !socket.isConnected()) {
            logger.warn("Versuch, Paket über geschlossene/ungültige Verbindung zu senden: {}", packet.getClass().getSimpleName());
            throw new IOException("Socket is closed or not connected");
        }

        int id = PacketRegistry.getPacketId(packet.getClass());
        if (id < 0) {
            logger.error("Paket nicht registriert: {}", packet.getClass().getSimpleName());
            throw new IOException("Packet not registered");
        }

        out.writeInt(id);   // Packet ID
        
        // Wenn es ein RequestPacket ist, Request-ID generieren und setzen (nur wenn noch nicht gesetzt)
        if (packet instanceof RequestPacket requestPacket) {
            if (requestPacket.getRequestId() == 0) {
                // Request-ID noch nicht gesetzt - generiere neue
                synchronized (requestIdLock) {
                    long requestId = nextRequestId++;
                    requestPacket.setRequestId(requestId);
                    logger.debug("Request-Paket mit Request-ID {}: {}", requestId, packet.getClass().getSimpleName());
                }
            } else {
                // Request-ID bereits gesetzt (z.B. von sendAndWait())
                logger.debug("Request-Paket mit bereits gesetzter Request-ID {}: {}", 
                           requestPacket.getRequestId(), packet.getClass().getSimpleName());
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream packetOut = new DataOutputStream(baos);

        // Paket encodieren (inkl. Request-ID, wenn vorhanden)
        packet.encode(packetOut);
        
        byte[] data = baos.toByteArray();

        out.writeInt(data.length);
        out.write(data);
        out.flush();
        
        long requestId = 0;
        if (packet instanceof RequestPacket rp) requestId = rp.getRequestId();
        if (packet instanceof ResponsePacket rp) requestId = rp.getRequestId();
        
        logger.debug("Paket gesendet: ID={}, Typ={}, Größe={} bytes, Request-ID={}", 
                     id, packet.getClass().getSimpleName(), data.length, requestId != 0 ? requestId : "N/A");
    }
    
    /**
     * Sendet ein Request-Paket und wartet auf die entsprechende Response.
     * 
     * @param request Das Request-Paket (muss RequestPacket implementieren)
     * @param responseClass Die erwartete Response-Klasse
     * @param timeout Timeout-Wert
     * @param unit Timeout-Einheit
     * @return Die Response als Return-Wert
     * @param <R> Der Typ der Response
     * @throws IOException Bei Netzwerk-Fehlern
     * @throws TimeoutException Wenn keine Response innerhalb des Timeouts kommt
     * @throws InterruptedException Wenn der Thread unterbrochen wird
     */
    @SuppressWarnings("unchecked")
    public <R extends ResponsePacket> R sendAndWait(RequestPacket request, 
                                                     Class<R> responseClass,
                                                     long timeout, 
                                                     TimeUnit unit) 
            throws IOException, TimeoutException, InterruptedException {
        
        if (!(request instanceof RequestPacket)) {
            throw new IllegalArgumentException("Paket muss RequestPacket implementieren: " + request.getClass().getSimpleName());
        }
        
        // Request-ID generieren
        long requestId;
        synchronized (requestIdLock) {
            requestId = nextRequestId++;
        }
        request.setRequestId(requestId);
        
        // Future für die Response erstellen
        CompletableFuture<ResponsePacket> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        
        logger.debug("Sende Request mit ID {} und warte auf Response: {} (Future in Map gespeichert)", 
                     requestId, responseClass.getSimpleName());
        
        try {
            // Request senden (Request-ID ist bereits gesetzt, wird nicht überschrieben)
            try {
                send(request);
            } catch (IOException e) {
                // Bei IOException während send() Future sofort entfernen
                pendingRequests.remove(requestId);
                throw e;
            }
            
            // Verifiziere, dass die Request-ID noch korrekt ist
            if (request.getRequestId() != requestId) {
                logger.error("FEHLER: Request-ID wurde von {} auf {} geändert!", requestId, request.getRequestId());
                pendingRequests.remove(requestId);
                throw new IOException("Request-ID wurde während send() geändert");
            }
            
            // Auf Response warten
            ResponsePacket response = future.get(timeout, unit);
            
            if (!responseClass.isInstance(response)) {
                throw new IOException("Unerwartete Response-Klasse: " + response.getClass().getSimpleName() + 
                                     " (erwartet: " + responseClass.getSimpleName() + ")");
            }
            
            logger.debug("Response für Request-ID {} erhalten: {}", requestId, response.getClass().getSimpleName());
            return (R) response;
            
        } catch (java.util.concurrent.TimeoutException e) {
            // Timeout: Future entfernen und Exception werfen
            CompletableFuture<ResponsePacket> removed = pendingRequests.remove(requestId);
            if (removed != null && !removed.isDone()) {
                // Future wurde noch nicht aufgelöst - das ist OK bei Timeout
                logger.debug("Timeout für Request-ID {}, Future entfernt", requestId);
            }
            throw new TimeoutException("Timeout beim Warten auf Response für Request-ID " + requestId);
        } catch (java.util.concurrent.ExecutionException e) {
            // ExecutionException: Future wurde mit Exception aufgelöst
            pendingRequests.remove(requestId);
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException("Fehler beim Warten auf Response", cause);
        } catch (InterruptedException e) {
            // Thread wurde unterbrochen: Future entfernen und Exception weiterwerfen
            pendingRequests.remove(requestId);
            Thread.currentThread().interrupt(); // Interrupt-Flag wieder setzen
            throw e;
        } finally {
            // Future aus Map entfernen (falls noch vorhanden - sollte bereits entfernt sein)
            CompletableFuture<ResponsePacket> removed = pendingRequests.remove(requestId);
            if (removed != null) {
                logger.debug("Future für Request-ID {} im finally-Block entfernt", requestId);
            }
        }
    }

    private void listenAsync() {
        new Thread(() -> {
            try {
                logger.debug("Starte asynchrones Lauschen auf Pakete");
                while (true) {
                    int id = in.readInt();
                    int len = in.readInt();

                    Class<? extends Packet> packetClass = PacketRegistry.getPacketClass(id);
                    if (packetClass == null) {
                        logger.warn("Unbekanntes Paket mit ID {} empfangen, Größe: {} bytes", id, len);
                        // Überspringe unbekannte Pakete
                        in.skipBytes(len);
                        continue;
                    }

                    logger.debug("Paket empfangen: ID={}, Typ={}, Größe={} bytes", id, packetClass.getSimpleName(), len);

                    byte[] data = new byte[len];
                    in.readFully(data);

                    Packet packet = packetClass.getDeclaredConstructor().newInstance();
                    
                    // Paket dekodieren (Request-ID wird dabei von den Paketen selbst gelesen)
                    packet.decode(new DataInputStream(new ByteArrayInputStream(data)));

                    logger.debug("Paket dekodiert, verarbeite: {}", packetClass.getSimpleName());
                    
                    // Prüfen, ob es eine Response zu einem wartenden Request ist
                    if (packet instanceof ResponsePacket responsePacket) {
                        long requestId = responsePacket.getRequestId();
                        if (requestId != 0) {
                            logger.debug("Response-Paket mit Request-ID {} empfangen, suche wartenden Request...", requestId);
                            CompletableFuture<ResponsePacket> future = pendingRequests.remove(requestId);
                            if (future != null) {
                                // Prüfen, ob Future bereits aufgelöst wurde (z.B. durch Timeout)
                                if (future.isDone()) {
                                    logger.warn("Response für Request-ID {} empfangen, aber Future bereits aufgelöst (wahrscheinlich Timeout)", requestId);
                                    // Response wurde zu spät empfangen, normal verarbeiten
                                } else {
                                    logger.debug("Response für wartenden Request-ID {} gefunden, löse Future auf", requestId);
                                    boolean completed = future.complete(responsePacket);
                                    if (!completed) {
                                        logger.warn("Future für Request-ID {} konnte nicht aufgelöst werden (bereits aufgelöst?)", requestId);
                                    }
                                    // Response wurde an wartenden Request übergeben, nicht weiter verarbeiten
                                    continue;
                                }
                            } else {
                                logger.debug("Response mit Request-ID {} empfangen, aber kein wartender Request gefunden (wartende Requests: {}) - normale Verarbeitung", 
                                           requestId, pendingRequests.keySet());
                            }
                        }
                    }
                    
                    // Normale asynchrone Verarbeitung
                    packet.handle(context);
                }
            } catch (java.io.EOFException e) {
                // Verbindung wurde ordnungsgemäß geschlossen
                logger.info("Verbindung wurde geschlossen (EOF)");
                handleDisconnection(context);
            } catch (java.net.SocketException e) {
                // Socket-Fehler (Verbindung unterbrochen)
                logger.warn("Socket-Fehler: Verbindung unterbrochen - {}", e.getMessage());
                handleDisconnection(context);
            } catch (java.io.IOException e) {
                // Andere IO-Fehler
                logger.error("IO-Fehler im NetworkChannel", e);
                handleDisconnection(context);
            } catch (Exception e) {
                logger.error("Unerwarteter Fehler im NetworkChannel", e);
                handleDisconnection(context);
            }
        }).start();
    }

    private void handleDisconnection(NetworkContext context) {
        if (!(context instanceof SocketClient sc))
            return;

        // WICHTIG: Alle wartenden Requests aufräumen, da keine Response mehr kommen kann
        if (!pendingRequests.isEmpty()) {
            logger.warn("Verbindung verloren, räume {} wartende Requests auf", pendingRequests.size());
            IOException disconnectException = new IOException("Verbindung verloren während Warten auf Response");
            for (Map.Entry<Long, CompletableFuture<ResponsePacket>> entry : pendingRequests.entrySet()) {
                CompletableFuture<ResponsePacket> future = entry.getValue();
                if (!future.isDone()) {
                    future.completeExceptionally(disconnectException);
                }
            }
            pendingRequests.clear();
        }

        if (sc.getDistro().equals(Distro.SERVER)) {
            logger.info("Entferne Server-Client aufgrund von Verbindungsfehler");
            ServerNetworkController.removeClient(sc);
        } else if (sc.getDistro().equals(Distro.CLIENT)) {
            logger.info("Client-Verbindung verloren - benachrichtige Client");
            try {
                if (!sc.getSocket().isClosed()) {
                    sc.getSocket().close();
                }
            } catch (IOException ex) {
                logger.error("Fehler beim Schließen des Client-Sockets", ex);
            }
            
            // Benachrichtige den Client über den Verbindungsverlust
            try {
                at.tgm.client.Client.connectionLost();
            } catch (Exception ex) {
                logger.error("Fehler beim Benachrichtigen des Clients über Verbindungsverlust", ex);
            }
        }
    }
}
