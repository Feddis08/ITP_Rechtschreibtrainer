package at.tgm.network.core;

import at.tgm.objects.Distro;
import at.tgm.server.ServerNetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class NetworkChannel {

    private static final Logger logger = LoggerFactory.getLogger(NetworkChannel.class);

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final NetworkContext context;


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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream packetOut = new DataOutputStream(baos);

        packet.encode(packetOut);
        byte[] data = baos.toByteArray();

        out.writeInt(data.length);
        out.write(data);
        out.flush();
        logger.debug("Paket gesendet: ID={}, Typ={}, Größe={} bytes", id, packet.getClass().getSimpleName(), data.length);
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
                    packet.decode(new DataInputStream(new ByteArrayInputStream(data)));

                    logger.debug("Paket dekodiert, verarbeite: {}", packetClass.getSimpleName());
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
