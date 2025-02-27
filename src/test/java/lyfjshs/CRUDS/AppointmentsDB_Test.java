package lyfjshs.CRUDS;

import lyfjshs.gomis.Database.DAO.AppointmentsDAO;
import lyfjshs.gomis.Database.model.Appointments;
import java.sql.Timestamp;
import java.util.List;

public class AppointmentsDB_Test {
    public static void main(String[] args) {
        AppointmentsDAO dao = new AppointmentsDAO();

        // Insert a new appointment
        Timestamp now = new Timestamp(System.currentTimeMillis());
        boolean insertSuccess = dao.insertAppointment(1, 2, "Counseling Session", "One-on-One", now, "First session", "Scheduled");
        System.out.println("Insert successful: " + insertSuccess);

        // Retrieve an appointment by ID
        Appointments appointment = dao.getAppointmentById(1);
        if (appointment != null) {
            System.out.println("Retrieved Appointment: " + appointment);
        } else {
            System.out.println("Appointment not found.");
        }

        // Retrieve all appointments
        List<Appointments> appointments = dao.getAllAppointments();
        System.out.println("All Appointments:");
        for (Appointments app : appointments) {
            System.out.println(app);
        }

        // Update an existing appointment
        boolean updateSuccess = dao.updateAppointment(1, 3, "Follow-up Session", "Online", now, "Second session", "Rescheduled");
        System.out.println("Update successful: " + updateSuccess);

        // Delete an appointment
        boolean deleteSuccess = dao.deleteAppointment(1);
        System.out.println("Delete successful: " + deleteSuccess);
    }
}
