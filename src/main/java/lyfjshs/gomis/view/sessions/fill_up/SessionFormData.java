package lyfjshs.gomis.view.sessions.fill_up;

import java.util.List;

import lyfjshs.gomis.view.sessions.TempParticipant;

public class SessionFormData {
    // Details
    public String sessionDate;
    public String sessionTime;
    public String appointmentType;
    public String consultationType;
    public String sessionStatus;
    public String notes;
    public String rescheduleDate;
    public String rescheduleTime;
    // Participants
    public List<TempParticipant> participants;
    // Summary
    public String summary;
    // Violation
    public String violationType;
    public String category;
    public String description;
    public String reinforcement;
    public String otherViolation;
    // Add more fields as needed
} 