package lyfjshs.gomis.view.appointment.add;

import java.sql.Connection;

import lyfjshs.gomis.view.students.StudentSearchPanel;

public class AppointmentStudentSearch extends StudentSearchPanel {
    public AppointmentStudentSearch(Connection connection, StudentSelectionCallback callback) {
        super(connection, callback, "appointment_student_search");
    }
} 