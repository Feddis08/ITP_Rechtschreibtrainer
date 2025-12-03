package at.tgm.client;

import at.tgm.client.anmeldung.AnmeldeController;
import at.tgm.client.dashboard.DashboardFrame;
import at.tgm.client.profile.ProfileFrame;
import at.tgm.client.quiz.QuizFrame;
import at.tgm.network.packets.C2SAuthenticationPacket;
import at.tgm.network.packets.C2SHelloPacket;
import at.tgm.objects.Nutzer;

import java.io.IOException;

public class Client {

    public static AnmeldeController ac;
    public static DashboardFrame dashboardFrame;

    public static QuizFrame quizFrame;

    public static Nutzer nutzer;
    public static void main(String[] args) throws IOException {

        ClientNetworkController.connect();

        ac = new AnmeldeController();

        ClientNetworkController.socketClient.send(new C2SHelloPacket("MAC_OS"));

    }
    public static void failedLogin(){
        System.out.println("Failed To Log in");

        ac.ff = new FehlerFrame("Failed Login", "Check Username or Password!");
    }

    public static void login(Nutzer n){
        Client.nutzer = n;
        ac.frame.setVisible(false);

        dashboardFrame = new DashboardFrame(n);

    }
}
