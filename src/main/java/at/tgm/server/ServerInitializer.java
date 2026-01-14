package at.tgm.server;

import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Lehrer;
import at.tgm.objects.Nutzer;
import at.tgm.objects.NutzerStatus;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialisiert die Server-Datenstrukturen mit initialen Werten.
 * Diese Klasse kapselt alle Setup-Logik für Test- und Demo-Daten.
 */
public class ServerInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ServerInitializer.class);

    /**
     * Initialisiert alle Server-Datenstrukturen mit initialen Werten.
     * Diese Methode erstellt und konfiguriert alle Standard-Nutzer.
     */
    public static void initialize() {
        logger.info("Initialisiere Server-Datenstrukturen...");

        // Initialisiere Nutzer-Array
        Server.nutzers = new Nutzer[1];
        logger.debug("Nutzer-Array initialisiert");

        // Erstelle und konfiguriere initiale Nutzer
        initializeSchueler();
        initializeLehrer();

        // Erstelle initiale Lernkarten und Quiz-Templates
        initializeFachbegriffe();
        initializeQuizTemplates();

        logger.info("Server-Initialisierung abgeschlossen. {} Nutzer wurden erstellt.", Server.nutzers.length);
    }

    /**
     * Erstellt und konfiguriert alle initialen Schüler.
     */
    private static void initializeSchueler() {
        // Schüler: Felix Riemer
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

        Server.addNutzer(felix);
        logger.info("Schüler '{}' wurde hinzugefügt", felix.getUsername());

        // Schüler: Matthias Wickenhauser
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

        Server.addNutzer(matthias);
        logger.info("Schüler '{}' wurde hinzugefügt", matthias.getUsername());
    }

    /**
     * Erstellt und konfiguriert alle initialen Lehrer.
     */
    private static void initializeLehrer() {
        Lehrer lehrer = new Lehrer("l", "123");
        Server.addNutzer(lehrer);
        logger.info("Lehrer '{}' wurde hinzugefügt", lehrer.getUsername());
    }

    /**
     * Erstellt und konfiguriert 10 initiale Lernkarten (Fachbegriffe).
     */
    private static void initializeFachbegriffe() {
        logger.info("Initialisiere Lernkarten...");

        // Lernkarte 1: IDE
        FachbegriffItem ide = new FachbegriffItem("IDE", 1, 2, "Eine integrierte Entwicklungsumgebung, mit der man Code schreibt, testet und debuggt.");
        Server.addFachbegriff(ide);
        logger.debug("Lernkarte '{}' hinzugefügt", ide.getWord());

        // Lernkarte 2: Compiler
        FachbegriffItem compiler = new FachbegriffItem("Compiler", 1, 2, "Ein Programm, das Quellcode in Maschinensprache übersetzt.");
        Server.addFachbegriff(compiler);
        logger.debug("Lernkarte '{}' hinzugefügt", compiler.getWord());

        // Lernkarte 3: Interpreter
        FachbegriffItem interpreter = new FachbegriffItem("Interpreter", 1, 2, "Ein Programm, das Quellcode Zeile für Zeile ausführt.");
        Server.addFachbegriff(interpreter);
        logger.debug("Lernkarte '{}' hinzugefügt", interpreter.getWord());

        // Lernkarte 4: Algorithmus
        FachbegriffItem algorithmus = new FachbegriffItem("Algorithmus", 1, 2, "Eine eindeutige Abfolge von Schritten zur Lösung eines Problems.");
        Server.addFachbegriff(algorithmus);
        logger.debug("Lernkarte '{}' hinzugefügt", algorithmus.getWord());

        // Lernkarte 5: Variable
        FachbegriffItem variable = new FachbegriffItem("Variable", 1, 1, "Ein Speicherplatz, der einen veränderlichen Wert enthält.");
        Server.addFachbegriff(variable);
        logger.debug("Lernkarte '{}' hinzugefügt", variable.getWord());

        // Lernkarte 6: Array
        FachbegriffItem array = new FachbegriffItem("Array", 1, 1, "Eine Datenstruktur, die mehrere Werte desselben Typs speichert.");
        Server.addFachbegriff(array);
        logger.debug("Lernkarte '{}' hinzugefügt", array.getWord());

        // Lernkarte 7: Klasse
        FachbegriffItem klasse = new FachbegriffItem("Klasse", 1, 2, "Ein Bauplan für Objekte, der Attribute und Methoden definiert.");
        Server.addFachbegriff(klasse);
        logger.debug("Lernkarte '{}' hinzugefügt", klasse.getWord());

        // Lernkarte 8: Objekt
        FachbegriffItem objekt = new FachbegriffItem("Objekt", 1, 1, "Eine Instanz einer Klasse mit konkreten Werten.");
        Server.addFachbegriff(objekt);
        logger.debug("Lernkarte '{}' hinzugefügt", objekt.getWord());

        // Lernkarte 9: Konstruktor
        FachbegriffItem konstruktor = new FachbegriffItem("Konstruktor", 1, 2, "Eine spezielle Methode, die beim Erstellen eines Objekts aufgerufen wird.");
        Server.addFachbegriff(konstruktor);
        logger.debug("Lernkarte '{}' hinzugefügt", konstruktor.getWord());

        // Lernkarte 10: Datenbank
        FachbegriffItem datenbank = new FachbegriffItem("Datenbank", 1, 2, "Ein System zur strukturierten Speicherung und Abfrage großer Datenmengen.");
        Server.addFachbegriff(datenbank);
        logger.debug("Lernkarte '{}' hinzugefügt", datenbank.getWord());

        logger.info("{} Lernkarten wurden initialisiert", Server.fachbegriffe.length);
    }

    /**
     * Erstellt und konfiguriert 2 initiale Quiz-Templates.
     */
    private static void initializeQuizTemplates() {
        logger.info("Initialisiere Quiz-Templates...");

        // Hole alle verfügbaren Fachbegriffe
        FachbegriffItem[] alleFachbegriffe = Server.fachbegriffe;
        if (alleFachbegriffe == null || alleFachbegriffe.length < 5) {
            logger.warn("Nicht genug Fachbegriffe vorhanden für Quiz-Templates");
            return;
        }

        // Quiz 1: Grundlagen der Programmierung (erste 5 Fachbegriffe)
        FachbegriffItem[] quiz1Items = new FachbegriffItem[5];
        for (int i = 0; i < 5 && i < alleFachbegriffe.length; i++) {
            quiz1Items[i] = alleFachbegriffe[i];
        }
        Quiz quiz1 = new Quiz("Grundlagen der Programmierung", quiz1Items);
        Server.addQuizTemplate(quiz1);
        logger.info("Quiz-Template '{}' wurde hinzugefügt ({} Items)", quiz1.getName(), quiz1Items.length);

        // Quiz 2: Objektorientierte Programmierung (letzte 5 Fachbegriffe)
        FachbegriffItem[] quiz2Items = new FachbegriffItem[5];
        int startIndex = Math.max(0, alleFachbegriffe.length - 5);
        for (int i = 0; i < 5 && (startIndex + i) < alleFachbegriffe.length; i++) {
            quiz2Items[i] = alleFachbegriffe[startIndex + i];
        }
        Quiz quiz2 = new Quiz("Objektorientierte Programmierung", quiz2Items);
        Server.addQuizTemplate(quiz2);
        logger.info("Quiz-Template '{}' wurde hinzugefügt ({} Items)", quiz2.getName(), quiz2Items.length);

        logger.info("{} Quiz-Templates wurden initialisiert", Server.quizTemplates.length);
    }
}
