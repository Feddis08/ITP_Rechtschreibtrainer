package at.tgm.objects;

public class NutzerTyp {
    private String name;
    private String beschreibung;

    public NutzerTyp(String name, String beschreibung) {
        this.name = name;
        this.beschreibung = beschreibung;
    }

    public String getName() {
        return name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }
}
