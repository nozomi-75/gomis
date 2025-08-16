 package lyfjshs.gomis.view.incident.INCIDENT_fill_up;

import java.util.ArrayList;
import java.util.List;

public class IncidentFullData {
    public String reportedBy;
    public String gradeSection;
    public java.time.LocalDate date;
    public java.time.LocalTime time;
    public String status;
    public String narrative;
    public String actions;
    public String recommendations;
    public List<TempIncidentParticipant> participants = new ArrayList<>();
}