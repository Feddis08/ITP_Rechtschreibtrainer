package at.tgm.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Schueler extends Nutzer{

    private String schoolClass;

    public Schueler(String username, String password) {
        super(username, password);
    }
    public Schueler(DataInputStream in, boolean includeSensitive) throws IOException {
        super(in, includeSensitive);

        this.schoolClass = in.readUTF();

    }

    public String getSchoolClass() {
        return schoolClass;
    }
    @Override
    public void encode(DataOutputStream out, boolean includeSensitive) throws IOException {
        super.encode(out, includeSensitive);

        out.writeUTF(getSchoolClass());
    }
    public void setSchoolClass(String schoolClass) {
        this.schoolClass = schoolClass;
    }
}
