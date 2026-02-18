package at.tgm.objects;

import at.tgm.server.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Basis-Klasse für alle Objekte, die sich in einer Datenbank speichern können.
 * Analog zu SendableObject, aber für Datenbank-Persistierung.
 * 
 * Jedes Objekt kann sich selbst speichern und laden, ohne Connection-Parameter zu benötigen.
 * Der DatabaseManager kümmert sich um Connection Pooling.
 */
public abstract class SavableObject extends SendableObject {

    private static final Logger logger = LoggerFactory.getLogger(SavableObject.class);

    /**
     * Speichert dieses Objekt in der Datenbank.
     * Implementiert INSERT oder UPDATE je nachdem, ob das Objekt bereits existiert.
     * 
     * @throws SQLException bei Datenbank-Fehlern
     */
    public void save() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try {
            save(conn);
            conn.commit();
            logger.debug("Objekt {} (ID: {}) erfolgreich gespeichert", 
                        getClass().getSimpleName(), getId());
        } catch (SQLException e) {
            conn.rollback();
            logger.error("Fehler beim Speichern von {} (ID: {}): {}", 
                        getClass().getSimpleName(), getId(), e.getMessage());
            throw e;
        } finally {
            DatabaseManager.returnConnection(conn);
        }
    }

    /**
     * Speichert dieses Objekt mit der gegebenen Connection.
     * Wird intern von save() verwendet, kann aber auch für Transaktionen verwendet werden.
     * 
     * @param conn Die Datenbank-Connection
     * @throws SQLException bei Datenbank-Fehlern
     */
    public abstract void save(Connection conn) throws SQLException;

    /**
     * Lädt ein Objekt aus der Datenbank anhand seiner ID.
     * 
     * @param clazz Die Klasse des zu ladenden Objekts
     * @param id Die ID des Objekts
     * @param <T> Der Typ des Objekts
     * @return Das geladene Objekt oder null, wenn nicht gefunden
     * @throws SQLException bei Datenbank-Fehlern
     */
    public static <T extends SavableObject> T load(Class<T> clazz, long id) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try {
            T obj = load(conn, clazz, id);
            logger.debug("Objekt {} (ID: {}) geladen", clazz.getSimpleName(), id);
            return obj;
        } finally {
            DatabaseManager.returnConnection(conn);
        }
    }

    /**
     * Lädt ein Objekt aus der Datenbank mit der gegebenen Connection.
     * Wird intern von load() verwendet, kann aber auch für Transaktionen verwendet werden.
     * 
     * @param conn Die Datenbank-Connection
     * @param clazz Die Klasse des zu ladenden Objekts
     * @param id Die ID des Objekts
     * @param <T> Der Typ des Objekts
     * @return Das geladene Objekt oder null, wenn nicht gefunden
     * @throws SQLException bei Datenbank-Fehlern
     */
    public static <T extends SavableObject> T load(Connection conn, Class<T> clazz, long id) throws SQLException {
        // Diese Methode muss von jeder Subklasse implementiert werden
        // Da wir keine Reflection verwenden wollen, wird sie in den Subklassen überschrieben
        throw new UnsupportedOperationException(
            "load() muss in der Subklasse " + clazz.getSimpleName() + " implementiert werden");
    }

    /**
     * Aktualisiert dieses Objekt in der Datenbank.
     * Standard-Implementierung ruft einfach save() auf (INSERT ... ON DUPLICATE KEY UPDATE).
     * Kann in Subklassen überschrieben werden für spezielle Update-Logik.
     * 
     * @throws SQLException bei Datenbank-Fehlern
     */
    public void update() throws SQLException {
        save(); // Standard: save() macht INSERT oder UPDATE
    }

    /**
     * Aktualisiert dieses Objekt mit der gegebenen Connection.
     * 
     * @param conn Die Datenbank-Connection
     * @throws SQLException bei Datenbank-Fehlern
     */
    public void update(Connection conn) throws SQLException {
        save(conn); // Standard: save() macht INSERT oder UPDATE
    }

    /**
     * Löscht dieses Objekt aus der Datenbank.
     * 
     * @throws SQLException bei Datenbank-Fehlern
     */
    public void delete() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try {
            delete(conn);
            conn.commit();
            logger.debug("Objekt {} (ID: {}) erfolgreich gelöscht", 
                        getClass().getSimpleName(), getId());
        } catch (SQLException e) {
            conn.rollback();
            logger.error("Fehler beim Löschen von {} (ID: {}): {}", 
                        getClass().getSimpleName(), getId(), e.getMessage());
            throw e;
        } finally {
            DatabaseManager.returnConnection(conn);
        }
    }

    /**
     * Löscht dieses Objekt mit der gegebenen Connection.
     * 
     * @param conn Die Datenbank-Connection
     * @throws SQLException bei Datenbank-Fehlern
     */
    public abstract void delete(Connection conn) throws SQLException;

    /**
     * Gibt die ID dieses Objekts zurück.
     * Muss von jeder Subklasse implementiert werden.
     * 
     * @return Die ID des Objekts
     */
    public abstract long getId();

    /**
     * Prüft, ob dieses Objekt bereits in der Datenbank existiert.
     * 
     * @return true, wenn das Objekt existiert, false sonst
     * @throws SQLException bei Datenbank-Fehlern
     */
    public boolean exists() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try {
            return exists(conn);
        } finally {
            DatabaseManager.returnConnection(conn);
        }
    }

    /**
     * Prüft, ob dieses Objekt mit der gegebenen Connection existiert.
     * 
     * @param conn Die Datenbank-Connection
     * @return true, wenn das Objekt existiert, false sonst
     * @throws SQLException bei Datenbank-Fehlern
     */
    public abstract boolean exists(Connection conn) throws SQLException;
}
