package at.tgm.objects;

public class Quiz extends SendableObject{

    private FachbegriffItem[] items;
    private FachbegriffItem[] userItems;
    private long timeStarted;
    private long timeEnded;

    private int points;
    private int maxPoints;

    public Quiz(int size, long timeStarted){
        items = new FachbegriffItem[size];
        this.timeStarted = timeStarted;
        getRandomItems(items);

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

        //some logic

        items[0] = new FachbegriffItem(
                "IDE",
                1,
                1,
                "Eine integrierte Entwicklungsumgebung, mit der man Code schreibt, testet und debuggt."
        );

        items[1] = new FachbegriffItem(
                "Compiler",
                1,
                2,
                "Ein Programm, das Quellcode in Maschinensprache übersetzt."
        );

        items[2] = new FachbegriffItem(
                "Interpreter",
                1,
                2,
                "Ein Programm, das Quellcode Zeile für Zeile ausführt."
        );

        items[3] = new FachbegriffItem(
                "Algorithmus",
                1,
                1,
                "Eine eindeutige Abfolge von Schritten zur Lösung eines Problems."
        );

        items[4] = new FachbegriffItem(
                "Variable",
                1,
                1,
                "Ein Speicherplatz, der einen veränderlichen Wert enthält."
        );

        items[5] = new FachbegriffItem(
                "Array",
                1,
                1,
                "Eine Datenstruktur, die mehrere Werte desselben Typs speichert."
        );

        items[6] = new FachbegriffItem(
                "Klasse",
                1,
                2,
                "Ein Bauplan für Objekte, der Attribute und Methoden definiert."
        );

        items[7] = new FachbegriffItem(
                "Objekt",
                1,
                1,
                "Eine Instanz einer Klasse mit konkreten Werten."
        );

        items[8] = new FachbegriffItem(
                "Konstruktor",
                1,
                2,
                "Eine spezielle Methode, die beim Erstellen eines Objekts aufgerufen wird."
        );

        items[9] = new FachbegriffItem(
                "Datenbank",
                1,
                2,
                "Ein System zur strukturierten Speicherung und Abfrage großer Datenmengen."
        );


        return;
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
}
