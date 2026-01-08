package at.tgm.server;

import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;

import java.io.IOException;

/**
 * State Interface für verschiedene Client-Rollen.
 * Definiert alle rollenspezifischen Methoden.
 */
public interface ClientState {

    /**
     * Sendet alle Schüler an den Lehrer-Client.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void postAllSchueler(ServerClient client, long requestId) throws IOException;

    /**
     * Fügt ein Quiz dauerhaft zum Schüler hinzu.
     * Nur für SchuelerState verfügbar.
     */
    void addQuiz(ServerClient client, Quiz q);

    /**
     * Startet ein neues Quiz für den Schüler.
     * Nur für SchuelerState verfügbar.
     */
    void startQuiz(ServerClient client) throws IOException;

    /**
     * Bewertet das Quiz und speichert es.
     * Nur für SchuelerState verfügbar.
     */
    void finishQuiz(ServerClient client, FachbegriffItem[] fgs);

    /**
     * Sendet alle bisherigen Quizzes des Schülers.
     * Nur für SchuelerState verfügbar.
     * @param client Der ServerClient
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void postStats(ServerClient client, long requestId);

    /**
     * Fügt einen neuen Schüler zum System hinzu.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param schueler Der neue Schüler
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void addSchueler(ServerClient client, Schueler schueler, long requestId) throws IOException;

    /**
     * Sendet die Statistiken (Quizes) eines bestimmten Schülers an den Lehrer.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param schuelerUsername Der Benutzername des Schülers
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void postSchuelerStats(ServerClient client, String schuelerUsername, long requestId) throws IOException;
}
