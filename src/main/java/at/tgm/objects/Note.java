package at.tgm.objects;

public class Note extends SendableObject {

    public enum Notenwert {
        SEHR_GUT("Sehr Gut"),
        GUT("Gut"),
        BEFRIEDIGEND("Befriedigend"),
        GENUEGEND("Genügend"),
        NICHT_GENUEGEND("Nicht Genügend");

        private final String displayName;

        Notenwert(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private Notenwert notenwert;
    private String reason;

    public Note() {
    }

    public Note(Notenwert notenwert, String reason) {
        this.notenwert = notenwert;
        this.reason = reason;
    }

    public Notenwert getNotenwert() {
        return notenwert;
    }

    public void setNotenwert(Notenwert notenwert) {
        this.notenwert = notenwert;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return notenwert != null ? notenwert.getDisplayName() : "Nicht benotet";
    }
}
