package at.tgm.client;

import at.tgm.client.anmeldung.AnmeldeController;
import at.tgm.client.dashboard.DashboardFrame;
import at.tgm.client.ClientNetworkController;
import at.tgm.network.packets.C2SPlayQuiz;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;

import java.io.IOException;

public class GuiController {

    private AnmeldeController anmeldeController;
    private DashboardFrame dashboardFrame;
    private Nutzer currentNutzer;

    public void showLogin() {
        if (anmeldeController == null) {
            anmeldeController = new AnmeldeController();
            Client.ac = anmeldeController; // für bestehenden Code
        } else {
            anmeldeController.show();
        }
    }

    public void loginSuccessful(Nutzer nutzer) {
        this.currentNutzer = nutzer;

        if (anmeldeController != null) {
            anmeldeController.hide();
        }

        if (dashboardFrame == null) {
            dashboardFrame = new DashboardFrame(nutzer, this);
            Client.dashboardFrame = dashboardFrame; // falls irgendwo genutzt
        } else {
            dashboardFrame.setNutzer(nutzer);
            dashboardFrame.setVisible(true);
        }
    }

    public void loginFailed(String title, String msg) {
        if (anmeldeController != null) {
            anmeldeController.showError(title, msg);
        }
    }

    // Wird vom Dashboard aufgerufen, wenn der Benutzer im Menü "Quiz starten" klickt
    public void onQuizMenuClicked() {
        try {
            C2SPlayQuiz packet = new C2SPlayQuiz();
            ClientNetworkController.socketClient.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Wird vom Netzwerkcode aufgerufen, sobald die Fachbegriffe angekommen sind
    public void showQuiz(FachbegriffItem[] items) {
        if (dashboardFrame != null) {
            dashboardFrame.showQuiz(items);
        }
    }

    public void showProfile() {
        if (dashboardFrame != null) {
            dashboardFrame.showProfile();
        }
    }

    public Nutzer getCurrentNutzer() {
        return currentNutzer;
    }
}
