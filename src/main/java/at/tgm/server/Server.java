package at.tgm.server;

import at.tgm.client.AnmeldeController;
import at.tgm.objects.Nutzer;

public class Server {

    public static Nutzer[] nutzers;
    public static void main(String[] args) {

        int port = 5123;

        nutzers =  new Nutzer[1];

        addNutzer( new Nutzer("Felix Riemer", "password"));
        addNutzer( new Nutzer("n1", "n1"));
        addNutzer( new Nutzer("n2", "n2"));
        addNutzer( new Nutzer("n3", "n3"));


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
