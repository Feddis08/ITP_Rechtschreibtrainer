package at.tgm.client;

import at.tgm.client.ClientNetworkController;
import at.tgm.client.anmeldung.AnmeldeController;
import at.tgm.client.dashboard.DashboardFrame;
import at.tgm.network.packets.C2SGETAllSchueler;
import at.tgm.network.packets.C2SGETOwnAccount;
import at.tgm.network.packets.C2SGETSchuelerStats;
import at.tgm.network.packets.C2SINITQuiz;
import at.tgm.network.packets.S2CPOSTAllSchueler;
import at.tgm.network.packets.S2CPOSTOwnAccount;
import at.tgm.network.packets.S2CPOSTStats;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GuiController {

    private static final Logger logger = LoggerFactory.getLogger(GuiController.class);

    private AnmeldeController anmeldeController;
    private DashboardFrame dashboardFrame;
    private Nutzer currentNutzer;

    public void showLogin() {
        logger.debug("Zeige Login-Fenster");
        if (anmeldeController == null) {
            anmeldeController = new AnmeldeController();
            Client.ac = anmeldeController; // für bestehenden Code
            logger.debug("Neuer AnmeldeController erstellt");
        } else {
            anmeldeController.show();
            logger.debug("Bestehender AnmeldeController angezeigt");
        }
    }

    public void loginSuccessful(Nutzer nutzer) {
        logger.info("Login erfolgreich, zeige Dashboard für: {}", nutzer != null ? nutzer.getUsername() : "unknown");
        this.currentNutzer = nutzer;

        if (anmeldeController != null) {
            anmeldeController.hide();
            logger.debug("Login-Fenster verborgen");
        }

        if (dashboardFrame == null) {
            dashboardFrame = new DashboardFrame(nutzer, this);
            Client.dashboardFrame = dashboardFrame; // falls irgendwo genutzt
            logger.debug("Neues Dashboard-Fenster erstellt");
        } else {
            dashboardFrame.setNutzer(nutzer);
            dashboardFrame.setVisible(true);
            logger.debug("Bestehendes Dashboard-Fenster aktualisiert und angezeigt");
        }
    }

    public void loginFailed(String title, String msg) {
        logger.warn("Login fehlgeschlagen: {} - {}", title, msg);
        if (anmeldeController != null) {
            anmeldeController.showError(title, msg);
        }
    }

    // Wird vom Dashboard aufgerufen, wenn der Benutzer im Menü "Quiz starten" klickt
    public void onQuizMenuClicked() {
        logger.info("Quiz-Menü geklickt, sende Quiz-Initialisierungs-Paket");
        try {
            C2SINITQuiz packet = new C2SINITQuiz();
            ClientNetworkController.socketClient.send(packet);
            logger.debug("Quiz-Initialisierungs-Paket gesendet");
        } catch (IOException e) {
            logger.error("Fehler beim Senden des Quiz-Initialisierungs-Pakets", e);
        }
    }
    public void showStats(Quiz[] quizzes) {
        logger.info("Zeige Statistiken ({} Quizzes)", quizzes != null ? quizzes.length : 0);
        if (dashboardFrame != null) {
            dashboardFrame.showStats(quizzes);
        }
    }
    // Lehrer klickt im Menü auf "Schüler"
    public void onSchuelerMenuClicked() {
        logger.info("Schüler-Menü geklickt, sende Anfrage für Schülerliste");
        
        // In separatem Thread ausführen, um UI nicht zu blockieren
        new Thread(() -> {
            try {
                C2SGETAllSchueler request = new C2SGETAllSchueler();
                S2CPOSTAllSchueler response = ClientNetworkController.socketClient
                    .getChannel()
                    .sendAndWait(
                        request,
                        S2CPOSTAllSchueler.class,
                        5,
                        TimeUnit.SECONDS
                    );
                
                // UI-Update im EDT (Event Dispatch Thread)
                javax.swing.SwingUtilities.invokeLater(() -> {
                    Schueler[] schueler = response.getSchueler();
                    showSchuelerList(schueler);
                });
                
            } catch (TimeoutException e) {
                logger.error("Timeout beim Laden der Schülerliste", e);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (dashboardFrame != null) {
                        javax.swing.JOptionPane.showMessageDialog(
                            dashboardFrame,
                            "Schülerliste konnte nicht geladen werden (Timeout).",
                            "Fehler",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            } catch (IOException e) {
                logger.error("Fehler beim Laden der Schülerliste", e);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (dashboardFrame != null) {
                        javax.swing.JOptionPane.showMessageDialog(
                            dashboardFrame,
                            "Fehler beim Laden der Schülerliste: " + e.getMessage(),
                            "Fehler",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Unterbrochen beim Laden der Schülerliste", e);
            }
        }).start();
    }

    // Wird vom Netzwerkcode aufgerufen, wenn die Schülerliste ankommt
    public void showSchuelerList(Schueler[] schueler) {
        logger.info("Schülerliste erhalten ({} Schüler)", schueler != null ? schueler.length : 0);
        if (dashboardFrame != null) {
            dashboardFrame.showSchuelerList(schueler);
        }
    }


    // Wird vom Netzwerkcode aufgerufen, sobald die Fachbegriffe angekommen sind
    public void showQuiz(FachbegriffItem[] items) {
        logger.info("Quiz-Items erhalten ({} Items), zeige Quiz-Fenster", items != null ? items.length : 0);
        if (dashboardFrame != null) {
            dashboardFrame.showQuiz(items);
        }
    }

    public void showProfile() {
        logger.debug("Zeige Profil - lade Account-Daten vom Server");
        // Lade Account-Daten vom Server
        new Thread(() -> {
            try {
                C2SGETOwnAccount request = new C2SGETOwnAccount();
                S2CPOSTOwnAccount response = ClientNetworkController.socketClient
                    .getChannel()
                    .sendAndWait(
                        request,
                        S2CPOSTOwnAccount.class,
                        5,
                        TimeUnit.SECONDS
                    );
                
                // UI-Update im EDT (Event Dispatch Thread)
                javax.swing.SwingUtilities.invokeLater(() -> {
                    Nutzer updatedNutzer = response.getNutzer();
                    if (updatedNutzer != null) {
                        this.currentNutzer = updatedNutzer;
                        if (dashboardFrame != null) {
                            dashboardFrame.setNutzer(updatedNutzer);
                            dashboardFrame.showProfile();
                        }
                    }
                });
                
            } catch (TimeoutException e) {
                logger.error("Timeout beim Laden der Account-Daten", e);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (dashboardFrame != null) {
                        dashboardFrame.showProfile(); // Zeige Profil trotzdem mit alten Daten
                    }
                });
            } catch (IOException e) {
                logger.error("Fehler beim Laden der Account-Daten", e);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (dashboardFrame != null) {
                        dashboardFrame.showProfile(); // Zeige Profil trotzdem mit alten Daten
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Unterbrochen beim Laden der Account-Daten", e);
            }
        }).start();
    }

    public void updateOwnAccount(Nutzer nutzer) {
        logger.info("Account-Daten aktualisiert für: {}", nutzer != null ? nutzer.getUsername() : "null");
        this.currentNutzer = nutzer;
        if (dashboardFrame != null) {
            dashboardFrame.setNutzer(nutzer);
            // Aktualisiere Profil-Panel, falls es bereits angezeigt wird
            if (dashboardFrame.isProfileVisible()) {
                dashboardFrame.refreshProfile();
            }
        }
    }

    public Nutzer getCurrentNutzer() {
        return currentNutzer;
    }

    /**
     * Lädt die Quizes eines bestimmten Schülers (für Lehrer).
     */
    public void loadSchuelerQuizes(String schuelerUsername) {
        logger.info("Lade Quizes für Schüler: {}", schuelerUsername);
        
        // In separatem Thread ausführen, um UI nicht zu blockieren
        new Thread(() -> {
            try {
                C2SGETSchuelerStats request = new C2SGETSchuelerStats(schuelerUsername);
                S2CPOSTStats response = ClientNetworkController.socketClient
                    .getChannel()
                    .sendAndWait(
                        request,
                        S2CPOSTStats.class,
                        5,
                        TimeUnit.SECONDS
                    );
                
                // UI-Update im EDT (Event Dispatch Thread)
                javax.swing.SwingUtilities.invokeLater(() -> {
                    Quiz[] quizzes = response.getQuizzes();
                    showStats(quizzes);
                });
                
            } catch (TimeoutException e) {
                logger.error("Timeout beim Laden der SchuelerQuizes", e);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (dashboardFrame != null) {
                        javax.swing.JOptionPane.showMessageDialog(
                            dashboardFrame,
                            "Quizes konnten nicht geladen werden (Timeout).",
                            "Fehler",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            } catch (IOException e) {
                logger.error("Fehler beim Laden der SchuelerQuizes", e);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (dashboardFrame != null) {
                        javax.swing.JOptionPane.showMessageDialog(
                            dashboardFrame,
                            "Fehler beim Laden der Quizes: " + e.getMessage(),
                            "Fehler",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Unterbrochen beim Laden der SchuelerQuizes", e);
            }
        }).start();
    }
}
