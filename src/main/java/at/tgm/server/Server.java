package at.tgm.server;

import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

        // Initialisiere Datenbank (MUSS erfolgreich sein, sonst Server-Stopp)
        try {
            logger.info("Initialisiere Datenbank...");
            DatabaseManager.getInstance().initialize();
            DatabaseSchema.createTables();
            logger.info("✅ Datenbank erfolgreich initialisiert");
            
            // Shutdown-Hook für sauberes Schließen der Datenbank
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Schließe Datenbank-Verbindungen...");
                DatabaseManager.getInstance().shutdown();
            }));
        } catch (Exception e) {
            logger.error("❌ KRITISCHER FEHLER: Datenbank konnte nicht initialisiert werden!", e);
            logger.error("   Fehler: {}", e.getMessage());
            System.exit(1);
        }

        // Initialisiere Server-Datenstrukturen mit initialen Werten
        ServerInitializer.initialize();

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
                // Speichere in Datenbank, falls initialisiert
                saveNutzerToDatabase(nutzer);
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
        // Speichere in Datenbank, falls initialisiert
        saveNutzerToDatabase(nutzer);
    }

    /**
     * Speichert einen Nutzer in die Datenbank, falls DatabaseManager initialisiert ist.
     */
    private static void saveNutzerToDatabase(Nutzer nutzer) {
        if (nutzer == null) {
            logger.warn("Versuch, null-Nutzer zu speichern");
            return;
        }
        logger.info("saveNutzerToDatabase aufgerufen für Nutzer '{}'", nutzer.getUsername());
        
        if (!DatabaseManager.getInstance().isInitialized()) {
            logger.warn("DatabaseManager nicht initialisiert - kann Nutzer '{}' nicht speichern", nutzer.getUsername());
            return;
        }
        
        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                conn.setAutoCommit(false);
                
                // Bestimme Nutzer-Typ
                String nutzerType = "SCHUELER";
                if (nutzer instanceof at.tgm.objects.Lehrer) {
                    nutzerType = "LEHRER";
                } else if (nutzer instanceof at.tgm.objects.SysAdmin) {
                    nutzerType = "SYSADMIN";
                }
                
                // Speichere Nutzer (nutze createdAt als ID, falls nicht vorhanden)
                String sql = """
                    INSERT INTO nutzer (id, uuid, username, password, type, first_name, last_name, age, display_name, 
                                      beschreibung, email, phone_number, profile_picture_url, status, 
                                      created_at, last_login_timestamp, is_deactivated)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        password = VALUES(password),
                        first_name = VALUES(first_name),
                        last_name = VALUES(last_name),
                        age = VALUES(age),
                        display_name = VALUES(display_name),
                        beschreibung = VALUES(beschreibung),
                        email = VALUES(email),
                        phone_number = VALUES(phone_number),
                        profile_picture_url = VALUES(profile_picture_url),
                        status = VALUES(status),
                        last_login_timestamp = VALUES(last_login_timestamp),
                        is_deactivated = VALUES(is_deactivated)
                    """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    long nutzerId = nutzer.getCreatedAt(); // Verwende createdAt als ID
                    stmt.setLong(1, nutzerId);
                    stmt.setString(2, nutzer.getUuid() != null ? nutzer.getUuid() : java.util.UUID.randomUUID().toString());
                    stmt.setString(3, nutzer.getUsername());
                    // Verwende getPasswordHash() statt getPassword() für bessere Klarheit
                    stmt.setString(4, nutzer.getPasswordHash());
                    stmt.setString(5, nutzerType);
                    stmt.setString(6, nutzer.getFirstName());
                    stmt.setString(7, nutzer.getLastName());
                    stmt.setInt(8, nutzer.getAge());
                    stmt.setString(9, nutzer.getDisplayName());
                    stmt.setString(10, nutzer.getBeschreibung());
                    stmt.setString(11, nutzer.getEmail());
                    stmt.setString(12, nutzer.getPhoneNumber());
                    stmt.setString(13, nutzer.getProfilePictureUrl());
                    stmt.setString(14, nutzer.getStatus() != null ? nutzer.getStatus().name() : "OFFLINE");
                    stmt.setLong(15, nutzer.getCreatedAt());
                    stmt.setLong(16, nutzer.getLastLoginTimestamp());
                    stmt.setBoolean(17, nutzer.isDeactivated());
                    
                    stmt.executeUpdate();
                    logger.debug("Nutzer '{}' in nutzer-Tabelle gespeichert (ID: {})", nutzer.getUsername(), nutzerId);
                }
                
                // Speichere Schüler-spezifische Daten, falls es ein Schüler ist
                if (nutzer instanceof at.tgm.objects.Schueler) {
                    at.tgm.objects.Schueler schueler = (at.tgm.objects.Schueler) nutzer;
                    String schuelerSql = """
                        INSERT INTO schueler (nutzer_id, school_class)
                        VALUES (?, ?)
                        ON DUPLICATE KEY UPDATE
                            school_class = VALUES(school_class)
                        """;
                    
                    try (PreparedStatement stmt = conn.prepareStatement(schuelerSql)) {
                        stmt.setLong(1, nutzer.getCreatedAt());
                        stmt.setString(2, schueler.getSchoolClass());
                        stmt.executeUpdate();
                        logger.debug("Schüler '{}' in schueler-Tabelle gespeichert", nutzer.getUsername());
                    }
                }
                
                // Speichere Lehrer-spezifische Daten, falls es ein Lehrer ist
                if (nutzer instanceof at.tgm.objects.Lehrer) {
                    String lehrerSql = """
                        INSERT INTO lehrer (nutzer_id)
                        VALUES (?)
                        ON DUPLICATE KEY UPDATE nutzer_id = VALUES(nutzer_id)
                        """;
                    
                    try (PreparedStatement stmt = conn.prepareStatement(lehrerSql)) {
                        stmt.setLong(1, nutzer.getCreatedAt());
                        stmt.executeUpdate();
                        logger.debug("Lehrer '{}' in lehrer-Tabelle gespeichert", nutzer.getUsername());
                    }
                }
                
                // Speichere SysAdmin-spezifische Daten, falls es ein SysAdmin ist
                if (nutzer instanceof at.tgm.objects.SysAdmin) {
                    String adminSql = """
                        INSERT INTO sysadmin (nutzer_id)
                        VALUES (?)
                        ON DUPLICATE KEY UPDATE nutzer_id = VALUES(nutzer_id)
                        """;
                    
                    try (PreparedStatement stmt = conn.prepareStatement(adminSql)) {
                        stmt.setLong(1, nutzer.getCreatedAt());
                        stmt.executeUpdate();
                        logger.debug("SysAdmin '{}' in sysadmin-Tabelle gespeichert", nutzer.getUsername());
                    }
                }
                
                conn.commit();
                logger.info("✅ Nutzer '{}' erfolgreich in Datenbank gespeichert", nutzer.getUsername());
            } catch (SQLException e) {
                conn.rollback();
                logger.error("❌ SQLException beim Speichern von Nutzer '{}': {}", nutzer.getUsername(), e.getMessage(), e);
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("❌ Fehler beim Speichern von Nutzer '{}': {}", nutzer.getUsername(), e.getMessage(), e);
        }
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
        logger.info("Nutzer '{}' erfolgreich aus Server-Array entfernt", nutzer.getUsername());

        // Lösche aus Datenbank
        deleteNutzerFromDatabase(nutzer.getUsername());
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

        logger.info("addFachbegriff aufgerufen für '{}' (ID: {})", item.getWord(), item.getId());
        logger.debug("Füge Fachbegriff '{}' hinzu (ID: {})", item.getWord(), item.getId());

        // Suche nach freiem Platz im Array
        for (int i = 0; i < fachbegriffe.length; i++) {
            if (fachbegriffe[i] == null) {
                fachbegriffe[i] = item;
                logger.debug("Fachbegriff '{}' an Index {} hinzugefügt", item.getWord(), i);
                // Speichere in Datenbank, falls initialisiert
                logger.info("Rufe saveFachbegriffToDatabase auf für '{}' (ID: {})", item.getWord(), item.getId());
                saveFachbegriffToDatabase(item);
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
        // Speichere in Datenbank, falls initialisiert
        logger.info("Rufe saveFachbegriffToDatabase auf für '{}' (ID: {})", item.getWord(), item.getId());
        saveFachbegriffToDatabase(item);
    }

    /**
     * Speichert einen Fachbegriff in die Datenbank, falls DatabaseManager initialisiert ist.
     */
    private static void saveFachbegriffToDatabase(FachbegriffItem item) {
        logger.info("saveFachbegriffToDatabase aufgerufen für '{}' (ID: {})", item.getWord(), item.getId());
        
        if (!DatabaseManager.getInstance().isInitialized()) {
            logger.warn("DatabaseManager nicht initialisiert - kann Fachbegriff '{}' nicht speichern", item.getWord());
            return;
        }
        
        logger.debug("DatabaseManager ist initialisiert, speichere Fachbegriff '{}' (ID: {})", item.getWord(), item.getId());
        
        try {
            logger.debug("Rufe item.save() auf für Fachbegriff '{}' (ID: {})", item.getWord(), item.getId());
            item.save();
            logger.info("✅ Fachbegriff '{}' (ID: {}) erfolgreich in Datenbank gespeichert", item.getWord(), item.getId());
        } catch (Exception e) {
            logger.error("❌ Fehler beim Speichern von Fachbegriff '{}' (ID: {}) in Datenbank: {}", 
                       item.getWord(), item.getId(), e.getMessage(), e);
        }
    }

    public static void updateFachbegriff(long id, FachbegriffItem updated) {
        logger.info("updateFachbegriff aufgerufen für ID: {}", id);
        
        if (updated == null) {
            logger.error("Versuch, null-FachbegriffItem zu aktualisieren");
            throw new IllegalArgumentException("FachbegriffItem darf nicht null sein");
        }

        FachbegriffItem existing = findFachbegriffById(id);
        if (existing == null) {
            logger.warn("FachbegriffItem mit ID {} nicht gefunden", id);
            throw new IllegalArgumentException("FachbegriffItem nicht gefunden");
        }

        logger.debug("Aktualisiere Felder für Fachbegriff '{}' (ID: {})", existing.getWord(), id);
        // Aktualisiere Felder
        existing.setWord(updated.getWord());
        existing.setLevel(updated.getLevel());
        existing.setPoints(updated.getPoints());
        existing.setMaxPoints(updated.getMaxPoints());
        existing.setPhrase(updated.getPhrase());

        logger.info("FachbegriffItem '{}' (ID: {}) erfolgreich aktualisiert", existing.getWord(), id);
        
        // WICHTIG: Speichere Änderungen in Datenbank
        logger.info("Speichere aktualisierten Fachbegriff '{}' (ID: {}) in Datenbank", existing.getWord(), id);
        saveFachbegriffToDatabase(existing);
    }

    public static void removeFachbegriff(long id) {
        logger.info("removeFachbegriff aufgerufen für ID: {}", id);
        
        FachbegriffItem item = findFachbegriffById(id);
        if (item == null) {
            logger.warn("FachbegriffItem mit ID {} nicht gefunden", id);
            throw new IllegalArgumentException("FachbegriffItem nicht gefunden");
        }

        logger.debug("Entferne Fachbegriff '{}' (ID: {})", item.getWord(), id);

        // WICHTIG: Lösche zuerst aus Datenbank
        if (DatabaseManager.getInstance().isInitialized()) {
            try {
                logger.info("Lösche Fachbegriff '{}' (ID: {}) aus Datenbank", item.getWord(), id);
                item.delete();
                logger.info("✅ Fachbegriff '{}' (ID: {}) erfolgreich aus Datenbank gelöscht", item.getWord(), id);
            } catch (Exception e) {
                logger.error("❌ Fehler beim Löschen von Fachbegriff '{}' (ID: {}) aus Datenbank: {}", 
                           item.getWord(), id, e.getMessage(), e);
                throw new RuntimeException("Fehler beim Löschen aus Datenbank", e);
            }
        } else {
            logger.warn("DatabaseManager nicht initialisiert - kann Fachbegriff '{}' nicht aus Datenbank löschen", item.getWord());
        }

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

        logger.info("addQuizTemplate aufgerufen für '{}' (ID: {})", quiz.getName(), quiz.getId());
        logger.debug("Füge QuizTemplate hinzu (ID: {})", quiz.getId());

        // Suche nach freiem Platz im Array
        for (int i = 0; i < quizTemplates.length; i++) {
            if (quizTemplates[i] == null) {
                quizTemplates[i] = quiz;
                logger.debug("QuizTemplate an Index {} hinzugefügt", i);
                // Speichere in Datenbank, falls initialisiert
                logger.info("Rufe saveQuizTemplateToDatabase auf für '{}' (ID: {})", quiz.getName(), quiz.getId());
                saveQuizTemplateToDatabase(quiz);
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
        // Speichere in Datenbank, falls initialisiert
        logger.info("Rufe saveQuizTemplateToDatabase auf für '{}' (ID: {})", quiz.getName(), quiz.getId());
        saveQuizTemplateToDatabase(quiz);
    }

    /**
     * Speichert ein Quiz-Template in die Datenbank, falls DatabaseManager initialisiert ist.
     */
    private static void saveQuizTemplateToDatabase(Quiz quiz) {
        logger.info("saveQuizTemplateToDatabase aufgerufen für Quiz '{}' (ID: {})", 
                   quiz != null ? quiz.getName() : "null", quiz != null ? quiz.getId() : -1);
        
        if (!DatabaseManager.getInstance().isInitialized()) {
            logger.warn("DatabaseManager nicht initialisiert - kann QuizTemplate '{}' nicht speichern", 
                       quiz != null ? quiz.getName() : "null");
            return;
        }
        
        if (quiz == null) {
            logger.warn("Versuch, null-QuizTemplate zu speichern");
            return;
        }
        
        logger.debug("DatabaseManager ist initialisiert, speichere QuizTemplate '{}' (ID: {})", quiz.getName(), quiz.getId());
        
        try {
            logger.debug("Hole Datenbank-Connection...");
            Connection conn = DatabaseManager.getConnection();
            logger.debug("Datenbank-Connection erhalten");
            
            try {
                // Speichere Quiz-Template
                String sql = """
                    INSERT INTO quiz_template (id, name, created_at)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        name = VALUES(name)
                    """;
                
                logger.debug("Speichere Quiz-Template '{}' (ID: {}) in quiz_template Tabelle", quiz.getName(), quiz.getId());
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, quiz.getId());
                    stmt.setString(2, quiz.getName());
                    stmt.setLong(3, System.currentTimeMillis());
                    int rowsAffected = stmt.executeUpdate();
                    logger.info("Quiz-Template '{}' (ID: {}) gespeichert - {} Zeilen betroffen", 
                               quiz.getName(), quiz.getId(), rowsAffected);
                }
                
                // Lösche alte Items für dieses Template
                logger.debug("Lösche alte Items für Quiz-Template '{}' (ID: {})", quiz.getName(), quiz.getId());
                String deleteItemsSql = "DELETE FROM quiz_template_items WHERE quiz_template_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteItemsSql)) {
                    stmt.setLong(1, quiz.getId());
                    int deletedRows = stmt.executeUpdate();
                    logger.debug("{} alte Items für Quiz-Template '{}' gelöscht", deletedRows, quiz.getName());
                }
                
                // Speichere Items
                FachbegriffItem[] items = quiz.getItems();
                logger.debug("Quiz-Template '{}' hat {} Items zum Speichern", quiz.getName(), 
                           items != null ? items.length : 0);
                
                if (items != null && items.length > 0) {
                    String insertItemSql = """
                        INSERT INTO quiz_template_items (quiz_template_id, fachbegriff_item_id, position)
                        VALUES (?, ?, ?)
                        """;
                    
                    try (PreparedStatement stmt = conn.prepareStatement(insertItemSql)) {
                        int itemsAdded = 0;
                        for (int i = 0; i < items.length; i++) {
                            if (items[i] != null) {
                                stmt.setLong(1, quiz.getId());
                                stmt.setLong(2, items[i].getId());
                                stmt.setInt(3, i);
                                stmt.addBatch();
                                itemsAdded++;
                                logger.debug("Füge Item {} hinzu: Fachbegriff '{}' (ID: {}) an Position {}", 
                                           i, items[i].getWord(), items[i].getId(), i);
                            } else {
                                logger.warn("Item an Position {} ist null, überspringe", i);
                            }
                        }
                        logger.debug("Führe Batch-Insert für {} Items aus...", itemsAdded);
                        int[] results = stmt.executeBatch();
                        logger.info("✅ {} Items für Quiz-Template '{}' gespeichert", results.length, quiz.getName());
                    }
                } else {
                    logger.warn("Quiz-Template '{}' hat keine Items zum Speichern", quiz.getName());
                }
                
                logger.debug("Commite Transaktion für Quiz-Template '{}'...", quiz.getName());
                conn.commit();
                logger.info("✅ QuizTemplate '{}' (ID: {}) erfolgreich in Datenbank gespeichert", 
                           quiz.getName(), quiz.getId());
            } catch (SQLException e) {
                logger.error("❌ SQLException beim Speichern von QuizTemplate '{}' (ID: {}): {}", 
                           quiz.getName(), quiz.getId(), e.getMessage(), e);
                conn.rollback();
                logger.debug("Transaktion zurückgerollt");
            } finally {
                logger.debug("Gebe Datenbank-Connection zurück...");
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("❌ Fehler beim Speichern von QuizTemplate '{}' (ID: {}): {}", 
                       quiz.getName(), quiz.getId(), e.getMessage(), e);
        }
    }

    public static void updateQuizTemplate(long id, Quiz updated) {
        logger.info("updateQuizTemplate aufgerufen für ID: {}", id);
        
        if (updated == null) {
            logger.error("Versuch, null-QuizTemplate zu aktualisieren");
            throw new IllegalArgumentException("QuizTemplate darf nicht null sein");
        }

        Quiz existing = findQuizTemplateById(id);
        if (existing == null) {
            logger.warn("QuizTemplate mit ID {} nicht gefunden", id);
            throw new IllegalArgumentException("QuizTemplate nicht gefunden");
        }

        logger.debug("Aktualisiere Name und Items für QuizTemplate '{}' (ID: {})", existing.getName(), id);
        // Aktualisiere Name und Items
        existing.setName(updated.getName());
        existing.setItems(updated.getItems());

        logger.info("QuizTemplate '{}' (ID: {}) erfolgreich aktualisiert", existing.getName(), id);
        
        // WICHTIG: Speichere Änderungen in Datenbank
        logger.info("Speichere aktualisiertes QuizTemplate '{}' (ID: {}) in Datenbank", existing.getName(), id);
        saveQuizTemplateToDatabase(existing);
    }

    public static void removeQuizTemplate(long id) {
        logger.info("removeQuizTemplate aufgerufen für ID: {}", id);
        
        Quiz quiz = findQuizTemplateById(id);
        if (quiz == null) {
            logger.warn("QuizTemplate mit ID {} nicht gefunden", id);
            throw new IllegalArgumentException("QuizTemplate nicht gefunden");
        }

        logger.debug("Entferne QuizTemplate '{}' (ID: {})", quiz.getName(), id);

        // WICHTIG: Lösche zuerst aus Datenbank
        deleteQuizTemplateFromDatabase(id);

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
        logger.info("QuizTemplate '{}' (ID: {}) erfolgreich entfernt", quiz.getName(), id);
    }
    
    /**
     * Löscht ein Quiz-Template aus der Datenbank.
     */
    private static void deleteQuizTemplateFromDatabase(long id) {
        logger.info("deleteQuizTemplateFromDatabase aufgerufen für ID: {}", id);
        
        if (!DatabaseManager.getInstance().isInitialized()) {
            logger.warn("DatabaseManager nicht initialisiert - kann QuizTemplate (ID: {}) nicht aus Datenbank löschen", id);
            return;
        }
        
        try {
            logger.debug("Hole Datenbank-Connection...");
            Connection conn = DatabaseManager.getConnection();
            logger.debug("Datenbank-Connection erhalten");
            
            try {
                conn.setAutoCommit(false);
                
                // Lösche zuerst Items
                logger.debug("Lösche Items für QuizTemplate (ID: {})", id);
                String deleteItemsSql = "DELETE FROM quiz_template_items WHERE quiz_template_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteItemsSql)) {
                    stmt.setLong(1, id);
                    int deletedItems = stmt.executeUpdate();
                    logger.info("{} Items für QuizTemplate (ID: {}) gelöscht", deletedItems, id);
                }
                
                // Lösche dann Template
                logger.debug("Lösche QuizTemplate (ID: {})", id);
                String deleteTemplateSql = "DELETE FROM quiz_template WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteTemplateSql)) {
                    stmt.setLong(1, id);
                    int deletedRows = stmt.executeUpdate();
                    if (deletedRows == 0) {
                        logger.warn("QuizTemplate mit ID {} nicht in Datenbank gefunden", id);
                    } else {
                        logger.info("✅ QuizTemplate (ID: {}) erfolgreich aus Datenbank gelöscht", id);
                    }
                }
                
                conn.commit();
                logger.info("✅ QuizTemplate (ID: {}) und zugehörige Items erfolgreich aus Datenbank gelöscht", id);
            } catch (SQLException e) {
                conn.rollback();
                logger.error("❌ SQLException beim Löschen von QuizTemplate (ID: {}): {}", id, e.getMessage(), e);
                throw e;
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("❌ Fehler beim Löschen von QuizTemplate (ID: {}) aus Datenbank: {}", id, e.getMessage(), e);
        }
    }
    
    /**
     * Hilfsmethode: Holt die Nutzer-ID eines Schülers aus der Datenbank.
     * Falls der Nutzer noch nicht in der DB ist, wird er gespeichert.
     */
    private static Long getSchuelerIdFromDatabase(Connection conn, String username) throws SQLException {
        String sql = """
            SELECT n.id 
            FROM nutzer n
            INNER JOIN schueler s ON n.id = s.nutzer_id
            WHERE n.username = ?
            LIMIT 1
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        
        // Falls nicht gefunden, versuche über created_at (falls verwendet)
        // Oder hole den Nutzer aus dem Server-Array und speichere ihn
        at.tgm.objects.Nutzer nutzer = findNutzerByUsername(username);
        if (nutzer != null) {
            // Nutzer existiert im Server, aber nicht in DB - speichere ihn
            logger.warn("Nutzer '{}' nicht in DB gefunden, speichere ihn jetzt...", username);
            saveNutzerToDatabase(nutzer);
            
            // Versuche erneut
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Speichert ein Quiz-Ergebnis (Quiz-Attempt) in die Datenbank.
     */
    public static void saveQuizAttemptToDatabase(at.tgm.objects.Schueler schueler, Quiz quiz) {
        logger.info("saveQuizAttemptToDatabase aufgerufen für Schüler '{}', Quiz (ID: {})", 
                   schueler.getUsername(), quiz.getId());
        
        if (!DatabaseManager.getInstance().isInitialized()) {
            logger.warn("DatabaseManager nicht initialisiert - kann Quiz-Ergebnis nicht speichern");
            return;
        }
        
        if (schueler == null || quiz == null) {
            logger.warn("Versuch, Quiz-Ergebnis mit null-Schüler oder null-Quiz zu speichern");
            return;
        }
        
        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                conn.setAutoCommit(false);
                
                // Hole Schüler-ID aus Datenbank
                Long schuelerId = getSchuelerIdFromDatabase(conn, schueler.getUsername());
                if (schuelerId == null) {
                    logger.error("Konnte Schüler-ID für '{}' nicht aus Datenbank holen", schueler.getUsername());
                    conn.rollback();
                    return;
                }
                
                // Speichere Quiz-Attempt
                String sql = """
                    INSERT INTO quiz_attempt (schueler_id, quiz_template_id, time_started, time_ended, points, max_points)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
                
                long attemptId;
                try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, schuelerId);
                    // quiz_template_id kann null sein, wenn es kein Template-Quiz war
                    Long templateId = quiz.getName() != null ? findQuizTemplateIdByName(conn, quiz.getName()) : null;
                    if (templateId != null) {
                        stmt.setLong(2, templateId);
                    } else {
                        stmt.setNull(2, java.sql.Types.BIGINT);
                    }
                    stmt.setLong(3, quiz.getTimeStarted());
                    stmt.setLong(4, quiz.getTimeEnded() > 0 ? quiz.getTimeEnded() : System.currentTimeMillis());
                    stmt.setInt(5, quiz.getPoints());
                    stmt.setInt(6, quiz.getMaxPoints());
                    
                    stmt.executeUpdate();
                    
                    // Hole generierte ID
                    try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            attemptId = rs.getLong(1);
                            logger.debug("Quiz-Attempt gespeichert mit ID: {}", attemptId);
                        } else {
                            logger.error("Konnte generierte ID für Quiz-Attempt nicht abrufen");
                            conn.rollback();
                            return;
                        }
                    }
                }
                
                // Speichere Quiz-Attempt-Items
                FachbegriffItem[] userItems = quiz.getUserItems();
                if (userItems != null && userItems.length > 0) {
                    String insertItemSql = """
                        INSERT INTO quiz_attempt_items (quiz_attempt_id, fachbegriff_item_id, user_word, points_earned, position)
                        VALUES (?, ?, ?, ?, ?)
                        """;
                    
                    try (PreparedStatement stmt = conn.prepareStatement(insertItemSql)) {
                        FachbegriffItem[] correctItems = quiz.getItems();
                        int itemsAdded = 0;
                        for (int i = 0; i < userItems.length; i++) {
                            if (userItems[i] != null && correctItems != null && i < correctItems.length && correctItems[i] != null) {
                                // Prüfe, ob das FachbegriffItem in der Datenbank existiert
                                if (fachbegriffItemExistsInDatabase(conn, correctItems[i].getId())) {
                                    stmt.setLong(1, attemptId);
                                    stmt.setLong(2, correctItems[i].getId());
                                    stmt.setString(3, userItems[i].getUserWord());
                                    stmt.setInt(4, userItems[i].getPoints());
                                    stmt.setInt(5, i);
                                    stmt.addBatch();
                                    itemsAdded++;
                                } else {
                                    logger.warn("FachbegriffItem mit ID {} existiert nicht in Datenbank, überspringe beim Speichern von Quiz-Attempt", 
                                               correctItems[i].getId());
                                }
                            }
                        }
                        if (itemsAdded > 0) {
                            int[] results = stmt.executeBatch();
                            logger.debug("{} Quiz-Attempt-Items gespeichert", results.length);
                        } else {
                            logger.warn("Keine Quiz-Attempt-Items konnten gespeichert werden (Items existieren nicht in DB)");
                        }
                    }
                }
                
                conn.commit();
                logger.info("✅ Quiz-Ergebnis für Schüler '{}' erfolgreich in Datenbank gespeichert (Attempt-ID: {})", 
                           schueler.getUsername(), attemptId);
            } catch (SQLException e) {
                conn.rollback();
                logger.error("❌ SQLException beim Speichern von Quiz-Ergebnis: {}", e.getMessage(), e);
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("❌ Fehler beim Speichern von Quiz-Ergebnis: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Hilfsmethode: Prüft, ob ein FachbegriffItem in der Datenbank existiert.
     */
    private static boolean fachbegriffItemExistsInDatabase(Connection conn, long id) throws SQLException {
        String sql = "SELECT 1 FROM fachbegriff_item WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Hilfsmethode: Holt die Nutzer-ID anhand des Usernames aus der Datenbank.
     */
    private static Long getNutzerIdFromDatabase(Connection conn, String username) throws SQLException {
        String sql = """
            SELECT id FROM nutzer WHERE username = ? LIMIT 1
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }

    /**
     * Löscht einen Nutzer (und abhängige Daten) aus der Datenbank.
     */
    private static void deleteNutzerFromDatabase(String username) {
        logger.info("deleteNutzerFromDatabase aufgerufen für Username: {}", username);

        if (!DatabaseManager.getInstance().isInitialized()) {
            logger.warn("DatabaseManager nicht initialisiert - kann Nutzer '{}' nicht aus DB löschen", username);
            return;
        }

        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                conn.setAutoCommit(false);

                Long nutzerId = getNutzerIdFromDatabase(conn, username);
                if (nutzerId == null) {
                    logger.warn("Nutzer '{}' nicht in DB gefunden - nichts zu löschen", username);
                    conn.rollback();
                    return;
                }

                String sql = "DELETE FROM nutzer WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, nutzerId);
                    int deleted = stmt.executeUpdate();
                    logger.info("{} Nutzer mit Username '{}' aus DB gelöscht", deleted, username);
                }

                conn.commit();
                logger.info("✅ Nutzer '{}' und abhängige Daten aus DB gelöscht", username);
            } catch (SQLException e) {
                conn.rollback();
                logger.error("❌ SQLException beim Löschen von Nutzer '{}': {}", username, e.getMessage(), e);
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("❌ Fehler beim Löschen von Nutzer '{}': {}", username, e.getMessage(), e);
        }
    }
    
    /**
     * Hilfsmethode: Findet Quiz-Template-ID anhand des Namens.
     */
    private static Long findQuizTemplateIdByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT id FROM quiz_template WHERE name = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }
    
    /**
     * Lädt Quiz-Ergebnisse für einen Schüler aus der Datenbank.
     */
    public static Quiz[] loadQuizAttemptsForSchueler(at.tgm.objects.Schueler schueler) {
        if (!DatabaseManager.getInstance().isInitialized()) {
            logger.warn("DatabaseManager nicht initialisiert - kann Quiz-Ergebnisse nicht laden");
            return new Quiz[0];
        }
        
        if (schueler == null) {
            logger.warn("Versuch, Quiz-Ergebnisse für null-Schüler zu laden");
            return new Quiz[0];
        }
        logger.info("loadQuizAttemptsForSchueler aufgerufen für Schüler '{}'", schueler.getUsername());
        
        java.util.List<Quiz> quizzes = new java.util.ArrayList<>();
        
        try {
            Connection conn = DatabaseManager.getConnection();
            try {
                // Hole Schüler-ID aus Datenbank
                Long schuelerId = getSchuelerIdFromDatabase(conn, schueler.getUsername());
                if (schuelerId == null) {
                    logger.warn("Konnte Schüler-ID für '{}' nicht aus Datenbank holen", schueler.getUsername());
                    return new Quiz[0];
                }
                
                String sql = """
                    SELECT id, quiz_template_id, time_started, time_ended, points, max_points
                    FROM quiz_attempt
                    WHERE schueler_id = ?
                    ORDER BY time_started DESC
                    """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, schuelerId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            long attemptId = rs.getLong("id");
                            long timeStarted = rs.getLong("time_started");
                            long timeEnded = rs.getLong("time_ended");
                            int points = rs.getInt("points");
                            int maxPoints = rs.getInt("max_points");
                            
                            // Lade Items für diesen Attempt
                            FachbegriffItem[] items = loadQuizAttemptItems(conn, attemptId);
                            
                            Quiz quiz = new Quiz(items, timeStarted);
                            quiz.setId(attemptId);
                            quiz.setTimeEnded(timeEnded);
                            quiz.setPoints(points);
                            quiz.setMaxPoints(maxPoints);
                            
                            // Setze Name, falls Template vorhanden
                            long templateId = rs.getLong("quiz_template_id");
                            if (!rs.wasNull() && templateId > 0) {
                                Quiz template = findQuizTemplateById(templateId);
                                if (template != null) {
                                    quiz.setName(template.getName());
                                }
                            }
                            
                            quizzes.add(quiz);
                        }
                    }
                }
                
                logger.info("✅ {} Quiz-Ergebnisse für Schüler '{}' aus Datenbank geladen", 
                           quizzes.size(), schueler.getUsername());
            } finally {
                DatabaseManager.returnConnection(conn);
            }
        } catch (Exception e) {
            logger.error("❌ Fehler beim Laden von Quiz-Ergebnissen für Schüler '{}': {}", 
                        schueler.getUsername(), e.getMessage(), e);
        }
        
        return quizzes.toArray(new Quiz[0]);
    }
    
    /**
     * Lädt Items für einen Quiz-Attempt aus der Datenbank.
     */
    private static FachbegriffItem[] loadQuizAttemptItems(Connection conn, long attemptId) throws SQLException {
        String sql = """
            SELECT fachbegriff_item_id, user_word, points_earned, position
            FROM quiz_attempt_items
            WHERE quiz_attempt_id = ?
            ORDER BY position
            """;
        
        java.util.List<FachbegriffItem> items = new java.util.ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, attemptId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long fachbegriffId = rs.getLong("fachbegriff_item_id");
                    String userWord = rs.getString("user_word");
                    int pointsEarned = rs.getInt("points_earned");
                    
                    // Hole das FachbegriffItem aus dem Server-Array
                    FachbegriffItem item = findFachbegriffById(fachbegriffId);
                    if (item != null) {
                        // Erstelle eine Kopie mit User-Antwort
                        FachbegriffItem userItem = new FachbegriffItem(
                            item.getWord(),
                            item.getLevel(),
                            pointsEarned,
                            item.getMaxPoints(),
                            item.getPhrase()
                        );
                        userItem.setId(item.getId());
                        userItem.setUserWord(userWord);
                        items.add(userItem);
                    }
                }
            }
        }
        
        return items.toArray(new FachbegriffItem[0]);
    }
}
