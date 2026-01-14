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
     * @param client Der ServerClient
     * @param templateId Die ID des Quiz-Templates (0 = zufälliges Quiz)
     */
    void startQuiz(ServerClient client, long templateId) throws IOException;

    /**
     * Sendet alle Quiz-Templates an den Schüler-Client (für Auswahl).
     * Nur für SchuelerState verfügbar.
     * @param client Der ServerClient
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void getQuizTemplatesForSchueler(ServerClient client, long requestId) throws IOException;

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

    /**
     * Aktiviert oder deaktiviert einen Schüler-Account.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param schuelerUsername Der Benutzername des Schülers
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void toggleSchuelerStatus(ServerClient client, String schuelerUsername, long requestId) throws IOException;

    /**
     * Löscht einen Schüler-Account komplett.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param schuelerUsername Der Benutzername des Schülers
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void deleteSchueler(ServerClient client, String schuelerUsername, long requestId) throws IOException;

    /**
     * Setzt eine Note für einen Schüler.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param schuelerUsername Der Benutzername des Schülers
     * @param note Die Note
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void setSchuelerNote(ServerClient client, String schuelerUsername, at.tgm.objects.Note note, long requestId) throws IOException;

    /**
     * Sendet die eigenen Account-Daten an den Client.
     * Verfügbar für alle authentifizierten States.
     * @param client Der ServerClient
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void getOwnAccount(ServerClient client, long requestId) throws IOException;

    // ======================================================
    // FachbegriffItem-Verwaltung (nur für Lehrer)
    // ======================================================

    /**
     * Sendet alle Fachbegriffe an den Lehrer-Client.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void getAllFachbegriffe(ServerClient client, long requestId) throws IOException;

    /**
     * Erstellt einen neuen Fachbegriff.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param item Der neue Fachbegriff
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void createFachbegriff(ServerClient client, FachbegriffItem item, long requestId) throws IOException;

    /**
     * Aktualisiert einen bestehenden Fachbegriff.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param id Die ID des zu aktualisierenden Fachbegriffs
     * @param item Die aktualisierten Daten
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void updateFachbegriff(ServerClient client, long id, FachbegriffItem item, long requestId) throws IOException;

    /**
     * Löscht einen Fachbegriff.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param id Die ID des zu löschenden Fachbegriffs
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void deleteFachbegriff(ServerClient client, long id, long requestId) throws IOException;

    // ======================================================
    // Quiz-Template-Verwaltung (nur für Lehrer)
    // ======================================================

    /**
     * Sendet alle Quiz-Templates an den Lehrer-Client.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void getAllQuizTemplates(ServerClient client, long requestId) throws IOException;

    /**
     * Erstellt ein neues Quiz-Template.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param quiz Das neue Quiz-Template
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void createQuizTemplate(ServerClient client, Quiz quiz, long requestId) throws IOException;

    /**
     * Aktualisiert ein bestehendes Quiz-Template.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param id Die ID des zu aktualisierenden Quiz-Templates
     * @param quiz Die aktualisierten Daten
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void updateQuizTemplate(ServerClient client, long id, Quiz quiz, long requestId) throws IOException;

    /**
     * Löscht ein Quiz-Template.
     * Nur für LehrerState verfügbar.
     * @param client Der ServerClient
     * @param id Die ID des zu löschenden Quiz-Templates
     * @param requestId Die Request-ID aus dem Request-Paket (für Response-Paket)
     */
    void deleteQuizTemplate(ServerClient client, long id, long requestId) throws IOException;
}
