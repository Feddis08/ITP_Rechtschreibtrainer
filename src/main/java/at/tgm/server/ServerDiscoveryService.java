package at.tgm.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UDP-Service für Server-Discovery.
 * Hört auf Discovery-Requests und antwortet mit Server-Adresse und Port.
 */
public class ServerDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(ServerDiscoveryService.class);
    
    private static final int DISCOVERY_PORT = 5124; // Separate Port für Discovery
    private static final String DISCOVERY_REQUEST = "ITP_SERVER_DISCOVERY_REQUEST";
    private static final String DISCOVERY_RESPONSE_PREFIX = "ITP_SERVER_DISCOVERY_RESPONSE:";
    
    private final int serverPort;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread discoveryThread;
    
    public ServerDiscoveryService(int serverPort) {
        this.serverPort = serverPort;
    }
    
    /**
     * Startet den Discovery-Service in einem separaten Thread.
     */
    public void start() {
        if (running.get()) {
            logger.warn("Discovery-Service läuft bereits");
            return;
        }
        
        running.set(true);
        discoveryThread = new Thread(this::run, "ServerDiscoveryService");
        discoveryThread.setDaemon(true);
        discoveryThread.start();
        logger.info("Server-Discovery-Service gestartet auf Port {}", DISCOVERY_PORT);
    }
    
    /**
     * Stoppt den Discovery-Service.
     */
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        if (discoveryThread != null) {
            discoveryThread.interrupt();
        }
        logger.info("Server-Discovery-Service gestoppt");
    }
    
    private void run() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            socket.setBroadcast(true);
            socket.setSoTimeout(1000); // Timeout für receive, damit wir regelmäßig prüfen können ob wir stoppen sollen
            logger.info("Discovery-Socket erstellt auf Port {}", DISCOVERY_PORT);
            
            byte[] buffer = new byte[1024];
            
            while (running.get()) {
                try {
                    // Warte auf Discovery-Request
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    socket.receive(request);
                    
                    String requestStr = new String(request.getData(), 0, request.getLength());
                    logger.debug("Discovery-Request erhalten von {}: {}", 
                               request.getAddress(), requestStr);
                    
                    if (DISCOVERY_REQUEST.equals(requestStr)) {
                        // Erstelle Response mit Server-Adresse und Port
                        // Verwende die Adresse des empfangenen Requests, um die lokale Adresse zu ermitteln
                        String serverAddress = getServerAddress(request.getAddress());
                        String responseData = DISCOVERY_RESPONSE_PREFIX + serverAddress + ":" + serverPort;
                        
                        byte[] responseBytes = responseData.getBytes();
                        DatagramPacket response = new DatagramPacket(
                            responseBytes,
                            responseBytes.length,
                            request.getAddress(),
                            request.getPort()
                        );
                        
                        socket.send(response);
                        logger.debug("Discovery-Response gesendet an {}: {}", 
                                   request.getAddress(), responseData);
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout ist normal, wenn keine Requests kommen - einfach weiter machen
                    continue;
                } catch (IOException e) {
                    if (running.get()) {
                        logger.error("Fehler beim Empfangen/Senden von Discovery-Paketen", e);
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("Fehler beim Erstellen des Discovery-Sockets", e);
        }
    }
    
    /**
     * Ermittelt die Server-Adresse basierend auf der Adresse des Clients.
     * Versucht die lokale Adresse zu finden, die mit dem Client kommunizieren kann.
     */
    private String getServerAddress(InetAddress clientAddress) {
        try {
            // Versuche die lokale Adresse zu finden, die mit dem Client kommunizieren kann
            // Dazu verbinden wir kurz mit dem Client (ohne tatsächlich zu verbinden)
            try (DatagramSocket testSocket = new DatagramSocket()) {
                testSocket.connect(clientAddress, 1);
                InetAddress localAddress = testSocket.getLocalAddress();
                
                // Wenn es eine Loopback-Adresse ist, verwende localhost
                if (localAddress.isLoopbackAddress() || clientAddress.isLoopbackAddress()) {
                    return "localhost";
                }
                
                // Wenn Client und Server im gleichen Netzwerk sind, verwende die lokale Adresse
                // Ansonsten versuche die Adresse des Interfaces zu finden, das mit dem Client kommunizieren kann
                return localAddress.getHostAddress();
            }
        } catch (Exception e) {
            logger.debug("Fehler beim Ermitteln der Server-Adresse, verwende localhost", e);
            // Fallback: Wenn Client localhost ist, verwende localhost
            if (clientAddress.isLoopbackAddress()) {
                return "localhost";
            }
            // Ansonsten versuche die erste nicht-Loopback Adresse zu finden
            try {
                java.util.Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue;
                    }
                    java.util.List<java.net.InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
                    for (java.net.InterfaceAddress addr : addresses) {
                        InetAddress inetAddr = addr.getAddress();
                        if (!inetAddr.isLoopbackAddress() && inetAddr instanceof java.net.Inet4Address) {
                            return inetAddr.getHostAddress();
                        }
                    }
                }
            } catch (Exception ex) {
                logger.debug("Fehler beim Finden der Netzwerk-Adresse", ex);
            }
            return "localhost";
        }
    }
}
