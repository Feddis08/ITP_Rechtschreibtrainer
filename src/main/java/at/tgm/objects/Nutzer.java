package at.tgm.objects;

public class Nutzer {
    private String username;
    private String password;

    private NutzerTyp nutzerTyp;

    public Nutzer (String username, String password){
        this.username = username;
        this.password = password;
    }

    public boolean checkPassword(String password){
        return this.password.equals(password);
    }
    @Override
    public boolean equals(Object o){
        if (o == null || o.getClass() != this.getClass()) return false;
        Nutzer n = (Nutzer) o;

        return n.getPassword().equals(getPassword()) && n.getUsername().equals(getUsername());
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
