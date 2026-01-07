package at.tgm.server;

import at.tgm.objects.Lehrer;
import at.tgm.objects.NutzerStatus;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Schueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static Nutzer[] nutzers;
    public static void main(String[] args) {
        logger.info("Server wird gestartet...");

        int port = 5123;

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
}
