package at.tgm.server;

import at.tgm.network.core.NetworkSystem;
import at.tgm.network.core.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNetworkController {

    private static final Logger logger = LoggerFactory.getLogger(ServerNetworkController.class);

    public static SocketClient clients[];
    private static ServerDiscoveryService discoveryService;

    public static void start(int port) {
        NetworkSystem.init();

        // Starte Discovery-Service
        discoveryService = new ServerDiscoveryService(port);
        discoveryService.start();

        clients = new SocketClient[0];

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server läuft auf Port {}", port);

            while (true) {
                logger.debug("Warte auf eingehende Verbindung...");
                Socket client = serverSocket.accept();
                logger.info("Neuer Client verbunden: {}:{}", client.getInetAddress(), client.getPort());

                addClient(new ServerClient(client));
                logger.debug("Client wurde zur Client-Liste hinzugefügt");

            }

        } catch (IOException e) {
            logger.error("Fehler beim Starten des Servers auf Port {}", port, e);
            throw new RuntimeException(e);
        }
    }
    public static void removeClient(SocketClient client) {
        if (client == null) {
            logger.warn("Versuch, null-Client zu entfernen");
            return;
        }

        logger.info("Entferne Client: {}", client.getSocket().getRemoteSocketAddress());

        // Wenn der Client authentifiziert war, setze den Nutzer-Status auf OFFLINE
        if (client instanceof ServerClient serverClient) {
            at.tgm.objects.Nutzer nutzer = serverClient.getNutzer();
            if (nutzer != null) {
                nutzer.setStatus(at.tgm.objects.NutzerStatus.OFFLINE);
                logger.info("Status von Nutzer '{}' auf OFFLINE gesetzt", nutzer.getUsername());
            }
        }

        for (int i = 0; i < clients.length; i++) {
            SocketClient c = clients[i];

            if (c == null) continue;

            if (c.getSocket().getRemoteSocketAddress()
                    .equals(client.getSocket().getRemoteSocketAddress())) {

                try {
                    if (!c.getSocket().isClosed()) {
                        c.getSocket().close();
                        logger.debug("Client-Socket geschlossen");
                    }
                } catch (IOException e) {
                    logger.warn("Fehler beim Schließen des Client-Sockets", e);
                }

                clients[i] = null;
                logger.info("Client erfolgreich entfernt");
                return;
            }
        }
        logger.warn("Client nicht in der Liste gefunden: {}", client.getSocket().getRemoteSocketAddress());
    }


    public static void addClient(SocketClient client){
        if (client == null) {
            logger.error("Versuch, null-Client hinzuzufügen");
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        logger.debug("Füge neuen Client hinzu: {}", client.getSocket().getRemoteSocketAddress());

        // Suche nach freiem Platz im Array
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                clients[i] = client;
                logger.debug("Client an Index {} hinzugefügt", i);
                return;
            }
        }

        // Kein freier Platz gefunden - Array vergrößern
        logger.debug("Client-Array voll, vergrößere von {} auf {}", clients.length, clients.length + 1);
        SocketClient[] clientsNeu = new SocketClient[clients.length + 1];
        System.arraycopy(clients, 0, clientsNeu, 0, clients.length);
        clientsNeu[clients.length] = client;
        clients = clientsNeu;
        logger.debug("Client erfolgreich hinzugefügt (Array vergrößert)");
    }




}
