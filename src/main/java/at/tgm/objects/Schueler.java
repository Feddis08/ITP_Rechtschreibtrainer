package at.tgm.objects;

public class Schueler extends Nutzer{

    private String schoolClass;

    public Schueler(String username, String password) {
        super(username, password);
    }

    public String getSchoolClass() {
        return schoolClass;
    }

    public void setSchoolClass(String schoolClass) {
        this.schoolClass = schoolClass;
    }
}
