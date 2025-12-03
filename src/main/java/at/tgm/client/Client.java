package at.tgm.client;

import at.tgm.client.anmeldung.AnmeldeController;
import at.tgm.client.dashboard.DashboardFrame;
import at.tgm.network.packets.C2SGETStats;
import at.tgm.network.packets.C2SHelloPacket;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;

import java.io.IOException;

public class Client {

    public static GuiController GUI = new GuiController();

    // für bestehenden Code weiterhin verfügbar:
    public static AnmeldeController ac;
    public static DashboardFrame dashboardFrame;
    public static Nutzer nutzer;

    public static void main(String[] args) throws IOException {

        ClientNetworkController.connect();

        // Login-GUI starten
        GUI.showLogin();



        // Hallo-Paket an den Server
        ClientNetworkController.socketClient.send(new C2SHelloPacket("MAC_OS"));
    }

    // Wird vom Netzwerkcode aufgerufen
    public static void failedLogin() {
        GUI.loginFailed("Failed Login", "Check Username or Password!");
    }

    // Wird vom Netzwerkcode aufgerufen
    public static void login(Nutzer n) {
        Client.nutzer = n;
        GUI.loginSuccessful(n);
    }

    // Wird vom Netzwerkcode aufgerufen, sobald Fachbegriffe fürs Quiz da sind
    public static void startQuiz(FachbegriffItem[] items) {
        GUI.showQuiz(items);
    }
}
