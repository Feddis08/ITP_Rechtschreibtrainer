package at.tgm.objects;

import at.tgm.server.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FachbegriffItem extends SavableObject{
    
    private static final Logger logger = LoggerFactory.getLogger(FachbegriffItem.class);

    private long id; // Eindeutige ID (Timestamp)
    private String word; // der Fachbegriff zum wissen
    private String userWord; // die eingegebene Antwort des Schülers
    private int level;
    private int points; // erreichte Punkte
    private int maxPoints; // maximale Punkte
    private  String phrase; // komplette phrase

    public FachbegriffItem(String word, int level, int points, String phrase) {
        this.id = System.currentTimeMillis(); // Timestamp als ID
        this.word = word;
        this.level = level;
        this.points = points;
        this.maxPoints = points; // Standard: maxPoints = points beim Erstellen
        this.phrase = phrase;
    }

    public FachbegriffItem(String word, int level, int points, int maxPoints, String phrase) {
        this.id = System.currentTimeMillis(); // Timestamp als ID
        this.word = word;
        this.level = level;
        this.points = points;
        this.maxPoints = maxPoints;
        this.phrase = phrase;
    }

    public FachbegriffItem(long id, String word, int level, int points, int maxPoints, String phrase) {
        this.id = id;
        this.word = word;
        this.level = level;
        this.points = points;
        this.maxPoints = maxPoints;
        this.phrase = phrase;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public String getUserWord() {
        return userWord;
    }

    public void setUserWord(String userWord) {
        this.userWord = userWord;
    }

    public int getLevel() {
        return level;
    }

    public int getPoints() {
        return points;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public String getPhrase() {
        return phrase;
    }


    public FachbegriffItem buildCensoredItem(){
        return new FachbegriffItem(this.id, null, this.level, this.points, this.maxPoints, this.phrase);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // ======================================================
    // SavableObject Implementation
    // ======================================================

    @Override
    public void save(Connection conn) throws SQLException {
        // INSERT ... ON DUPLICATE KEY UPDATE für Upsert-Verhalten
        String sql = """
            INSERT INTO fachbegriff_item (id, word, level, points, max_points, phrase)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                word = VALUES(word),
                level = VALUES(level),
                points = VALUES(points),
                max_points = VALUES(max_points),
                phrase = VALUES(phrase)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, this.id);
            stmt.setString(2, this.word);
            stmt.setInt(3, this.level);
            stmt.setInt(4, this.points);
            stmt.setInt(5, this.maxPoints);
            stmt.setString(6, this.phrase);
            
            stmt.executeUpdate();
            logger.debug("FachbegriffItem '{}' (ID: {}) gespeichert", this.word, this.id);
        }
    }

    @Override
    public void delete(Connection conn) throws SQLException {
        String sql = "DELETE FROM fachbegriff_item WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, this.id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                logger.warn("FachbegriffItem mit ID {} nicht zum Löschen gefunden.", this.id);
            } else {
                logger.debug("FachbegriffItem '{}' (ID: {}) gelöscht", this.word, this.id);
            }
        }
    }

    @Override
    public boolean exists(Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM fachbegriff_item WHERE id = ? LIMIT 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, this.id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Lädt ein FachbegriffItem aus der Datenbank.
     * Statische Methode analog zu SendableObject.decode()
     */
    public static FachbegriffItem load(Connection conn, long id) throws SQLException {
        String sql = "SELECT * FROM fachbegriff_item WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return fromResultSet(rs);
                }
                return null; // Nicht gefunden
            }
        }
    }

    /**
     * Erstellt ein FachbegriffItem aus einem ResultSet.
     */
    private static FachbegriffItem fromResultSet(ResultSet rs) throws SQLException {
        FachbegriffItem item = new FachbegriffItem(
            rs.getLong("id"),
            rs.getString("word"),
            rs.getInt("level"),
            rs.getInt("points"),
            rs.getInt("max_points"),
            rs.getString("phrase")
        );
        return item;
    }

    /**
     * Lädt alle FachbegriffItems aus der Datenbank.
     */
    public static FachbegriffItem[] loadAll() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try {
            String sql = "SELECT * FROM fachbegriff_item ORDER BY id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                java.util.List<FachbegriffItem> items = new java.util.ArrayList<>();
                while (rs.next()) {
                    items.add(fromResultSet(rs));
                }
                
                return items.toArray(new FachbegriffItem[0]);
            }
        } finally {
            DatabaseManager.returnConnection(conn);
        }
    }
}
