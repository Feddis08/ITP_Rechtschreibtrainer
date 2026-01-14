package at.tgm.server;

import at.tgm.network.packets.S2CPOSTAllSchueler;
import at.tgm.network.packets.S2CPOSTStats;
import at.tgm.network.packets.S2CResponseSchuelerVorschlag;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * State für authentifizierte Lehrer-Clients.
 * Implementiert Lehrer-spezifische Funktionalität.
 */
public class LehrerState implements ClientState {

    private static final Logger logger = LoggerFactory.getLogger(LehrerState.class);

    @Override
    public void postAllSchueler(ServerClient client, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        // Sammle alle Schüler in einem Array (effizienter als doppelte Iteration)
        List<Schueler> schuelerList = new ArrayList<>();
        if (Server.nutzers != null) {
            for (Nutzer nutzer : Server.nutzers) {
                if (nutzer instanceof Schueler) {
                    schuelerList.add((Schueler) nutzer);
                }
            }
        }

        Schueler[] schuelerArray = schuelerList.toArray(new Schueler[0]);
        logger.info("Sende {} Schüler an Lehrer '{}' (Request-ID: {})", 
                   schuelerArray.length, 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown",
                   requestId);
        S2CPOSTAllSchueler packet = new S2CPOSTAllSchueler(schuelerArray);
        packet.setRequestId(requestId); // WICHTIG: Request-ID übernehmen
        client.send(packet);
        logger.debug("Schülerliste erfolgreich gesendet");
    }

    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        logger.warn("Lehrer '{}' versuchte, Quiz hinzuzufügen (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("Lehrer können keine Quizzes hinzufügen");
    }

    @Override
    public void startQuiz(ServerClient client) throws IOException {
        logger.warn("Lehrer '{}' versuchte, Quiz zu starten (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("Lehrer können keine Quizzes starten");
    }

    @Override
    public void finishQuiz(ServerClient client, FachbegriffItem[] fgs) {
        logger.warn("Lehrer '{}' versuchte, Quiz zu beenden (nicht erlaubt)", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
        throw new UnsupportedOperationException("Lehrer können keine Quizzes beenden");
    }

    @Override
    public void postStats(ServerClient client, long requestId) {
        logger.warn("Lehrer '{}' versuchte, Statistiken abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Lehrer können keine Statistiken abrufen");
    }

    @Override
    public void addSchueler(ServerClient client, Schueler schueler, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (schueler == null) {
            logger.error("Lehrer '{}' versuchte, null-Schueler hinzuzufügen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            S2CResponseSchuelerVorschlag response = new S2CResponseSchuelerVorschlag(false, "Schüler-Daten fehlen");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        String username = schueler.getUsername();
        if (username == null || username.trim().isEmpty()) {
            logger.error("Lehrer '{}' versuchte, Schueler ohne Benutzername hinzuzufügen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            S2CResponseSchuelerVorschlag response = new S2CResponseSchuelerVorschlag(false, "Benutzername ist erforderlich");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Prüfe, ob Benutzername bereits existiert
        Nutzer existing = Server.findNutzerByUsername(username);
        if (existing != null) {
            logger.warn("Lehrer '{}' versuchte, Schueler mit bereits existierendem Benutzername '{}' hinzuzufügen", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", username);
            S2CResponseSchuelerVorschlag response = new S2CResponseSchuelerVorschlag(false, "Benutzername bereits vorhanden");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Füge Schüler hinzu
        try {
            Server.addNutzer(schueler);
            logger.info("Lehrer '{}' hat erfolgreich Schueler '{}' hinzugefügt", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", username);
            S2CResponseSchuelerVorschlag response = new S2CResponseSchuelerVorschlag(true, "Schüler erfolgreich angelegt");
            response.setRequestId(requestId);
            client.send(response);
        } catch (Exception e) {
            logger.error("Fehler beim Hinzufügen des Schülers '{}' durch Lehrer '{}'", 
                        username, client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", e);
            S2CResponseSchuelerVorschlag response = new S2CResponseSchuelerVorschlag(false, "Fehler: " + e.getMessage());
            response.setRequestId(requestId);
            client.send(response);
        }
    }

    @Override
    public void postSchuelerStats(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (schuelerUsername == null || schuelerUsername.trim().isEmpty()) {
            logger.error("Lehrer '{}' versuchte, Stats ohne Benutzername abzurufen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            return;
        }

        // Finde den Schüler
        Nutzer nutzer = Server.findNutzerByUsername(schuelerUsername);
        if (nutzer == null || !(nutzer instanceof Schueler)) {
            logger.warn("Lehrer '{}' versuchte, Stats für nicht existierenden Schüler '{}' abzurufen", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", schuelerUsername);
            // Sende leeres Array
            S2CPOSTStats response = new S2CPOSTStats(new Quiz[0]);
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        Schueler schueler = (Schueler) nutzer;
        Quiz[] quizzes = schueler.getQuizzes();
        if (quizzes == null) {
            quizzes = new Quiz[0];
        }

        logger.info("Sende {} Quizes von Schüler '{}' an Lehrer '{}' (Request-ID: {})", 
                   quizzes.length, 
                   schuelerUsername,
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown",
                   requestId);
        
        S2CPOSTStats response = new S2CPOSTStats(quizzes);
        response.setRequestId(requestId); // WICHTIG: Request-ID übernehmen
        client.send(response);
        logger.debug("SchuelerStats erfolgreich gesendet");
    }

    @Override
    public void toggleSchuelerStatus(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (schuelerUsername == null || schuelerUsername.trim().isEmpty()) {
            logger.error("Lehrer '{}' versuchte, Status ohne Benutzername zu ändern", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(false, "Benutzername ist erforderlich");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Finde den Schüler
        at.tgm.objects.Nutzer nutzer = Server.findNutzerByUsername(schuelerUsername);
        if (nutzer == null || !(nutzer instanceof Schueler)) {
            logger.warn("Lehrer '{}' versuchte, Status für nicht existierenden Schüler '{}' zu ändern", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", schuelerUsername);
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(false, "Schüler nicht gefunden");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        Schueler schueler = (Schueler) nutzer;
        boolean wasDeactivated = schueler.isDeactivated();
        schueler.setDeactivated(!wasDeactivated);
        
        String action = wasDeactivated ? "eingeschrieben" : "ausgeschrieben";
        logger.info("Lehrer '{}' hat Schüler '{}' {} (Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", 
                   schuelerUsername, action, requestId);
        
        at.tgm.network.packets.S2CResponseSchuelerOperation response = 
            new at.tgm.network.packets.S2CResponseSchuelerOperation(true, 
                "Schüler erfolgreich " + action);
        response.setRequestId(requestId);
        client.send(response);
    }

    @Override
    public void deleteSchueler(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (schuelerUsername == null || schuelerUsername.trim().isEmpty()) {
            logger.error("Lehrer '{}' versuchte, Schüler ohne Benutzername zu löschen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(false, "Benutzername ist erforderlich");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Finde den Schüler
        at.tgm.objects.Nutzer nutzer = Server.findNutzerByUsername(schuelerUsername);
        if (nutzer == null || !(nutzer instanceof Schueler)) {
            logger.warn("Lehrer '{}' versuchte, nicht existierenden Schüler '{}' zu löschen", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", schuelerUsername);
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(false, "Schüler nicht gefunden");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Lösche den Schüler
        try {
            Server.removeNutzer(nutzer);
            logger.info("Lehrer '{}' hat Schüler '{}' gelöscht (Request-ID: {})", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", 
                       schuelerUsername, requestId);
            
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(true, "Schüler erfolgreich gelöscht");
            response.setRequestId(requestId);
            client.send(response);
        } catch (Exception e) {
            logger.error("Fehler beim Löschen des Schülers '{}' durch Lehrer '{}'", 
                        schuelerUsername, client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", e);
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(false, "Fehler: " + e.getMessage());
            response.setRequestId(requestId);
            client.send(response);
        }
    }

    @Override
    public void setSchuelerNote(ServerClient client, String schuelerUsername, at.tgm.objects.Note note, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (schuelerUsername == null || schuelerUsername.trim().isEmpty()) {
            logger.error("Lehrer '{}' versuchte, Note ohne Benutzername zu setzen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(false, "Benutzername ist erforderlich");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }
        if (note == null) {
            logger.error("Lehrer '{}' versuchte, null-Note zu setzen", 
                        client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown");
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(false, "Note ist erforderlich");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        // Finde den Schüler
        at.tgm.objects.Nutzer nutzer = Server.findNutzerByUsername(schuelerUsername);
        if (nutzer == null || !(nutzer instanceof Schueler)) {
            logger.warn("Lehrer '{}' versuchte, Note für nicht existierenden Schüler '{}' zu setzen", 
                       client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", schuelerUsername);
            at.tgm.network.packets.S2CResponseSchuelerOperation response = 
                new at.tgm.network.packets.S2CResponseSchuelerOperation(false, "Schüler nicht gefunden");
            response.setRequestId(requestId);
            client.send(response);
            return;
        }

        Schueler schueler = (Schueler) nutzer;
        schueler.setNote(note);
        
        logger.info("Lehrer '{}' hat Note '{}' für Schüler '{}' gesetzt (Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", 
                   note.getNotenwert() != null ? note.getNotenwert().getDisplayName() : "null",
                   schuelerUsername, requestId);
        
        at.tgm.network.packets.S2CResponseSchuelerOperation response = 
            new at.tgm.network.packets.S2CResponseSchuelerOperation(true, 
                "Note erfolgreich gesetzt");
        response.setRequestId(requestId);
        client.send(response);
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

        at.tgm.objects.Nutzer nutzer = client.getNutzer();
        // Hole die aktuellen Daten vom Server (falls sich etwas geändert hat)
        at.tgm.objects.Nutzer serverNutzer = Server.findNutzerByUsername(nutzer.getUsername());
        if (serverNutzer == null) {
            logger.warn("Nutzer '{}' nicht auf Server gefunden", nutzer.getUsername());
            serverNutzer = nutzer; // Fallback: verwende lokale Daten
        }

        logger.info("Sende Account-Daten an Lehrer '{}' (Request-ID: {})", 
                   serverNutzer.getUsername(), requestId);
        
        at.tgm.network.packets.S2CPOSTOwnAccount response = new at.tgm.network.packets.S2CPOSTOwnAccount(serverNutzer);
        response.setRequestId(requestId);
        client.send(response);
        logger.debug("Account-Daten erfolgreich gesendet");
    }
}
