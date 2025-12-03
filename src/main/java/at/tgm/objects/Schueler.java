package at.tgm.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Schueler extends Nutzer{

    private String schoolClass;


    private Quiz quiz;
    private Quiz[] quizzes;

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
}
