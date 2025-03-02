package lyfjshs.gomis.Database.entity;

public class SessionParticipant {
    private int sessionParticipantsId;
    private int participantId;
    private int sessionId;

    public int getSessionParticipantsId() { return sessionParticipantsId; }
    public void setSessionParticipantsId(int sessionParticipantsId) { this.sessionParticipantsId = sessionParticipantsId; }
    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
}
