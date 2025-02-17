package lyfjshs.gomis.Database.model;

public class SessionParticipant {
    private int sessionId;
    private int participantId;

    // Constructor to initialize sessionId and participantId
    public SessionParticipant(int sessionId, int participantId) {
        this.sessionId = sessionId;
        this.participantId = participantId;
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    // Optional: toString method for easy display
    @Override
    public String toString() {
        return "SessionParticipant{sessionId=" + sessionId + ", participantId=" + participantId + "}";
    }

    // Optional: equals and hashCode methods if needed for comparison or hashing
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SessionParticipant that = (SessionParticipant) obj;
        return sessionId == that.sessionId && participantId == that.participantId;
    }

    @Override
    public int hashCode() {
        return 31 * sessionId + participantId;
    }
}
