package at.tgm.server;

import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Lehrer;
import at.tgm.objects.Nutzer;
import at.tgm.objects.NutzerStatus;
import at.tgm.objects.Quiz;
import at.tgm.objects.Schueler;
import at.tgm.objects.SysAdmin;
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
     * Lädt zuerst Daten aus der Datenbank, erstellt nur neue, wenn keine vorhanden sind.
     */
    public static void initialize() {
        logger.info("Initialisiere Server-Datenstrukturen...");

        // Lade Nutzer aus DB oder erzeuge Default-Daten, falls DB leer ist
        Nutzer[] loadedUsers = loadNutzerFromDatabase();
        if (loadedUsers != null && loadedUsers.length > 0) {
            Server.nutzers = loadedUsers;
            logger.info("✅ {} Nutzer aus Datenbank geladen", loadedUsers.length);
        } else {
            // Initialisiere Nutzer-Array
            Server.nutzers = new Nutzer[1];
            logger.debug("Nutzer-Array initialisiert (leer), erstelle Default-Nutzer...");

            // Erstelle und konfiguriere initiale Nutzer
            initializeSchueler();
            initializeLehrer();
            initializeSysAdmin();
        }

        // Lade oder erstelle Lernkarten und Quiz-Templates
        loadOrInitializeFachbegriffe();
        loadOrInitializeQuizTemplates();

        logger.info("Server-Initialisierung abgeschlossen. {} Nutzer wurden erstellt.", Server.nutzers.length);
    }

    /**
     * Lädt alle Nutzer aus der Datenbank. Gibt null/leer zurück, wenn keine vorhanden oder DB nicht initialisiert.
     */
    private static Nutzer[] loadNutzerFromDatabase() {
        if (!DatabaseManager.getInstance().isInitialized()) {
            logger.warn("DatabaseManager nicht initialisiert - kann Nutzer nicht laden");
            return new Nutzer[0];
        }

        java.util.List<Nutzer> users = new java.util.ArrayList<>();

        try {
            java.sql.Connection conn = DatabaseManager.getConnection();
            try {
                String sql = """
                    SELECT n.id, n.username, n.password, n.type, n.first_name, n.last_name, n.email,
                           n.phone_number, n.age, n.display_name, n.beschreibung, n.status,
                           n.profile_picture_url, n.created_at, n.last_login_timestamp, n.is_deactivated,
                           s.school_class,
                           (l.nutzer_id IS NOT NULL) AS is_lehrer,
                           (sa.nutzer_id IS NOT NULL) AS is_sysadmin
                    FROM nutzer n
                    LEFT JOIN schueler s ON n.id = s.nutzer_id
                    LEFT JOIN lehrer l ON n.id = l.nutzer_id
                    LEFT JOIN sysadmin sa ON n.id = sa.nutzer_id
                    """;

                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                     java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("type");
                        String username = rs.getString("username");
                        String passwordHash = rs.getString("password"); // In DB ist bereits Hash gespeichert
                        Nutzer nutzer;
                        if ("LEHRER".equalsIgnoreCase(type)) {
                            nutzer = new Lehrer(username, passwordHash, true); // true = aus DB geladen
                        } else if ("SYSADMIN".equalsIgnoreCase(type)) {
                            nutzer = new SysAdmin(username, passwordHash, true); // true = aus DB geladen
                        } else {
                            // Default: Schüler
                            Schueler sch = new Schueler(username, passwordHash, true); // true = aus DB geladen
                            sch.setSchoolClass(rs.getString("school_class"));
                            nutzer = sch;
                        }

                        nutzer.setFirstName(rs.getString("first_name"));
                        nutzer.setLastName(rs.getString("last_name"));
                        nutzer.setEmail(rs.getString("email"));
                        nutzer.setPhoneNumber(rs.getString("phone_number"));
                        nutzer.setAge(rs.getInt("age"));
                        nutzer.setDisplayName(rs.getString("display_name"));
                        nutzer.setBeschreibung(rs.getString("beschreibung"));
                        nutzer.setProfilePictureUrl(rs.getString("profile_picture_url"));
                        try {
                            nutzer.setStatus(rs.getString("status") != null ? at.tgm.objects.NutzerStatus.valueOf(rs.getString("status")) : at.tgm.objects.NutzerStatus.OFFLINE);
                        } catch (IllegalArgumentException e) {
                            nutzer.setStatus(at.tgm.objects.NutzerStatus.OFFLINE);
                        }
                        nutzer.setLastLoginTimestamp(rs.getLong("last_login_timestamp"));
                        nutzer.setDeactivated(rs.getBoolean("is_deactivated"));

                        users.add(nutzer);
                    }
                }
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Laden der Nutzer aus DB: {}", e.getMessage(), e);
        }

        return users.toArray(new Nutzer[0]);
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
     * Erstellt und konfiguriert den initialen SysAdmin.
     */
    private static void initializeSysAdmin() {
        SysAdmin sysAdmin = new SysAdmin("admin", "admin");
        Server.addNutzer(sysAdmin);
        logger.info("SysAdmin '{}' wurde hinzugefügt", sysAdmin.getUsername());
    }

    /**
     * Lädt Fachbegriffe aus der Datenbank oder erstellt initiale, falls keine vorhanden sind.
     */
    private static void loadOrInitializeFachbegriffe() {
        logger.info("Lade Lernkarten aus Datenbank...");
        
        try {
            // Versuche, alle Fachbegriffe aus der Datenbank zu laden
            FachbegriffItem[] loadedItems = FachbegriffItem.loadAll();
            
            if (loadedItems != null && loadedItems.length > 0) {
                // Datenbank enthält bereits Fachbegriffe - lade sie
                logger.info("✅ {} Lernkarten aus Datenbank geladen", loadedItems.length);
                for (FachbegriffItem item : loadedItems) {
                    // Füge zu Server-Array hinzu (ohne erneutes Speichern in DB)
                    if (item != null) {
                        // Direktes Hinzufügen zum Array, ohne save() aufzurufen
                        addFachbegriffToArray(item);
                    }
                }
                logger.info("✅ {} Lernkarten erfolgreich in Server-Array geladen", Server.fachbegriffe.length);
            } else {
                // Keine Daten in DB - erstelle initiale Fachbegriffe
                logger.info("Keine Lernkarten in Datenbank gefunden, erstelle initiale Lernkarten...");
                initializeFachbegriffe();
            }
        } catch (Exception e) {
            logger.error("Fehler beim Laden der Lernkarten aus Datenbank: {}", e.getMessage(), e);
            logger.warn("Erstelle stattdessen initiale Lernkarten...");
            initializeFachbegriffe();
        }
    }

    /**
     * Fügt einen Fachbegriff direkt zum Server-Array hinzu, ohne ihn erneut in der DB zu speichern.
     * Prüft, ob das Item bereits im Array ist (anhand der ID), um Duplikate zu vermeiden.
     */
    private static void addFachbegriffToArray(FachbegriffItem item) {
        if (item == null) {
            return;
        }

        // Prüfe, ob das Item bereits im Array ist (anhand der ID)
        for (FachbegriffItem existing : Server.fachbegriffe) {
            if (existing != null && existing.getId() == item.getId()) {
                logger.debug("FachbegriffItem '{}' (ID: {}) ist bereits im Array, überspringe", item.getWord(), item.getId());
                return;
            }
        }

        // Suche nach freiem Platz im Array
        for (int i = 0; i < Server.fachbegriffe.length; i++) {
            if (Server.fachbegriffe[i] == null) {
                Server.fachbegriffe[i] = item;
                return;
            }
        }

        // Kein freier Platz gefunden - Array vergrößern
        FachbegriffItem[] neu = new FachbegriffItem[Server.fachbegriffe.length + 1];
        System.arraycopy(Server.fachbegriffe, 0, neu, 0, Server.fachbegriffe.length);
        neu[Server.fachbegriffe.length] = item;
        Server.fachbegriffe = neu;
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
     * Lädt Quiz-Templates aus der Datenbank oder erstellt initiale, falls keine vorhanden sind.
     */
    private static void loadOrInitializeQuizTemplates() {
        logger.info("Lade Quiz-Templates aus Datenbank...");
        
        try {
            // Versuche, Quiz-Templates aus der Datenbank zu laden
            Quiz[] loadedTemplates = loadQuizTemplatesFromDatabase();
            
            if (loadedTemplates != null && loadedTemplates.length > 0) {
                // Datenbank enthält bereits Quiz-Templates - lade sie
                logger.info("✅ {} Quiz-Templates aus Datenbank geladen", loadedTemplates.length);
                for (Quiz template : loadedTemplates) {
                    if (template != null) {
                        // Direktes Hinzufügen zum Array, ohne erneutes Speichern
                        addQuizTemplateToArray(template);
                    }
                }
                logger.info("✅ {} Quiz-Templates erfolgreich in Server-Array geladen", Server.quizTemplates.length);
            } else {
                // Keine Daten in DB - erstelle initiale Quiz-Templates
                logger.info("Keine Quiz-Templates in Datenbank gefunden, erstelle initiale Templates...");
                initializeQuizTemplates();
            }
        } catch (Exception e) {
            logger.error("Fehler beim Laden der Quiz-Templates aus Datenbank: {}", e.getMessage(), e);
            logger.warn("Erstelle stattdessen initiale Quiz-Templates...");
            initializeQuizTemplates();
        }
    }

    /**
     * Lädt alle Quiz-Templates aus der Datenbank.
     */
    private static Quiz[] loadQuizTemplatesFromDatabase() throws java.sql.SQLException {
        java.sql.Connection conn = at.tgm.server.DatabaseManager.getConnection();
        try {
            // Lade Quiz-Templates
            String sql = "SELECT * FROM quiz_template ORDER BY id";
            java.util.List<Quiz> templates = new java.util.ArrayList<>();
            
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    long templateId = rs.getLong("id");
                    String name = rs.getString("name");
                    
                    // Lade Items für dieses Template
                    FachbegriffItem[] items = loadQuizTemplateItems(conn, templateId);
                    
                    Quiz template = new Quiz(name, items);
                    template.setId(templateId);
                    templates.add(template);
                }
            }
            
            return templates.toArray(new Quiz[0]);
        } finally {
            at.tgm.server.DatabaseManager.returnConnection(conn);
        }
    }

    /**
     * Lädt die FachbegriffItems für ein Quiz-Template.
     * Verwendet die bereits geladenen Fachbegriffe aus Server.fachbegriffe, falls vorhanden.
     */
    private static FachbegriffItem[] loadQuizTemplateItems(java.sql.Connection conn, long templateId) throws java.sql.SQLException {
        String sql = """
            SELECT f.id, f.word, f.level, f.points, f.max_points, f.phrase
            FROM quiz_template_items qti
            JOIN fachbegriff_item f ON qti.fachbegriff_item_id = f.id
            WHERE qti.quiz_template_id = ?
            ORDER BY qti.position
            """;
        
        java.util.List<FachbegriffItem> items = new java.util.ArrayList<>();
        
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, templateId);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long itemId = rs.getLong("id");
                    
                    // Versuche zuerst, das Item aus Server.fachbegriffe zu finden
                    FachbegriffItem item = Server.findFachbegriffById(itemId);
                    
                    if (item == null) {
                        // Item nicht im Server-Array gefunden - erstelle neues (sollte nicht passieren)
                        logger.warn("FachbegriffItem mit ID {} nicht in Server.fachbegriffe gefunden, erstelle neues", itemId);
                        item = new FachbegriffItem(
                            itemId,
                            rs.getString("word"),
                            rs.getInt("level"),
                            rs.getInt("points"),
                            rs.getInt("max_points"),
                            rs.getString("phrase")
                        );
                    }
                    
                    items.add(item);
                }
            }
        }
        
        return items.toArray(new FachbegriffItem[0]);
    }

    /**
     * Fügt ein Quiz-Template direkt zum Server-Array hinzu, ohne es erneut in der DB zu speichern.
     */
    private static void addQuizTemplateToArray(Quiz quiz) {
        if (quiz == null) {
            return;
        }

        // Suche nach freiem Platz im Array
        for (int i = 0; i < Server.quizTemplates.length; i++) {
            if (Server.quizTemplates[i] == null) {
                Server.quizTemplates[i] = quiz;
                return;
            }
        }

        // Kein freier Platz gefunden - Array vergrößern
        Quiz[] neu = new Quiz[Server.quizTemplates.length + 1];
        System.arraycopy(Server.quizTemplates, 0, neu, 0, Server.quizTemplates.length);
        neu[Server.quizTemplates.length] = quiz;
        Server.quizTemplates = neu;
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
