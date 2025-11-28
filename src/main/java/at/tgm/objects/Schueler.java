package at.tgm.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Schueler extends Nutzer{

    private String schoolClass;



    public Schueler() {
        super();
    }

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
