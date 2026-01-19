package at.tgm.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton für Datenbank-Verbindungsverwaltung.
 * Verwaltet einen Connection Pool (HikariCP) und stellt Connections bereit.
 * 
 * Konfiguration über database.properties Datei.
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    
    private static DatabaseManager instance;
    private HikariDataSource dataSource;
    private boolean initialized = false;

    private DatabaseManager() {
        // Private Konstruktor für Singleton
    }

    /**
     * Gibt die Singleton-Instanz zurück.
     * 
     * @return Die DatabaseManager-Instanz
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initialisiert den DatabaseManager mit der Konfiguration aus database.properties.
     * Muss vor der ersten Verwendung aufgerufen werden.
     * 
     * @throws SQLException bei Fehlern beim Verbindungsaufbau
     */
    public synchronized void initialize() throws SQLException {
        if (initialized) {
            logger.warn("DatabaseManager wurde bereits initialisiert");
            return;
        }

        logger.info("Initialisiere DatabaseManager...");

        try {
            // Lade Konfiguration
            Properties props = loadProperties();
            
            String dbUrl = props.getProperty("db.url", "jdbc:mysql://localhost:3306/rechtschreibtrainer");
            String dbUser = props.getProperty("db.user", "root");
            String dbPassword = props.getProperty("db.password", "");
            
            // Stelle sicher, dass die Datenbank existiert
            ensureDatabaseExists(dbUrl, dbUser, dbPassword);
            
            // Erstelle HikariCP Config
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Connection Pool Einstellungen
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
            
            // Connection Test
            config.setConnectionTestQuery("SELECT 1");
            
            // Erstelle DataSource
            dataSource = new HikariDataSource(config);
            
            // Teste Verbindung
            try (Connection testConn = dataSource.getConnection()) {
                logger.info("✅ Datenbank-Verbindung erfolgreich hergestellt");
                logger.info("   URL: {}", config.getJdbcUrl());
                logger.info("   Pool-Größe: {}", config.getMaximumPoolSize());
            }
            
            initialized = true;
            logger.info("DatabaseManager erfolgreich initialisiert");
            
        } catch (Exception e) {
            logger.error("Fehler beim Initialisieren des DatabaseManagers: {}", e.getMessage(), e);
            throw new SQLException("DatabaseManager konnte nicht initialisiert werden", e);
        }
    }

    /**
     * Stellt sicher, dass die Datenbank existiert. Erstellt sie, falls sie nicht existiert.
     * 
     * @param dbUrl Die vollständige JDBC URL mit Datenbankname
     * @param dbUser Der Datenbank-Benutzer
     * @param dbPassword Das Datenbank-Passwort
     * @throws SQLException bei Fehlern beim Erstellen der Datenbank
     */
    private void ensureDatabaseExists(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        // Parse die URL, um Host, Port und Datenbankname zu extrahieren
        // Format: jdbc:mysql://host:port/database
        String databaseName;
        String serverUrl;
        
        try {
            // Entferne "jdbc:mysql://" Präfix
            String urlWithoutPrefix = dbUrl.substring("jdbc:mysql://".length());
            
            // Finde den letzten "/" um Datenbankname zu extrahieren
            int lastSlash = urlWithoutPrefix.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new IllegalArgumentException("Ungültige JDBC URL: Kein Datenbankname gefunden");
            }
            
            databaseName = urlWithoutPrefix.substring(lastSlash + 1);
            serverUrl = "jdbc:mysql://" + urlWithoutPrefix.substring(0, lastSlash);
            
            // Entferne Query-Parameter vom Datenbanknamen (falls vorhanden)
            int questionMark = databaseName.indexOf('?');
            if (questionMark != -1) {
                databaseName = databaseName.substring(0, questionMark);
            }
            
            logger.debug("Datenbankname: {}, Server-URL: {}", databaseName, serverUrl);
            
        } catch (Exception e) {
            logger.error("Fehler beim Parsen der JDBC URL: {}", dbUrl);
            throw new SQLException("Ungültige JDBC URL: " + dbUrl, e);
        }
        
        // Verbinde zum MySQL-Server OHNE Datenbank
        try (Connection serverConn = java.sql.DriverManager.getConnection(serverUrl, dbUser, dbPassword)) {
            logger.debug("Verbunden zum MySQL-Server, prüfe ob Datenbank '{}' existiert...", databaseName);
            
            // Prüfe, ob die Datenbank existiert
            boolean dbExists = false;
            try (java.sql.Statement stmt = serverConn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + databaseName + "'")) {
                dbExists = rs.next();
            }
            
            if (dbExists) {
                logger.info("✅ Datenbank '{}' existiert bereits", databaseName);
            } else {
                logger.info("Datenbank '{}' existiert nicht, erstelle sie...", databaseName);
                
                // Erstelle die Datenbank
                try (java.sql.Statement stmt = serverConn.createStatement()) {
                    // Verwende CREATE DATABASE IF NOT EXISTS für Sicherheit
                    String createDbSql = "CREATE DATABASE IF NOT EXISTS `" + databaseName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                    stmt.executeUpdate(createDbSql);
                    logger.info("✅ Datenbank '{}' erfolgreich erstellt", databaseName);
                }
            }
        } catch (SQLException e) {
            logger.error("Fehler beim Erstellen/Prüfen der Datenbank '{}': {}", databaseName, e.getMessage());
            throw new SQLException("Konnte Datenbank '" + databaseName + "' nicht erstellen oder prüfen", e);
        }
    }

    /**
     * Lädt die Datenbank-Konfiguration aus database.properties.
     * 
     * @return Properties-Objekt mit der Konfiguration
     * @throws IOException wenn die Datei nicht gefunden werden kann
     */
    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        
        // Versuche database.properties aus resources zu laden
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (is != null) {
                props.load(is);
                logger.info("Konfiguration aus database.properties geladen");
            } else {
                logger.warn("database.properties nicht gefunden, verwende Standard-Werte");
                // Setze Standard-Werte
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/rechtschreibtrainer");
                props.setProperty("db.user", "root");
                props.setProperty("db.password", "");
                props.setProperty("db.pool.size", "10");
                props.setProperty("db.pool.minIdle", "5");
            }
        }
        
        // Environment Variables überschreiben Properties (für Production)
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null) {
            props.setProperty("db.url", dbUrl);
            logger.info("DB_URL aus Environment Variable verwendet");
        }
        
        String dbUser = System.getenv("DB_USER");
        if (dbUser != null) {
            props.setProperty("db.user", dbUser);
            logger.info("DB_USER aus Environment Variable verwendet");
        }
        
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword != null) {
            props.setProperty("db.password", dbPassword);
            logger.info("DB_PASSWORD aus Environment Variable verwendet");
        }
        
        return props;
    }

    /**
     * Gibt eine Connection aus dem Pool zurück.
     * Die Connection muss mit returnConnection() zurückgegeben werden.
     * 
     * @return Eine Datenbank-Connection
     * @throws SQLException wenn keine Connection verfügbar ist
     */
    public static Connection getConnection() throws SQLException {
        DatabaseManager manager = getInstance();
        if (!manager.initialized) {
            throw new SQLException("DatabaseManager wurde noch nicht initialisiert. Rufe DatabaseManager.getInstance().initialize() auf.");
        }
        
        Connection conn = manager.dataSource.getConnection();
        conn.setAutoCommit(false); // Transaktionen explizit verwalten
        return conn;
    }

    /**
     * Gibt eine Connection an den Pool zurück.
     * Sollte immer in einem finally-Block aufgerufen werden.
     * 
     * @param conn Die zurückzugebende Connection
     */
    public static void returnConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.setAutoCommit(true); // Reset für nächste Verwendung
                }
                conn.close(); // HikariCP gibt die Connection automatisch an den Pool zurück
            } catch (SQLException e) {
                logger.error("Fehler beim Zurückgeben der Connection: {}", e.getMessage());
            }
        }
    }

    /**
     * Schließt den DatabaseManager und gibt alle Ressourcen frei.
     * Sollte beim Server-Shutdown aufgerufen werden.
     */
    public synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Schließe DatabaseManager...");
            dataSource.close();
            initialized = false;
            logger.info("DatabaseManager geschlossen");
        }
    }

    /**
     * Prüft, ob der DatabaseManager initialisiert ist.
     * 
     * @return true, wenn initialisiert, false sonst
     */
    public boolean isInitialized() {
        return initialized;
    }
}
