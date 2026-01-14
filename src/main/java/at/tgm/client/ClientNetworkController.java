package at.tgm.client;

import at.tgm.network.core.NetworkSystem;
import at.tgm.objects.Distro;
import at.tgm.network.core.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class ClientNetworkController {

    private static final Logger logger = LoggerFactory.getLogger(ClientNetworkController.class);

    public static SocketClient socketClient;
    
    /**
     * Verbindet zum Server mit Standardwerten (localhost:5123).
     */
    public static void connect() {
        connect("localhost", 5123);
    }
    
    /**
     * Verbindet zum Server mit der angegebenen Adresse und Port.
     * @param host Die Server-Adresse
     * @param port Der Server-Port
     */
    public static void connect(String host, int port) {
        logger.info("Initialisiere Netzwerk-System");
        NetworkSystem.init();

        logger.info("Verbinde mit Server {}:{}", host, port);
        try {
            Socket socket = new Socket(host, port);
            logger.info("Erfolgreich mit Server verbunden: {}:{}", socket.getRemoteSocketAddress(), socket.getPort());
            socketClient = new SocketClient(socket, Distro.CLIENT);
            logger.debug("SocketClient erstellt");

        } catch (IOException e) {
            logger.error("Fehler beim Verbinden mit Server {}:{}", host, port, e);
            throw new RuntimeException(e);
        }
    }

}
