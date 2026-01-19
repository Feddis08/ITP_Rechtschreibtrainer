package at.tgm.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Verwaltet das Datenbank-Schema.
 * Erstellt alle notwendigen Tabellen beim ersten Start.
 */
public class DatabaseSchema {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchema.class);

    /**
     * Erstellt alle Tabellen, falls sie noch nicht existieren.
     * Sollte beim Server-Start aufgerufen werden, nachdem DatabaseManager initialisiert wurde.
     * 
     * @throws SQLException bei Fehlern beim Erstellen der Tabellen
     */
    public static void createTables() throws SQLException {
        logger.info("Erstelle Datenbank-Schema...");
        
        Connection conn = DatabaseManager.getConnection();
        try {
            // Erstelle Tabellen in der richtigen Reihenfolge (wegen Foreign Keys)
            createFachbegriffItemTable(conn);
            createNutzerTable(conn);
            createSchuelerTable(conn);
            createLehrerTable(conn);
            createSysAdminTable(conn);
            createQuizTemplateTable(conn);
            createQuizTemplateItemsTable(conn);
            createQuizAttemptTable(conn);
            createQuizAttemptItemsTable(conn);
            
            conn.commit();
            logger.info("✅ Datenbank-Schema erfolgreich erstellt");
        } catch (SQLException e) {
            conn.rollback();
            logger.error("Fehler beim Erstellen des Datenbank-Schemas: {}", e.getMessage(), e);
            throw e;
        } finally {
            DatabaseManager.returnConnection(conn);
        }
    }

    /**
     * Erstellt die Tabelle für FachbegriffItem.
     */
    private static void createFachbegriffItemTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS fachbegriff_item (
                id BIGINT PRIMARY KEY,
                word VARCHAR(255) NOT NULL,
                level INT NOT NULL,
                points INT NOT NULL,
                max_points INT NOT NULL,
                phrase TEXT,
                INDEX idx_word (word),
                INDEX idx_level (level)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'fachbegriff_item' erstellt/überprüft");
        }
    }

    /**
     * Erstellt die Tabelle für Nutzer (Basis-Tabelle).
     */
    private static void createNutzerTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS nutzer (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                uuid VARCHAR(36) UNIQUE NOT NULL,
                username VARCHAR(255) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                type ENUM('SCHUELER', 'LEHRER', 'SYSADMIN') NOT NULL,
                first_name VARCHAR(255),
                last_name VARCHAR(255),
                email VARCHAR(255),
                phone_number VARCHAR(50),
                age INT,
                display_name VARCHAR(255),
                beschreibung TEXT,
                status ENUM('ONLINE', 'OFFLINE', 'BUSY', 'AWAY', 'BANNED') DEFAULT 'OFFLINE',
                profile_picture_url VARCHAR(512),
                created_at BIGINT NOT NULL,
                last_login_timestamp BIGINT DEFAULT 0,
                is_deactivated BOOLEAN DEFAULT FALSE,
                INDEX idx_username (username),
                INDEX idx_uuid (uuid),
                INDEX idx_type (type)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'nutzer' erstellt/überprüft");
        }
    }

    /**
     * Erstellt die Tabelle für Schueler (erweitert Nutzer).
     */
    private static void createSchuelerTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS schueler (
                nutzer_id BIGINT PRIMARY KEY,
                school_class VARCHAR(50),
                FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE,
                INDEX idx_school_class (school_class)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'schueler' erstellt/überprüft");
        }
    }

    /**
     * Erstellt die Tabelle für Lehrer (erweitert Nutzer).
     */
    private static void createLehrerTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS lehrer (
                nutzer_id BIGINT PRIMARY KEY,
                FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'lehrer' erstellt/überprüft");
        }
    }

    /**
     * Erstellt die Tabelle für SysAdmin (erweitert Nutzer).
     */
    private static void createSysAdminTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS sysadmin (
                nutzer_id BIGINT PRIMARY KEY,
                FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'sysadmin' erstellt/überprüft");
        }
    }

    /**
     * Erstellt die Tabelle für Quiz-Templates.
     */
    private static void createQuizTemplateTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS quiz_template (
                id BIGINT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                created_at BIGINT NOT NULL,
                INDEX idx_name (name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'quiz_template' erstellt/überprüft");
        }
    }

    /**
     * Erstellt die Junction-Tabelle für Quiz-Template Items (Many-to-Many).
     */
    private static void createQuizTemplateItemsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS quiz_template_items (
                quiz_template_id BIGINT NOT NULL,
                fachbegriff_item_id BIGINT NOT NULL,
                position INT NOT NULL,
                PRIMARY KEY (quiz_template_id, fachbegriff_item_id, position),
                FOREIGN KEY (quiz_template_id) REFERENCES quiz_template(id) ON DELETE CASCADE,
                FOREIGN KEY (fachbegriff_item_id) REFERENCES fachbegriff_item(id) ON DELETE CASCADE,
                INDEX idx_quiz_template (quiz_template_id),
                INDEX idx_fachbegriff_item (fachbegriff_item_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'quiz_template_items' erstellt/überprüft");
        }
    }

    /**
     * Erstellt die Tabelle für Quiz-Durchläufe (Schüler-Quiz-Versuche).
     */
    private static void createQuizAttemptTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS quiz_attempt (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                schueler_id BIGINT NOT NULL,
                quiz_template_id BIGINT,
                time_started BIGINT NOT NULL,
                time_ended BIGINT,
                points INT DEFAULT 0,
                max_points INT DEFAULT 0,
                FOREIGN KEY (schueler_id) REFERENCES schueler(nutzer_id) ON DELETE CASCADE,
                FOREIGN KEY (quiz_template_id) REFERENCES quiz_template(id) ON DELETE SET NULL,
                INDEX idx_schueler (schueler_id),
                INDEX idx_quiz_template (quiz_template_id),
                INDEX idx_time_started (time_started)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'quiz_attempt' erstellt/überprüft");
        }
    }

    /**
     * Erstellt die Tabelle für Quiz-Durchlauf-Items (Antworten pro Quiz).
     */
    private static void createQuizAttemptItemsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS quiz_attempt_items (
                quiz_attempt_id BIGINT NOT NULL,
                fachbegriff_item_id BIGINT NOT NULL,
                user_word VARCHAR(255),
                points_earned INT DEFAULT 0,
                position INT NOT NULL,
                PRIMARY KEY (quiz_attempt_id, position),
                FOREIGN KEY (quiz_attempt_id) REFERENCES quiz_attempt(id) ON DELETE CASCADE,
                FOREIGN KEY (fachbegriff_item_id) REFERENCES fachbegriff_item(id) ON DELETE CASCADE,
                INDEX idx_quiz_attempt (quiz_attempt_id),
                INDEX idx_fachbegriff_item (fachbegriff_item_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("Tabelle 'quiz_attempt_items' erstellt/überprüft");
        }
    }
}
