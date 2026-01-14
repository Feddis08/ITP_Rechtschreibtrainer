package at.tgm.objects;

public class FachbegriffItem extends SendableObject{

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
}
