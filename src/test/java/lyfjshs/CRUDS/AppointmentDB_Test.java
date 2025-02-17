package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.model.Appointment;

public class AppointmentDB_Test{

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/gomisDB";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            
            // Test addAppointment
            appointmentDAO.addAppointment(conn, 1, 2, "Career Guidance", Timestamp.valueOf(LocalDateTime.now()), "Scheduled");
            
            // Test getAppointmentsForDate
            List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(conn, LocalDateTime.now().toLocalDate());
            System.out.println("Appointments found: " + appointments.size());
            
            // Test updateAppointment (assuming appointment_id = 1 exists)
            appointmentDAO.updateAppointments(conn, 1, 2, "Updated Type", Timestamp.valueOf(LocalDateTime.now()), "Completed");
            
            // Test deleteAppointment (assuming participant_id = 1 has records)
            appointmentDAO.deleteAppointments(conn, 1);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } 

}