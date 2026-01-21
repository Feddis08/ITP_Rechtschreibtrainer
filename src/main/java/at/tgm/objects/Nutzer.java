package at.tgm.objects;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public abstract class Nutzer extends SendableObject{

    private static final Logger logger = LoggerFactory.getLogger(Nutzer.class);
    
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private final String username;
    private String passwordHash; // Gespeichertes Hash statt Klartext-Passwort

    private String beschreibung;
    private int age;

    private String displayName;
    private NutzerStatus status;

    private final long createdAt;
    private final String uuid;
    private String email;
    private String profilePictureUrl;
    private long lastLoginTimestamp;
    private boolean isDeactivated;

    public Nutzer() {
        this.username = "";
        this.passwordHash = "";
        this.createdAt = System.currentTimeMillis();
        this.uuid = UUID.randomUUID().toString();
        this.status = NutzerStatus.OFFLINE;
        this.isDeactivated = false;
    }
    
    public Nutzer(String username, String password){
        this.username = username;
        // Hashe das Passwort beim Erstellen
        this.passwordHash = hashPassword(password);
        this.createdAt = System.currentTimeMillis();
        this.status = NutzerStatus.OFFLINE;
        this.uuid = String.valueOf(UUID.randomUUID());
        this.isDeactivated = false;
    }
    
    /**
     * Konstruktor für das Laden aus der Datenbank, wo bereits ein Hash vorhanden ist.
     * @param username Der Benutzername
     * @param passwordHash Das bereits gehashte Passwort (aus der DB)
     * @param fromDatabase Flag, ob dies aus der DB geladen wurde
     */
    public Nutzer(String username, String passwordHash, boolean fromDatabase) {
        if (!fromDatabase) {
            throw new IllegalArgumentException("Verwende den normalen Konstruktor für neue Nutzer");
        }
        this.username = username;
        this.passwordHash = passwordHash != null ? passwordHash : "";
        this.createdAt = System.currentTimeMillis();
        this.status = NutzerStatus.OFFLINE;
        this.uuid = String.valueOf(UUID.randomUUID());
        this.isDeactivated = false;
    }

    /**
     * Hasht ein Klartext-Passwort mit BCrypt.
     * @param plainPassword Das Klartext-Passwort
     * @return Der BCrypt-Hash
     */
    private static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return "";
        }
        // BCrypt mit Cost-Faktor 12 (balance zwischen Sicherheit und Performance)
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Prüft, ob das übergebene Klartext-Passwort mit dem gespeicherten Hash übereinstimmt.
     * @param plainPassword Das zu prüfende Klartext-Passwort
     * @return true, wenn das Passwort korrekt ist
     */
    public boolean checkPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty() || passwordHash == null || passwordHash.isEmpty()) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, passwordHash);
        } catch (Exception e) {
            logger.error("Fehler beim Prüfen des Passworts für Nutzer '{}': {}", username, e.getMessage());
            return false;
        }
    }
    
    /**
     * Setzt ein neues Passwort (wird automatisch gehasht).
     * @param plainPassword Das neue Klartext-Passwort
     */
    public void setPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            this.passwordHash = "";
        } else {
            this.passwordHash = hashPassword(plainPassword);
        }
    }
    
    @Override
    public boolean equals(Object o){
        if (o == null || o.getClass() != this.getClass()) return false;
        Nutzer n = (Nutzer) o;
        // Vergleiche nur Username, nicht Passwort (Passwörter sollten nicht in equals() verwendet werden)
        return n.getUsername().equals(getUsername());
    }

    /**
     * Gibt das Passwort-Hash zurück (nur für interne Verwendung und Datenbank-Persistierung).
     * @return Das gehashte Passwort
     */
    public String getPasswordHash() {
        return passwordHash;
    }
    
    /**
     * @deprecated Verwende getPasswordHash() stattdessen. Diese Methode wird aus Kompatibilitätsgründen beibehalten.
     * @return Das Passwort-Hash
     */
    @Deprecated
    public String getPassword() {
        logger.warn("getPassword() wurde aufgerufen - sollte getPasswordHash() verwenden");
        return passwordHash;
    }

    public String getUsername() {
        return username;
    }


    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public NutzerStatus getStatus() {
        return status;
    }

    public void setStatus(NutzerStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getUuid() {
        return uuid;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public long getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    public void setLastLoginTimestamp(long lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isDeactivated() {
        return isDeactivated;
    }

    public void setDeactivated(boolean deactivated) {
        isDeactivated = deactivated;
    }
}
