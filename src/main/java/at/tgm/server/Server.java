package at.tgm.server;

import at.tgm.network.packets.NutzerStatus;
import at.tgm.objects.Nutzer;

public class Server {

    public static Nutzer[] nutzers;
    public static void main(String[] args) {

        int port = 5123;

        nutzers =  new Nutzer[1];

        Nutzer felix = new Nutzer("riemer", "123");

        felix.setFirstName("Felix");
        felix.setLastName("Riemer");
        felix.setAge(17);

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
        ServerNetworkController.start(port);
    }
    public static Nutzer findNutzerByUsername(String username){

        for (Nutzer n : Server.nutzers){
            if (n != null && n.getUsername().equals(username)) return n;
        }
        return null;
    }

    public static void addNutzer(Nutzer nutzer){
        int i = 0;
        for (Nutzer n : nutzers){
            if (n == null){
                nutzers[i] = nutzer;
                return;
            }
            i ++;
        }

        i++;
        Nutzer[] nutzersNeu = new Nutzer[i];
        i = 0;
        for (Nutzer n : nutzers){
            nutzersNeu[i] = nutzers[i];
            i ++;
        }

        nutzersNeu[i] = nutzer;
        nutzers = nutzersNeu;
    }
}
