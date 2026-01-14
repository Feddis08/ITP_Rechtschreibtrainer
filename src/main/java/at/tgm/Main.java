package at.tgm;

import at.tgm.client.Client;
import at.tgm.client.ServerDiscoveryLauncher;
import at.tgm.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final int DEFAULT_PORT = 5123;
    private static final String DEFAULT_HOST = "localhost";

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String mode = args[0].toLowerCase();

        switch (mode) {
            case "server":
                startServer(args);
                break;
            case "client":
                startClient(args);
                break;
            default:
                logger.error("Unbekannter Modus: {}. Erwartet 'server' oder 'client'", mode);
                printUsage();
                System.exit(1);
        }
    }

    private static void startServer(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
                if (port < 1 || port > 65535) {
                    logger.error("Ungültiger Port: {}. Port muss zwischen 1 und 65535 liegen", port);
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                logger.error("Ungültiger Port: '{}'. Port muss eine Zahl sein", args[1]);
                System.exit(1);
            }
        }

        logger.info("Starte Server auf Port {}", port);
        Server.main(new String[]{String.valueOf(port)});
    }

    private static void startClient(String[] args) {
        // Wenn Host und Port explizit angegeben, verwende direkte Verbindung
        // Ansonsten starte Launcher mit Discovery
        if (args.length > 1) {
            String host = DEFAULT_HOST;
            int port = DEFAULT_PORT;

            // Wenn das zweite Argument einen Doppelpunkt enthält, ist es "host:port"
            if (args[1].contains(":")) {
                String[] parts = args[1].split(":", 2);
                host = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                    if (port < 1 || port > 65535) {
                        logger.error("Ungültiger Port: {}. Port muss zwischen 1 und 65535 liegen", port);
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    logger.error("Ungültiger Port: '{}'. Port muss eine Zahl sein", parts[1]);
                    System.exit(1);
                }
            } else {
                // Nur Host angegeben
                host = args[1];
                if (args.length > 2) {
                    try {
                        port = Integer.parseInt(args[2]);
                        if (port < 1 || port > 65535) {
                            logger.error("Ungültiger Port: {}. Port muss zwischen 1 und 65535 liegen", port);
                            System.exit(1);
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Ungültiger Port: '{}'. Port muss eine Zahl sein", args[2]);
                        System.exit(1);
                    }
                }
            }

            logger.info("Starte Client und verbinde direkt mit {}:{}", host, port);
            try {
                Client.main(new String[]{host, String.valueOf(port)});
            } catch (IOException e) {
                logger.error("Fehler beim Starten des Clients", e);
                System.exit(1);
            }
        } else {
            // Keine Parameter - starte Launcher mit Discovery
            logger.info("Starte Client-Launcher mit Server-Discovery");
            ServerDiscoveryLauncher.discoverAndConnect();
        }
    }

    private static void printUsage() {
        System.out.println("Verwendung:");
        System.out.println("  Server starten:");
        System.out.println("    java -jar itp-app.jar server [PORT]");
        System.out.println("    Beispiel: java -jar itp-app.jar server 5123");
        System.out.println();
        System.out.println("  Client starten:");
        System.out.println("    java -jar itp-app.jar client [HOST[:PORT]] [PORT]");
        System.out.println("    Beispiele:");
        System.out.println("      java -jar itp-app.jar client");
        System.out.println("      java -jar itp-app.jar client 192.168.1.100");
        System.out.println("      java -jar itp-app.jar client localhost:5123");
        System.out.println("      java -jar itp-app.jar client 192.168.1.100 5123");
        System.out.println();
        System.out.println("  Standardwerte:");
        System.out.println("    Server Port: 5123");
        System.out.println("    Client Host: localhost");
        System.out.println("    Client Port: 5123");
    }
}
