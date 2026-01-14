package at.tgm.objects;

import at.tgm.server.Server;

public class Quiz extends SendableObject{

    private long id; // Eindeutige ID für Quiz-Templates (Timestamp)
    private String name; // Name des Quiz-Templates (zur Identifikation)
    private FachbegriffItem[] items;
    private FachbegriffItem[] userItems;
    private long timeStarted;
    private long timeEnded;

    private int points;
    private int maxPoints;

    public Quiz(int size, long timeStarted){
        this.id = System.currentTimeMillis(); // Timestamp als ID
        items = new FachbegriffItem[size];
        this.timeStarted = timeStarted;
        getRandomItems(items);
    }

    /**
     * Konstruktor für ein Quiz mit vorgegebenen Items (z.B. aus einem Template).
     * @param items Die FachbegriffItems für das Quiz
     * @param timeStarted Der Zeitpunkt, zu dem das Quiz gestartet wurde
     */
    public Quiz(FachbegriffItem[] items, long timeStarted) {
        this.id = System.currentTimeMillis(); // Timestamp als ID
        this.items = items != null ? items : new FachbegriffItem[0];
        this.timeStarted = timeStarted;
        // Kein getRandomItems() Aufruf - Items sind bereits vorgegeben
    }

    public Quiz(String name, FachbegriffItem[] items) {
        // Konstruktor für Quiz-Templates (ohne timeStarted)
        this.id = System.currentTimeMillis();
        this.name = name;
        this.items = items != null ? items : new FachbegriffItem[0];
        this.timeStarted = 0; // Noch nicht gestartet
    }

    public Quiz(FachbegriffItem[] items) {
        // Konstruktor für Quiz-Templates ohne Name (Legacy)
        this.id = System.currentTimeMillis();
        this.name = null;
        this.items = items != null ? items : new FachbegriffItem[0];
        this.timeStarted = 0; // Noch nicht gestartet
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public FachbegriffItem[] getUserItems() {
        return userItems;
    }

    public void setUserItems(FachbegriffItem[] userItems) {
        this.userItems = userItems;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    public long getTimeEnded() {
        return timeEnded;
    }

    public void setTimeEnded(long timeEnded) {
        this.timeEnded = timeEnded;
    }

    public void getRandomItems(FachbegriffItem[] items){
        if (items == null) {
            throw new IllegalArgumentException("Items-Array darf nicht null sein");
        }
        if (items.length < 10) {
            throw new IllegalArgumentException("Items-Array muss mindestens 10 Elemente haben, hat aber nur " + items.length);
        }

        // Versuche, Items aus Server.fachbegriffe zu wählen
        at.tgm.objects.FachbegriffItem[] availableItems = getAvailableFachbegriffe();
        
        if (availableItems.length >= items.length) {
            // Genug Items vorhanden - zufällige Auswahl
            java.util.List<at.tgm.objects.FachbegriffItem> itemList = new java.util.ArrayList<>(java.util.Arrays.asList(availableItems));
            java.util.Collections.shuffle(itemList);
            
            for (int i = 0; i < items.length; i++) {
                items[i] = itemList.get(i);
            }
        } else {
            // Nicht genug Items - verwende Fallback (hardcoded Items)
            // Kopiere verfügbare Items
            for (int i = 0; i < availableItems.length && i < items.length; i++) {
                items[i] = availableItems[i];
            }
            
            // Fülle Rest mit Fallback-Items
            fillWithFallbackItems(items, availableItems.length);
        }
    }

    private FachbegriffItem[] getAvailableFachbegriffe() {
        // Hole Items aus Server.fachbegriffe
        FachbegriffItem[] serverItems = Server.fachbegriffe;
        if (serverItems != null && serverItems.length > 0) {
            // Filtere null-Einträge
            java.util.List<FachbegriffItem> filtered = new java.util.ArrayList<>();
            for (FachbegriffItem item : serverItems) {
                if (item != null) {
                    filtered.add(item);
                }
            }
            return filtered.toArray(new FachbegriffItem[0]);
        }
        
        // Fallback: leeres Array
        return new FachbegriffItem[0];
    }

    private void fillWithFallbackItems(FachbegriffItem[] items, int startIndex) {
        // Fallback-Items (hardcoded)
        FachbegriffItem[] fallback = new FachbegriffItem[]{
            new FachbegriffItem("IDE", 1, 1, "Eine integrierte Entwicklungsumgebung, mit der man Code schreibt, testet und debuggt."),
            new FachbegriffItem("Compiler", 1, 2, "Ein Programm, das Quellcode in Maschinensprache übersetzt."),
            new FachbegriffItem("Interpreter", 1, 2, "Ein Programm, das Quellcode Zeile für Zeile ausführt."),
            new FachbegriffItem("Algorithmus", 1, 1, "Eine eindeutige Abfolge von Schritten zur Lösung eines Problems."),
            new FachbegriffItem("Variable", 1, 1, "Ein Speicherplatz, der einen veränderlichen Wert enthält."),
            new FachbegriffItem("Array", 1, 1, "Eine Datenstruktur, die mehrere Werte desselben Typs speichert."),
            new FachbegriffItem("Klasse", 1, 2, "Ein Bauplan für Objekte, der Attribute und Methoden definiert."),
            new FachbegriffItem("Objekt", 1, 1, "Eine Instanz einer Klasse mit konkreten Werten."),
            new FachbegriffItem("Konstruktor", 1, 2, "Eine spezielle Methode, die beim Erstellen eines Objekts aufgerufen wird."),
            new FachbegriffItem("Datenbank", 1, 2, "Ein System zur strukturierten Speicherung und Abfrage großer Datenmengen.")
        };

        int fallbackIndex = 0;
        for (int i = startIndex; i < items.length; i++) {
            if (fallbackIndex < fallback.length) {
                items[i] = fallback[fallbackIndex++];
            } else {
                // Wenn nicht genug Fallback-Items vorhanden, wiederhole
                items[i] = fallback[fallbackIndex % fallback.length];
                fallbackIndex++;
            }
        }
    }

    public FachbegriffItem[] getCensoredItems(){
        if (items == null) {
            return new FachbegriffItem[0];
        }

        FachbegriffItem[] fs = new FachbegriffItem[items.length];
        int i = 0;
        for (FachbegriffItem f : items){
            if (f != null) {
                fs[i] = f.buildCensoredItem();
            }
            i++;
        }
        return fs;
    }


    public FachbegriffItem[] getItems() {
        return items;
    }

    public void setItems(FachbegriffItem[] items) {
        this.items = items;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
