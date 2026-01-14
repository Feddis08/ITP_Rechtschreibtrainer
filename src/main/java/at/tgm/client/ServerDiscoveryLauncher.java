package at.tgm.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class ServerDiscoveryLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ServerDiscoveryLauncher.class);
    
    private static final int DISCOVERY_PORT = 5124; // Separate Port für Discovery
    private static final int DISCOVERY_TIMEOUT_MS = 3000; // 3 Sekunden Timeout
    private static final String DISCOVERY_REQUEST = "ITP_SERVER_DISCOVERY_REQUEST";
    private static final String DISCOVERY_RESPONSE_PREFIX = "ITP_SERVER_DISCOVERY_RESPONSE:";
    
    public static class ServerInfo {
        private final String address;
        private final int port;
        
        public ServerInfo(String address, int port) {
            this.address = address;
            this.port = port;
        }
        
        public String getAddress() {
            return address;
        }
        
        public int getPort() {
            return port;
        }
        
        @Override
        public String toString() {
            return address + ":" + port;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ServerInfo that = (ServerInfo) o;
            return port == that.port && Objects.equals(address, that.address);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(address, port);
        }
    }
    
    /**
     * Startet die Server-Discovery und zeigt das entsprechende UI.
     * Nach erfolgreicher Verbindung wird der Client gestartet.
     */
    public static void discoverAndConnect() {
        logger.info("Starte Server-Discovery...");
        
        SwingUtilities.invokeLater(() -> {
            ServerDiscoveryLauncherFrame frame = new ServerDiscoveryLauncherFrame();
            frame.setVisible(true);
            
            // Discovery in separatem Thread durchführen
            new Thread(() -> {
                List<ServerInfo> servers = discoverServers();
                
                SwingUtilities.invokeLater(() -> {
                    if (servers.isEmpty()) {
                        // Keine Server gefunden - zeige Eingabefeld
                        logger.warn("Keine Server gefunden, zeige manuelle Eingabe");
                        frame.showManualInput();
                    } else if (servers.size() == 1) {
                        // Ein Server gefunden - verbinde direkt
                        logger.info("Ein Server gefunden: {}, verbinde direkt", servers.get(0));
                        frame.dispose();
                        connectToServer(servers.get(0).getAddress(), servers.get(0).getPort());
                    } else {
                        // Mehrere Server gefunden - zeige Liste
                        logger.info("{} Server gefunden, zeige Auswahl", servers.size());
                        frame.showServerList(servers);
                    }
                });
            }).start();
        });
    }
    
    /**
     * Sendet UDP-Broadcasts auf allen Netzwerk-Interfaces und sammelt Server-Antworten.
     */
    public static List<ServerInfo> discoverServers() {
        Set<ServerInfo> discoveredServers = new HashSet<>();
        
        try {
            // Erstelle DatagramSocket für Broadcasts
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(DISCOVERY_TIMEOUT_MS);
            
            // Hole alle Netzwerk-Interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            List<InetAddress> broadcastAddresses = new ArrayList<>();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Überspringe Loopback und inaktive Interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                // Hole Broadcast-Adressen für dieses Interface
                List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress addr : addresses) {
                    InetAddress broadcast = addr.getBroadcast();
                    if (broadcast != null) {
                        broadcastAddresses.add(broadcast);
                        logger.debug("Broadcast-Adresse gefunden: {} auf Interface {}", 
                                    broadcast, networkInterface.getName());
                    }
                }
            }
            
            // Wenn keine Broadcast-Adressen gefunden, verwende 255.255.255.255
            if (broadcastAddresses.isEmpty()) {
                broadcastAddresses.add(InetAddress.getByName("255.255.255.255"));
                logger.debug("Verwende Standard-Broadcast-Adresse: 255.255.255.255");
            }
            
            // Sende Discovery-Request an alle Broadcast-Adressen
            byte[] requestData = DISCOVERY_REQUEST.getBytes();
            for (InetAddress broadcastAddr : broadcastAddresses) {
                try {
                    DatagramPacket request = new DatagramPacket(
                        requestData,
                        requestData.length,
                        broadcastAddr,
                        DISCOVERY_PORT
                    );
                    socket.send(request);
                    logger.debug("Discovery-Request gesendet an {}", broadcastAddr);
                } catch (IOException e) {
                    logger.warn("Fehler beim Senden an {}: {}", broadcastAddr, e.getMessage());
                }
            }
            
            // Sammle Antworten
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < DISCOVERY_TIMEOUT_MS) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                    socket.receive(response);
                    
                    String responseStr = new String(response.getData(), 0, response.getLength());
                    logger.debug("Discovery-Response erhalten: {} von {}", 
                               responseStr, response.getAddress());
                    
                    if (responseStr.startsWith(DISCOVERY_RESPONSE_PREFIX)) {
                        // Parse Response: "ITP_SERVER_DISCOVERY_RESPONSE:host:port"
                        String data = responseStr.substring(DISCOVERY_RESPONSE_PREFIX.length());
                        String[] parts = data.split(":");
                        if (parts.length == 2) {
                            try {
                                String host = parts[0];
                                int port = Integer.parseInt(parts[1]);
                                ServerInfo serverInfo = new ServerInfo(host, port);
                                discoveredServers.add(serverInfo);
                                logger.info("Server gefunden: {}", serverInfo);
                            } catch (NumberFormatException e) {
                                logger.warn("Ungültiger Port in Response: {}", data);
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout ist normal, wenn keine weiteren Antworten kommen
                    break;
                } catch (IOException e) {
                    logger.warn("Fehler beim Empfangen von Discovery-Response: {}", e.getMessage());
                }
            }
            
            socket.close();
            
        } catch (SocketException e) {
            logger.error("Fehler beim Erstellen des Discovery-Sockets", e);
        } catch (IOException e) {
            logger.error("Fehler bei der Server-Discovery", e);
        }
        
        return new ArrayList<>(discoveredServers);
    }
    
    /**
     * Verbindet zum Server und startet den Client.
     */
    public static void connectToServer(String host, int port) {
        logger.info("Verbinde mit Server {}:{}", host, port);
        
        try {
            // Verbinde zum Server
            ClientNetworkController.connect(host, port);
            logger.info("Erfolgreich mit Server verbunden");
            
            // Starte Client (ohne Parameter, da Verbindung bereits hergestellt)
            SwingUtilities.invokeLater(() -> {
                try {
                    Client.startAfterConnection();
                } catch (IOException e) {
                    logger.error("Fehler beim Starten des Clients", e);
                    JOptionPane.showMessageDialog(
                        null,
                        "Fehler beim Starten des Clients: " + e.getMessage(),
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            });
        } catch (Exception e) {
            logger.error("Fehler beim Verbinden mit Server {}:{}", host, port, e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    null,
                    "Fehler beim Verbinden mit Server:\n" + e.getMessage(),
                    "Verbindungsfehler",
                    JOptionPane.ERROR_MESSAGE
                );
                // Zeige Launcher erneut
                discoverAndConnect();
            });
        }
    }
}
