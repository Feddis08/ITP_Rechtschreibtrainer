package at.tgm.server;

import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Lehrer;
import at.tgm.objects.NutzerStatus;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static Nutzer[] nutzers;
    
    // Datenstrukturen für Lernkarten-Verwaltung
    public static FachbegriffItem[] fachbegriffe = new FachbegriffItem[0];
    public static Quiz[] quizTemplates = new Quiz[0];
    public static void main(String[] args) {
        logger.info("Server wird gestartet...");

        int port = 5123;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    logger.error("Ungültiger Port: {}. Port muss zwischen 1 und 65535 liegen", port);
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                logger.error("Ungültiger Port: '{}'. Port muss eine Zahl sein", args[0]);
                System.exit(1);
            }
        }

        nutzers =  new Nutzer[1];
        logger.debug("Nutzer-Array initialisiert");

        Schueler felix = new Schueler("riemer", "123");

        felix.setFirstName("Felix");
        felix.setLastName("Riemer");
        felix.setAge(17);
        felix.setSchoolClass("3BHIT");

// Darstellung
        felix.setDisplayName("Felix R.");
        felix.setBeschreibung("Tech enthusiast, Minecraft dev, Java enjoyer.");
        felix.setStatus(NutzerStatus.ONLINE);

// Kontakt
        felix.setEmail("felix.riemer@example.com");
        felix.setPhoneNumber("+43 660 1234567");

// Profilbild
        felix.setProfilePictureUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Minecraft-creeper-face.jpg/500px-Minecraft-creeper-face.jpg");

        addNutzer(felix);
        logger.info("Schüler '{}' wurde hinzugefügt", felix.getUsername());

        Schueler matthias = new Schueler("wickenhauser", "123");

        matthias.setFirstName("Matthias");
        matthias.setLastName("Wickenhauser");
        matthias.setAge(17);
        matthias.setSchoolClass("3BHIT");

// Darstellung
        matthias.setDisplayName("Matthias W.");
        matthias.setBeschreibung("Linux enjoyer, Network wizard, loyal HIT-Kollege.");
        matthias.setStatus(NutzerStatus.ONLINE);

// Kontakt
        matthias.setEmail("matthias.wickenhauser@example.com");
        matthias.setPhoneNumber("+43 660 9876543");

// Profilbild (witzig aber neutral, gerne änderbar)
        matthias.setProfilePictureUrl("https://upload.wikimedia.org/wikipedia/commons/5/59/Crystal_Project_penguin.png");

        addNutzer(matthias);
        logger.info("Schüler '{}' wurde hinzugefügt", matthias.getUsername());

        Lehrer l = new Lehrer("l","123");
        addNutzer(l);
        logger.info("Lehrer '{}' wurde hinzugefügt", l.getUsername());

        logger.info("Starte Server auf Port {}", port);
        ServerNetworkController.start(port);
    }
    public static Nutzer findNutzerByUsername(String username){
        if (username == null) {
            logger.warn("findNutzerByUsername wurde mit null aufgerufen");
            return null;
        }

        logger.debug("Suche nach Nutzer mit Username: {}", username);
        for (Nutzer n : Server.nutzers){
            if (n != null && username.equals(n.getUsername())) {
                logger.debug("Nutzer '{}' gefunden", username);
                return n;
            }
        }
        logger.debug("Nutzer '{}' nicht gefunden", username);
        return null;
    }

    public static void addNutzer(Nutzer nutzer){
        if (nutzer == null) {
            logger.error("Versuch, null-Nutzer hinzuzufügen");
            throw new IllegalArgumentException("Nutzer darf nicht null sein");
        }

        logger.debug("Füge Nutzer '{}' hinzu", nutzer.getUsername());

        // Suche nach freiem Platz im Array
        for (int i = 0; i < nutzers.length; i++) {
            if (nutzers[i] == null) {
                nutzers[i] = nutzer;
                logger.debug("Nutzer '{}' an Index {} hinzugefügt", nutzer.getUsername(), i);
                return;
            }
        }

        // Kein freier Platz gefunden - Array vergrößern
        logger.debug("Array voll, vergrößere von {} auf {}", nutzers.length, nutzers.length + 1);
        Nutzer[] nutzersNeu = new Nutzer[nutzers.length + 1];
        System.arraycopy(nutzers, 0, nutzersNeu, 0, nutzers.length);
        nutzersNeu[nutzers.length] = nutzer;
        nutzers = nutzersNeu;
        logger.debug("Nutzer '{}' erfolgreich hinzugefügt (Array vergrößert)", nutzer.getUsername());
    }

    public static void removeNutzer(Nutzer nutzer) {
        if (nutzer == null) {
            logger.error("Versuch, null-Nutzer zu entfernen");
            throw new IllegalArgumentException("Nutzer darf nicht null sein");
        }

        logger.debug("Entferne Nutzer '{}'", nutzer.getUsername());

        // Finde den Index des Nutzers
        int indexToRemove = -1;
        for (int i = 0; i < nutzers.length; i++) {
            if (nutzers[i] != null && nutzers[i].equals(nutzer)) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove == -1) {
            logger.warn("Nutzer '{}' nicht im Array gefunden", nutzer.getUsername());
            throw new IllegalArgumentException("Nutzer nicht gefunden");
        }

        // Erstelle neues Array ohne den zu löschenden Nutzer
        Nutzer[] nutzersNeu = new Nutzer[nutzers.length - 1];
        int newIndex = 0;
        for (int i = 0; i < nutzers.length; i++) {
            if (i != indexToRemove) {
                nutzersNeu[newIndex++] = nutzers[i];
            }
        }
        nutzers = nutzersNeu;
        logger.info("Nutzer '{}' erfolgreich entfernt", nutzer.getUsername());
    }

    // ======================================================
    // FachbegriffItem Helper-Methoden
    // ======================================================

    public static FachbegriffItem findFachbegriffById(long id) {
        if (fachbegriffe == null) {
            return null;
        }
        for (FachbegriffItem item : fachbegriffe) {
            if (item != null && item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public static void addFachbegriff(FachbegriffItem item) {
        if (item == null) {
            logger.error("Versuch, null-FachbegriffItem hinzuzufügen");
            throw new IllegalArgumentException("FachbegriffItem darf nicht null sein");
        }

        logger.debug("Füge Fachbegriff '{}' hinzu (ID: {})", item.getWord(), item.getId());

        // Suche nach freiem Platz im Array
        for (int i = 0; i < fachbegriffe.length; i++) {
            if (fachbegriffe[i] == null) {
                fachbegriffe[i] = item;
                logger.debug("Fachbegriff '{}' an Index {} hinzugefügt", item.getWord(), i);
                return;
            }
        }

        // Kein freier Platz gefunden - Array vergrößern
        logger.debug("Fachbegriffe-Array voll, vergrößere von {} auf {}", fachbegriffe.length, fachbegriffe.length + 1);
        FachbegriffItem[] neu = new FachbegriffItem[fachbegriffe.length + 1];
        System.arraycopy(fachbegriffe, 0, neu, 0, fachbegriffe.length);
        neu[fachbegriffe.length] = item;
        fachbegriffe = neu;
        logger.info("Fachbegriff '{}' erfolgreich hinzugefügt (ID: {}, Array vergrößert)", item.getWord(), item.getId());
    }

    public static void updateFachbegriff(long id, FachbegriffItem updated) {
        if (updated == null) {
            logger.error("Versuch, null-FachbegriffItem zu aktualisieren");
            throw new IllegalArgumentException("FachbegriffItem darf nicht null sein");
        }

        FachbegriffItem existing = findFachbegriffById(id);
        if (existing == null) {
            logger.warn("FachbegriffItem mit ID {} nicht gefunden", id);
            throw new IllegalArgumentException("FachbegriffItem nicht gefunden");
        }

        // Aktualisiere Felder
        existing.setWord(updated.getWord());
        existing.setLevel(updated.getLevel());
        existing.setPoints(updated.getPoints());
        existing.setMaxPoints(updated.getMaxPoints());
        existing.setPhrase(updated.getPhrase());

        logger.info("FachbegriffItem '{}' (ID: {}) erfolgreich aktualisiert", existing.getWord(), id);
    }

    public static void removeFachbegriff(long id) {
        FachbegriffItem item = findFachbegriffById(id);
        if (item == null) {
            logger.warn("FachbegriffItem mit ID {} nicht gefunden", id);
            throw new IllegalArgumentException("FachbegriffItem nicht gefunden");
        }

        logger.debug("Entferne Fachbegriff '{}' (ID: {})", item.getWord(), id);

        // Finde den Index
        int indexToRemove = -1;
        for (int i = 0; i < fachbegriffe.length; i++) {
            if (fachbegriffe[i] != null && fachbegriffe[i].getId() == id) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove == -1) {
            logger.warn("FachbegriffItem mit ID {} nicht im Array gefunden", id);
            throw new IllegalArgumentException("FachbegriffItem nicht gefunden");
        }

        // Erstelle neues Array ohne den zu löschenden Eintrag
        FachbegriffItem[] neu = new FachbegriffItem[fachbegriffe.length - 1];
        int newIndex = 0;
        for (int i = 0; i < fachbegriffe.length; i++) {
            if (i != indexToRemove) {
                neu[newIndex++] = fachbegriffe[i];
            }
        }
        fachbegriffe = neu;
        logger.info("FachbegriffItem '{}' (ID: {}) erfolgreich entfernt", item.getWord(), id);
    }

    // ======================================================
    // Quiz-Template Helper-Methoden
    // ======================================================

    public static Quiz findQuizTemplateById(long id) {
        if (quizTemplates == null) {
            return null;
        }
        for (Quiz quiz : quizTemplates) {
            if (quiz != null && quiz.getId() == id) {
                return quiz;
            }
        }
        return null;
    }

    public static void addQuizTemplate(Quiz quiz) {
        if (quiz == null) {
            logger.error("Versuch, null-QuizTemplate hinzuzufügen");
            throw new IllegalArgumentException("QuizTemplate darf nicht null sein");
        }

        logger.debug("Füge QuizTemplate hinzu (ID: {})", quiz.getId());

        // Suche nach freiem Platz im Array
        for (int i = 0; i < quizTemplates.length; i++) {
            if (quizTemplates[i] == null) {
                quizTemplates[i] = quiz;
                logger.debug("QuizTemplate an Index {} hinzugefügt", i);
                return;
            }
        }

        // Kein freier Platz gefunden - Array vergrößern
        logger.debug("QuizTemplates-Array voll, vergrößere von {} auf {}", quizTemplates.length, quizTemplates.length + 1);
        Quiz[] neu = new Quiz[quizTemplates.length + 1];
        System.arraycopy(quizTemplates, 0, neu, 0, quizTemplates.length);
        neu[quizTemplates.length] = quiz;
        quizTemplates = neu;
        logger.info("QuizTemplate (ID: {}) erfolgreich hinzugefügt (Array vergrößert)", quiz.getId());
    }

    public static void updateQuizTemplate(long id, Quiz updated) {
        if (updated == null) {
            logger.error("Versuch, null-QuizTemplate zu aktualisieren");
            throw new IllegalArgumentException("QuizTemplate darf nicht null sein");
        }

        Quiz existing = findQuizTemplateById(id);
        if (existing == null) {
            logger.warn("QuizTemplate mit ID {} nicht gefunden", id);
            throw new IllegalArgumentException("QuizTemplate nicht gefunden");
        }

        // Aktualisiere Name und Items
        existing.setName(updated.getName());
        existing.setItems(updated.getItems());

        logger.info("QuizTemplate (ID: {}) erfolgreich aktualisiert", id);
    }

    public static void removeQuizTemplate(long id) {
        Quiz quiz = findQuizTemplateById(id);
        if (quiz == null) {
            logger.warn("QuizTemplate mit ID {} nicht gefunden", id);
            throw new IllegalArgumentException("QuizTemplate nicht gefunden");
        }

        logger.debug("Entferne QuizTemplate (ID: {})", id);

        // Finde den Index
        int indexToRemove = -1;
        for (int i = 0; i < quizTemplates.length; i++) {
            if (quizTemplates[i] != null && quizTemplates[i].getId() == id) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove == -1) {
            logger.warn("QuizTemplate mit ID {} nicht im Array gefunden", id);
            throw new IllegalArgumentException("QuizTemplate nicht gefunden");
        }

        // Erstelle neues Array ohne den zu löschenden Eintrag
        Quiz[] neu = new Quiz[quizTemplates.length - 1];
        int newIndex = 0;
        for (int i = 0; i < quizTemplates.length; i++) {
            if (i != indexToRemove) {
                neu[newIndex++] = quizTemplates[i];
            }
        }
        quizTemplates = neu;
        logger.info("QuizTemplate (ID: {}) erfolgreich entfernt", id);
    }
}
