package at.tgm.server;

import at.tgm.objects.Lehrer;
import at.tgm.objects.NutzerStatus;
import at.tgm.objects.Nutzer;
import at.tgm.objects.Schueler;

public class Server {

    public static Nutzer[] nutzers;
    public static void main(String[] args) {

        int port = 5123;

        nutzers =  new Nutzer[1];

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


        Lehrer l = new Lehrer("l","123");
        addNutzer(l);

        ServerNetworkController.start(port);
    }
    public static Nutzer findNutzerByUsername(String username){
        if (username == null) {
            return null;
        }

        for (Nutzer n : Server.nutzers){
            if (n != null && username.equals(n.getUsername())) {
                return n;
            }
        }
        return null;
    }

    public static void addNutzer(Nutzer nutzer){
        if (nutzer == null) {
            throw new IllegalArgumentException("Nutzer darf nicht null sein");
        }

        // Suche nach freiem Platz im Array
        for (int i = 0; i < nutzers.length; i++) {
            if (nutzers[i] == null) {
                nutzers[i] = nutzer;
                return;
            }
        }

        // Kein freier Platz gefunden - Array vergrößern
        Nutzer[] nutzersNeu = new Nutzer[nutzers.length + 1];
        System.arraycopy(nutzers, 0, nutzersNeu, 0, nutzers.length);
        nutzersNeu[nutzers.length] = nutzer;
        nutzers = nutzersNeu;
    }
}
