package at.tgm.objects;

public class SysAdmin extends Nutzer {
    public SysAdmin() {
    }

    public SysAdmin(String username, String password) {
        super(username, password);
    }
    
    /**
     * Konstruktor für das Laden aus der Datenbank mit bereits gehashtem Passwort.
     * @param username Der Benutzername
     * @param passwordHash Das bereits gehashte Passwort
     * @param fromDatabase Flag, ob dies aus der DB geladen wurde
     */
    public SysAdmin(String username, String passwordHash, boolean fromDatabase) {
        super(username, passwordHash, fromDatabase);
    }
}
