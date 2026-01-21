package at.tgm.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Schueler extends Nutzer{

    private String schoolClass;

    private Note note;
    private Quiz quiz;
    private Quiz[] quizzes;

    public Schueler() {
        super();
    }

    public Schueler(String username, String password) {
        super(username, password);
    }
    
    /**
     * Konstruktor für das Laden aus der Datenbank mit bereits gehashtem Passwort.
     * @param username Der Benutzername
     * @param passwordHash Das bereits gehashte Passwort
     * @param fromDatabase Flag, ob dies aus der DB geladen wurde
     */
    public Schueler(String username, String passwordHash, boolean fromDatabase) {
        super(username, passwordHash, fromDatabase);
    }

    public String getSchoolClass() {
        return schoolClass;
    }
    public void setSchoolClass(String schoolClass) {
        this.schoolClass = schoolClass;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Quiz[] getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(Quiz[] quizzes) {
        this.quizzes = quizzes;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
