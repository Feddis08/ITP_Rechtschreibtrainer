package at.tgm.server;

import at.tgm.network.packets.S2CPOSTQuiz;
import at.tgm.network.packets.S2CPOSTStats;
import at.tgm.network.packets.S2CResultOfQuiz;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * State für authentifizierte Schüler-Clients.
 * Implementiert Schüler-spezifische Funktionalität.
 */
public class SchuelerState implements ClientState {

    private static final Logger logger = LoggerFactory.getLogger(SchuelerState.class);

    @Override
    public void postAllSchueler(ServerClient client, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Schülerliste abzurufen (nicht erlaubt, Request-ID: {})", 
                    client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Schülerliste abrufen");
    }

    /**
     * Fügt ein Quiz dauerhaft zum Schüler hinzu.
     */
    @Override
    public void addQuiz(ServerClient client, Quiz q) {
        if (q == null) {
            logger.warn("Versuch, null-Quiz hinzuzufügen");
            return;
        }

        Schueler s = (Schueler) client.getNutzer();
        Quiz[] quizzes = s.getQuizzes();

        if (quizzes == null) {
            // Erstes Quiz überhaupt
            s.setQuizzes(new Quiz[]{q});
            logger.info("Erstes Quiz für Schüler '{}' hinzugefügt", s.getUsername());
        } else {
            Quiz[] newArr = new Quiz[quizzes.length + 1];
            System.arraycopy(quizzes, 0, newArr, 0, quizzes.length);
            newArr[quizzes.length] = q;
            s.setQuizzes(newArr);
            logger.info("Quiz für Schüler '{}' hinzugefügt (Gesamt: {})", s.getUsername(), newArr.length);
        }
        
        // WICHTIG: Speichere Quiz-Ergebnis in Datenbank
        logger.info("Speichere Quiz-Ergebnis für Schüler '{}' in Datenbank", s.getUsername());
        Server.saveQuizAttemptToDatabase(s, q);
    }

    /**
     * Startet ein neues Quiz.
     */
    @Override
    public void startQuiz(ServerClient client, long templateId) throws IOException {
        String username = client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown";
        logger.info("Starte Quiz für Schüler: {} (Template-ID: {})", username, templateId);

        Quiz quiz;
        if (templateId > 0) {
            // Quiz aus Template erstellen
            Quiz template = Server.findQuizTemplateById(templateId);
            if (template == null) {
                logger.warn("Quiz-Template mit ID {} nicht gefunden für Schüler '{}'", templateId, username);
                throw new IllegalArgumentException("Quiz-Template nicht gefunden");
            }

            // Erstelle neues Quiz aus Template
            FachbegriffItem[] templateItems = template.getItems();
            if (templateItems == null || templateItems.length == 0) {
                logger.warn("Quiz-Template mit ID {} hat keine Items für Schüler '{}'", templateId, username);
                throw new IllegalArgumentException("Quiz-Template hat keine Items");
            }

            // Kopiere Items aus Template (tiefe Kopie, um sicherzustellen, dass Änderungen am Quiz das Template nicht beeinflussen)
            FachbegriffItem[] quizItems = new FachbegriffItem[templateItems.length];
            for (int i = 0; i < templateItems.length; i++) {
                if (templateItems[i] != null) {
                    quizItems[i] = templateItems[i];
                }
            }

            // Erstelle Quiz direkt mit Items (ohne getRandomItems() Aufruf)
            quiz = new Quiz(quizItems, System.currentTimeMillis());
            quiz.setName(template.getName()); // Name vom Template übernehmen
        } else {
            // Zufälliges Quiz (Legacy-Verhalten)
            quiz = new Quiz(10, System.currentTimeMillis());
        }

        ((Schueler) client.getNutzer()).setQuiz(quiz);
        logger.debug("Quiz erstellt mit {} Items", quiz.getCensoredItems() != null ? quiz.getCensoredItems().length : 0);

        client.send(new S2CPOSTQuiz(quiz.getCensoredItems()));
        logger.info("Quiz-Paket an Schüler '{}' gesendet", username);
    }

    @Override
    public void getQuizTemplatesForSchueler(ServerClient client, long requestId) throws IOException {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        Quiz[] templates = Server.quizTemplates != null ? Server.quizTemplates : new Quiz[0];
        
        // Filtere null-Einträge heraus
        int count = 0;
        for (Quiz quiz : templates) {
            if (quiz != null) count++;
        }
        
        Quiz[] filtered = new Quiz[count];
        int index = 0;
        for (Quiz quiz : templates) {
            if (quiz != null) {
                filtered[index++] = quiz;
            }
        }

        logger.info("Sende {} Quiz-Templates an Schüler '{}' (Request-ID: {})", 
                   filtered.length, 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown",
                   requestId);
        
        at.tgm.network.packets.S2CPOSTAllQuizTemplates response = new at.tgm.network.packets.S2CPOSTAllQuizTemplates(filtered);
        response.setRequestId(requestId);
        client.send(response);
        logger.debug("Quiz-Templates erfolgreich an Schüler gesendet");
    }

    /**
     * Bewertet das Quiz, vergibt Punkte und speichert es.
     */
    @Override
    public void finishQuiz(ServerClient client, FachbegriffItem[] fgs) {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }
        if (fgs == null) {
            throw new IllegalArgumentException("FachbegriffItem-Array darf nicht null sein");
        }

        Schueler s = (Schueler) client.getNutzer();
        Quiz quiz = s.getQuiz();

        if (quiz == null) {
            logger.warn("finishQuiz() ohne aktives Quiz für Schüler '{}'", s.getUsername());
            return;
        }

        FachbegriffItem[] correctItems = quiz.getItems();
        if (correctItems == null) {
            logger.warn("Quiz hat keine Items für Schüler '{}'", s.getUsername());
            return;
        }

        // Validierung: Arrays müssen die gleiche Länge haben
        if (fgs.length != correctItems.length) {
            logger.warn("Ungleiche Array-Längen für Schüler '{}': Erwartet {}, Erhalten {}", 
                       s.getUsername(), correctItems.length, fgs.length);
            return;
        }

        logger.info("Bewerte Quiz für Schüler '{}' ({} Items)", s.getUsername(), fgs.length);

        int totalPoints = 0;
        int maxPoints = 0;

        for (int i = 0; i < fgs.length; i++) {
            FachbegriffItem rightOne = correctItems[i];
            FachbegriffItem userOne = fgs[i];
            
            if (rightOne == null || userOne == null) {
                logger.warn("Null-Item an Index {} für Schüler '{}'", i, s.getUsername());
                continue;
            }

            String correct = safe(rightOne.getWord());
            String user = safe(userOne.getWord());

            int full = rightOne.getPoints();
            maxPoints += full;

            int earned = 0;

            if (user.isEmpty()) {
                earned = 0;
            } else if (user.equals(correct)) {
                earned = full;
            } else if (user.equalsIgnoreCase(correct)) {
                earned = Math.max(1, full / 2);
            } else {
                earned = 0;
            }

            totalPoints += earned;

            // Speichere die eingegebene Antwort des Schülers, bevor wir word überschreiben
            userOne.setUserWord(user);

            // Dem Client das richtige Wort + erreichte Punkte + maxPoints zurückgeben
            userOne.setPoints(earned);
            userOne.setMaxPoints(full);
            userOne.setWord(rightOne.getWord());
        }

        quiz.setUserItems(fgs);
        quiz.setPoints(totalPoints);
        quiz.setMaxPoints(maxPoints);
        quiz.setTimeEnded(System.currentTimeMillis());

        logger.info("Quiz bewertet für Schüler '{}': {}/{} Punkte", s.getUsername(), totalPoints, maxPoints);

        addQuiz(client, quiz);        // Quiz speichern
        s.setQuiz(null);              // Quiz als "abgeschlossen" entfernen

        try {
            client.send(new S2CResultOfQuiz(fgs, totalPoints, maxPoints));
            logger.debug("Quiz-Ergebnis an Schüler '{}' gesendet", s.getUsername());
        } catch (IOException e) {
            logger.error("Fehler beim Senden des Quiz-Ergebnisses an Schüler '{}'", s.getUsername(), e);
            // IOException wird nicht weitergeworfen, da die Methode keine IOException deklariert
        }
    }

    /**
     * Sendet alle bisherigen Quizzes des Schülers.
     * Lädt Quiz-Ergebnisse aus der Datenbank, falls noch nicht im Speicher.
     */
    @Override
    public void postStats(ServerClient client, long requestId) {
        if (client == null) {
            throw new IllegalArgumentException("Client darf nicht null sein");
        }

        Schueler s = (Schueler) client.getNutzer();
        Quiz[] quizzes = s.getQuizzes();

        // WICHTIG: Lade Quiz-Ergebnisse aus Datenbank, falls noch nicht geladen
        if (quizzes == null || quizzes.length == 0) {
            logger.info("Lade Quiz-Ergebnisse für Schüler '{}' aus Datenbank...", s.getUsername());
            quizzes = Server.loadQuizAttemptsForSchueler(s);
            if (quizzes != null && quizzes.length > 0) {
                s.setQuizzes(quizzes);
                logger.info("✅ {} Quiz-Ergebnisse für Schüler '{}' aus Datenbank geladen", quizzes.length, s.getUsername());
            }
        }

        if (quizzes == null)
            quizzes = new Quiz[0];

        logger.info("Sende Statistiken an Schüler '{}' ({} Quizzes, Request-ID: {})", s.getUsername(), quizzes.length, requestId);

        try {
            S2CPOSTStats response = new S2CPOSTStats(quizzes);
            response.setRequestId(requestId); // WICHTIG: Request-ID übernehmen
            client.send(response);
            logger.debug("Statistiken erfolgreich an Schüler '{}' gesendet", s.getUsername());
        } catch (IOException e) {
            logger.error("Fehler beim Senden der Statistiken an Schüler '{}'", s.getUsername(), e);
            // IOException wird nicht weitergeworfen, da die Methode keine IOException deklariert
        }
    }

    @Override
    public void addSchueler(ServerClient client, Schueler schueler, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Schueler hinzuzufügen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Schueler hinzufügen");
    }

    @Override
    public void postSchuelerStats(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, SchuelerStats abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine SchuelerStats abrufen");
    }

    @Override
    public void toggleSchuelerStatus(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Schueler-Status zu ändern (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Schueler-Status ändern");
    }

    @Override
    public void deleteSchueler(ServerClient client, String schuelerUsername, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Schueler zu löschen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Schueler löschen");
    }

    @Override
    public void setSchuelerNote(ServerClient client, String schuelerUsername, at.tgm.objects.Note note, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Note zu setzen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Noten setzen");
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

        logger.info("Sende Account-Daten an Schüler '{}' (Request-ID: {})", 
                   serverNutzer.getUsername(), requestId);
        
        at.tgm.network.packets.S2CPOSTOwnAccount response = new at.tgm.network.packets.S2CPOSTOwnAccount(serverNutzer);
        response.setRequestId(requestId);
        client.send(response);
        logger.debug("Account-Daten erfolgreich gesendet");
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // ======================================================
    // FachbegriffItem-Verwaltung (nur für Lehrer)
    // ======================================================

    @Override
    public void getAllFachbegriffe(ServerClient client, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Fachbegriffe abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Fachbegriffe abrufen");
    }

    @Override
    public void createFachbegriff(ServerClient client, FachbegriffItem item, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Fachbegriff zu erstellen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Fachbegriffe erstellen");
    }

    @Override
    public void updateFachbegriff(ServerClient client, long id, FachbegriffItem item, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Fachbegriff zu aktualisieren (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Fachbegriffe aktualisieren");
    }

    @Override
    public void deleteFachbegriff(ServerClient client, long id, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Fachbegriff zu löschen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Fachbegriffe löschen");
    }

    // ======================================================
    // Quiz-Template-Verwaltung (nur für Lehrer)
    // ======================================================

    @Override
    public void getAllQuizTemplates(ServerClient client, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Quiz-Templates abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Quiz-Templates abrufen");
    }

    @Override
    public void createQuizTemplate(ServerClient client, Quiz quiz, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, QuizTemplate zu erstellen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Quiz-Templates erstellen");
    }

    @Override
    public void updateQuizTemplate(ServerClient client, long id, Quiz quiz, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, QuizTemplate zu aktualisieren (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Quiz-Templates aktualisieren");
    }

    @Override
    public void deleteQuizTemplate(ServerClient client, long id, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, QuizTemplate zu löschen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Quiz-Templates löschen");
    }

    // ======================================================
    // Lehrer-Verwaltung (nur für SysAdmin)
    // ======================================================

    @Override
    public void postAllLehrer(ServerClient client, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Lehrerliste abzurufen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Lehrerliste abrufen");
    }

    @Override
    public void addLehrer(ServerClient client, at.tgm.objects.Lehrer lehrer, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Lehrer hinzuzufügen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Lehrer hinzufügen");
    }

    @Override
    public void toggleLehrerStatus(ServerClient client, String lehrerUsername, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Lehrer-Status zu ändern (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Lehrer-Status ändern");
    }

    @Override
    public void deleteLehrer(ServerClient client, String lehrerUsername, long requestId) throws IOException {
        logger.warn("Schüler '{}' versuchte, Lehrer zu löschen (nicht erlaubt, Request-ID: {})", 
                   client.getNutzer() != null ? client.getNutzer().getUsername() : "unknown", requestId);
        throw new UnsupportedOperationException("Schüler können keine Lehrer löschen");
    }
}
