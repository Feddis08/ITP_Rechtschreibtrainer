package at.tgm.server;

import at.tgm.network.packets.S2CPOSTAllLehrer;
import at.tgm.network.packets.S2CResponseLehrerOperation;
import at.tgm.network.packets.S2CResponseLehrerVorschlag;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Lehrer;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * State für authentifizierte SysAdmin-Clients.
 * Implementiert SysAdmin-spezifische Funktionalität (Lehrer-Verwaltung).
 */
public class ServerSysAdminState implements ClientState {

    private static final Logger logger = LoggerFactory.getLogger(ServerSysAdminState.class);

    // ======================================================
    // Schüler-Funktionen (nicht verfügbar für SysAdmin)
    // ======================================================

    @Override
    public void postAllSchueler(ServerClient client, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Schülerliste abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Schülerliste abrufen");
    }

    @Override
    public void addSchueler(ServerClient client, Schueler schueler, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Schüler hinzuzufügen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Schüler hinzufügen");
    }

    @Override
    public void postSchuelerStats(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, SchuelerStats abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine SchuelerStats abrufen");
    }

    @Override
    public void toggleSchuelerStatus(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Schueler-Status zu ändern (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Schueler-Status ändern");
    }

    @Override
    public void deleteSchueler(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Schüler zu löschen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Schüler löschen");
    }

    @Override
    public void setSchuelerNote(ServerClient client, String schuelerUsername, at.tgm.objects.Note note, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Note zu setzen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Noten setzen");
    }

    // ======================================================
    // Quiz-Funktionen (nicht verfügbar für SysAdmin)
    // ======================================================

    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        logger.warn("SysAdmin '{}' versuchte, Quiz hinzuzufügen (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("SysAdmin können keine Quizzes hinzufügen");
    }

    @Override
    public void startQuiz(ServerClient client, long templateId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Quiz zu starten (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("SysAdmin können keine Quizzes starten");
    }

    @Override
    public void getQuizTemplatesForSchueler(ServerClient client, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Quiz-Templates für Schüler abzurufen (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("Nur für Schüler verfügbar");
    }

    @Override
    public void finishQuiz(ServerClient client, FachbegriffItem[] fgs) {
        logger.warn("SysAdmin '{}' versuchte, Quiz zu beenden (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("SysAdmin können keine Quizzes beenden");
    }

    @Override
    public void postStats(ServerClient client, long requestId) {
        logger.warn("SysAdmin '{}' versuchte, Statistiken abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Statistiken abrufen");
    }

    // ======================================================
    // FachbegriffItem-Verwaltung (nicht verfügbar für SysAdmin)
    // ======================================================

    @Override
    public void getAllFachbegriffe(ServerClient client, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Fachbegriffe abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Fachbegriffe abrufen");
    }

    @Override
    public void createFachbegriff(ServerClient client, FachbegriffItem item, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Fachbegriff zu erstellen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Fachbegriffe erstellen");
    }

    @Override
    public void updateFachbegriff(ServerClient client, long id, FachbegriffItem item, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Fachbegriff zu aktualisieren (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Fachbegriffe aktualisieren");
    }

    @Override
    public void deleteFachbegriff(ServerClient client, long id, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Fachbegriff zu löschen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Fachbegriffe löschen");
    }

    // ======================================================
    // Quiz-Template-Verwaltung (nicht verfügbar für SysAdmin)
    // ======================================================

    @Override
    public void getAllQuizTemplates(ServerClient client, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, Quiz-Templates abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Quiz-Templates abrufen");
    }

    @Override
    public void createQuizTemplate(ServerClient client, Quiz quiz, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, QuizTemplate zu erstellen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Quiz-Templates erstellen");
    }

    @Override
    public void updateQuizTemplate(ServerClient client, long id, Quiz quiz, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, QuizTemplate zu aktualisieren (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Quiz-Templates aktualisieren");
    }

    @Override
    public void deleteQuizTemplate(ServerClient client, long id, long requestId) throws IOException {
        logger.warn("SysAdmin '{}' versuchte, QuizTemplate zu löschen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("SysAdmin können keine Quiz-Templates löschen");
    }

    // ======================================================
    // Lehrer-Verwaltung (SF10, SF20)
    // ======================================================

    @Override
    public void postAllLehrer(ServerClient client, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        // Sammle alle Lehrer in einem Array
        List<Lehrer> lehrerList = new ArrayList<>();
        if (Server.nutzers != null) {
            for (Nutzer nutzer : Server.nutzers) {
                if (nutzer instanceof Lehrer) {
                    lehrerList.add((Lehrer) nutzer);
                }
            }
        }

        Lehrer[] lehrerArray = lehrerList.toArray(new Lehrer[0]);
        logger.info("Sende {} Lehrer an SysAdmin '{}' (Request-ID: {})", 
                   lehrerArray.length, 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown",
                   requestId);
        S2CPOSTAllLehrer packet = new S2CPOSTAllLehrer(lehrerArray);
        packet.setRequestId(requestId);
        client.send(packet);
        logger.debug("Lehrerliste erfolgreich gesendet");
    }

    @Override
    public void addLehrer(ServerClient client, Lehrer lehrer, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (lehrer == null) {
            logger.error("SysAdmin '{}' versuchte, null-Lehrer hinzuzufügen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            S2CResponseLehrerVorschlag response = new S2CResponseLehrerVorschlag(false, "Lehrer-Daten fehlen");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        String username = lehrer.getUsername();
        if (username == null || username.trim().isEmpty()) {
            logger.error("SysAdmin '{}' versuchte, Lehrer ohne Benutzername hinzuzufügen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            S2CResponseLehrerVorschlag response = new S2CResponseLehrerVorschlag(false, "Benutzername ist erforderlich");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Prüfe, ob Benutzername bereits existiert
        Nutzer existing = Server.findNutzerByUsername(username);
        if (existing != null) {
            logger.warn("SysAdmin '{}' versuchte, Lehrer mit bereits existierendem Benutzername '{}' hinzuzufügen", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", username);
            S2CResponseLehrerVorschlag response = new S2CResponseLehrerVorschlag(false, "Benutzername bereits vorhanden");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Füge Lehrer hinzu
        try {
            Server.addNutzer(lehrer);
            logger.info("SysAdmin '{}' hat erfolgreich Lehrer '{}' hinzugefügt", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", username);
            S2CResponseLehrerVorschlag response = new S2CResponseLehrerVorschlag(true, "Lehrer erfolgreich angelegt");
            response.setRequestId(requestId);
            client.send(response);
        } catch (Exception e) {
            logger.error("Fehler beim Hinzufügen des Lehrers '{}' durch SysAdmin '{}'", 
                        username, client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", e);
            S2CResponseLehrerVorschlag response = new S2CResponseLehrerVorschlag(false, "Fehler: " + e.getMessage());
            response.setRequestId(requestId);
            client.send(response);
        }
    }

    @Override
    public void toggleLehrerStatus(ServerClient client, String lehrerUsername, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (lehrerUsername == null || lehrerUsername.trim().isEmpty()) {
            logger.error("SysAdmin '{}' versuchte, Status ohne Benutzername zu ändern", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            S2CResponseLehrerOperation response = 
                new S2CResponseLehrerOperation(false, "Benutzername ist erforderlich");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Finde den Lehrer
        Nutzer nutzer = Server.findNutzerByUsername(lehrerUsername);
        if (nutzer == null || !(nutzer instanceof Lehrer)) {
            logger.warn("SysAdmin '{}' versuchte, Status für nicht existierenden Lehrer '{}' zu ändern", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", lehrerUsername);
            S2CResponseLehrerOperation response = 
                new S2CResponseLehrerOperation(false, "Lehrer nicht gefunden");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        Lehrer lehrer = (Lehrer) nutzer;
        boolean wasDeactivated = lehrer.isDeactivated();
        lehrer.setDeactivated(!wasDeactivated);
        
        String action = wasDeactivated ? "eingeschrieben" : "ausgeschrieben";
        logger.info("SysAdmin '{}' hat Lehrer '{}' {} (Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", 
                   lehrerUsername, action, requestId);
        
        S2CResponseLehrerOperation response = 
            new S2CResponseLehrerOperation(true, 
                "Lehrer erfolgreich " + action);
        response.setRequestId(requestId);
        client.send(response);
    }

    @Override
    public void deleteLehrer(ServerClient client, String lehrerUsername, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (lehrerUsername == null || lehrerUsername.trim().isEmpty()) {
            logger.error("SysAdmin '{}' versuchte, Lehrer ohne Benutzername zu löschen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            S2CResponseLehrerOperation response = 
                new S2CResponseLehrerOperation(false, "Benutzername ist erforderlich");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Finde den Lehrer
        Nutzer nutzer = Server.findNutzerByUsername(lehrerUsername);
        if (nutzer == null || !(nutzer instanceof Lehrer)) {
            logger.warn("SysAdmin '{}' versuchte, nicht existierenden Lehrer '{}' zu löschen", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", lehrerUsername);
            S2CResponseLehrerOperation response = 
                new S2CResponseLehrerOperation(false, "Lehrer nicht gefunden");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Lösche den Lehrer
        try {
            Server.removeNutzer(nutzer);
            logger.info("SysAdmin '{}' hat Lehrer '{}' gelöscht (Request-ID: {})", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", 
                       lehrerUsername, requestId);
            
            S2CResponseLehrerOperation response = 
                new S2CResponseLehrerOperation(true, "Lehrer erfolgreich gelöscht");
            response.setRequestId(requestId);
            client.send(response);
        } catch (Exception e) {
            logger.error("Fehler beim Löschen des Lehrers '{}' durch SysAdmin '{}'", 
                        lehrerUsername, client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", e);
            S2CResponseLehrerOperation response = 
                new S2CResponseLehrerOperation(false, "Fehler: " + e.getMessage());
            response.setRequestId(requestId);
            client.send(response);
        }
    }

    @Override
    public void getOwnAccount(ServerClient client, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (client.getNutzer() == null) {
            logger.warn("getOwnAccount() aufgerufen, aber Client hat keinen Nutzer");
            return;
        }

        Nutzer nutzer = client.getNutzer();
        // Hole die aktuellen Daten vom Server (falls sich etwas geändert hat)
        Nutzer serverNutzer = Server.findNutzerByUsername(nutzer.getUsername());
        if (serverNutzer == null) {
            logger.warn("Nutzer '{}' nicht auf Server gefunden", nutzer.getUsername());
            serverNutzer = nutzer; // Fallback: verwende lokale Daten
        }

        logger.info("Sende Account-Daten an SysAdmin '{}' (Request-ID: {})", 
                   serverNutzer.getUsername(), requestId);
        
        at.tgm.network.packets.S2CPOSTOwnAccount response = new at.tgm.network.packets.S2CPOSTOwnAccount(serverNutzer);
        response.setRequestId(requestId);
        client.send(response);
        logger.debug("Account-Daten erfolgreich gesendet");
    }
}
