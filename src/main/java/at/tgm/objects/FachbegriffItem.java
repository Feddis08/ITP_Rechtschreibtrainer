package at.tgm.objects;

public class FachbegriffItem extends SendableObject{

    private String word; // der Fachbegriff zum wissen
    private int level;
    private int points;
    private  String phrase; // komplette phrase

    public FachbegriffItem(String word, int level, int points, String phrase) {
        this.word = word;
        this.level = level;
        this.points = points;
        this.phrase = phrase;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public int getLevel() {
        return level;
    }

    public int getPoints() {
        return points;
    }

    public String getPhrase() {
        return phrase;
    }


    public FachbegriffItem buildCensoredItem(){
        return new FachbegriffItem(null, this.level, this.points, this.phrase);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
}
