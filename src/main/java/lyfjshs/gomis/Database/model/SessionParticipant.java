package lyfjshs.gomis.Database.model;

public class SessionParticipant {
    private int sessionId;
    private int participantId;

    // Constructor, getters, and setters

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
} 