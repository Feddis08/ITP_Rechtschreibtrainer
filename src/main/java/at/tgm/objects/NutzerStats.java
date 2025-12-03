package at.tgm.objects;

public class NutzerStats extends SendableObject{
    private int quizesApplied;
    private int quizesPassed;
    private int totalTimeInQuizes;

    public NutzerStats() {
        this.quizesApplied = 0;
        this.quizesPassed = 0;
        this.totalTimeInQuizes = 0;
    }

    public int getQuizesApplied() {
        return quizesApplied;
    }

    public void setQuizesApplied(int quizesApplied) {
        this.quizesApplied = quizesApplied;
    }

    public int getQuizesPassed() {
        return quizesPassed;
    }

    public void setQuizesPassed(int quizesPassed) {
        this.quizesPassed = quizesPassed;
    }

    public int getTotalTimeInQuizes() {
        return totalTimeInQuizes;
    }

    public void setTotalTimeInQuizes(int totalTimeInQuizes) {
        this.totalTimeInQuizes = totalTimeInQuizes;
    }
}
