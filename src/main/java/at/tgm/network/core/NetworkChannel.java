package at.tgm.network.core;

import at.tgm.objects.Distro;
import at.tgm.server.ServerNetworkController;

import java.io.*;
import java.net.Socket;

public class NetworkChannel {

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final NetworkContext context;


    public NetworkChannel(Socket socket, NetworkContext context) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
        this.context = context;

        System.out.println("New channel open.");
        listenAsync();
    }

    public void send(Packet packet) throws IOException {
        int id = PacketRegistry.getPacketId(packet.getClass());
        if (id < 0) throw new IOException("Packet not registered");

        out.writeInt(id);   // Packet ID
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream packetOut = new DataOutputStream(baos);

        packet.encode(packetOut);
        byte[] data = baos.toByteArray();

        out.writeInt(data.length);
        out.write(data);
        out.flush();
        System.out.println("Sent new packet: " + id);
    }

    private void listenAsync() {
        new Thread(() -> {
            try {
                while (true) {
                    int id = in.readInt();
                    int len = in.readInt();

                    System.out.println("Got new packet: " + id);

                    byte[] data = new byte[len];
                    in.readFully(data);

                    Packet packet = PacketRegistry.getPacketClass(id).getDeclaredConstructor().newInstance();
                    packet.decode(new DataInputStream(new ByteArrayInputStream(data)));

                    packet.handle(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(".Connection closed.");

                if (!(context instanceof SocketClient sc))
                    return;

                if (sc.getDistro().equals(Distro.SERVER)) {
                    ServerNetworkController.removeClient(sc);
                }else if(sc.getDistro().equals(Distro.CLIENT)){
                    try {
                        sc.getSocket().close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
        }).start();
    }
}
