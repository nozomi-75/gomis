package lyfjshs.gomis.view.appointment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;

/**
 * Adapter class to bridge between GOMIS's AppointmentDAO and CustomCalendar's expected interface.
 * This class handles the conversion between GOMIS appointment models and the calendar's display format.
 */
public class AppointmentCalendarAdapter {
    private static final Logger logger = LogManager.getLogger(AppointmentCalendarAdapter.class);
    private final AppointmentDAO gomisDAO;

    public AppointmentCalendarAdapter(AppointmentDAO gomisDAO) {
        this.gomisDAO = gomisDAO;
    }

    /**
     * Gets appointments for a specific month.
     * @param year The year
     * @param month The month (1-12)
     * @return Map of day number to list of appointments
     */
    public Map<Integer, List<Appointment>> getAppointmentsForMonth(int year, int month) {
        try {
            // Get all appointments for the month
            List<Appointment> allAppointments = gomisDAO.getAllAppointments();
            
            // Filter appointments for the specified month and year, excluding completed, cancelled, and in-progress ones
            Map<Integer, List<Appointment>> appointmentsByDay = new HashMap<>();
            for (Appointment appointment : allAppointments) {
                // Skip completed, cancelled, and in-progress appointments
                String status = appointment.getAppointmentStatus();
                if ("Completed".equalsIgnoreCase(status) || 
                    "Cancelled".equalsIgnoreCase(status) || 
                    "In Progress".equalsIgnoreCase(status)) {
                    continue;
                }
                
                LocalDate appointmentDate = appointment.getAppointmentDateTime().toLocalDateTime().toLocalDate();
                if (appointmentDate.getYear() == year && appointmentDate.getMonthValue() == month) {
                    int day = appointmentDate.getDayOfMonth();
                    appointmentsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(appointment);
                }
            }
            return appointmentsByDay;
        } catch (SQLException e) {
            logger.error("Error getting appointments for month", e);
            return new HashMap<>();
        }
    }

    /**
     * Gets appointments for a specific date.
     * @param date The date to get appointments for
     * @return List of appointments for the date
     */
    public List<Appointment> getAppointmentsForDate(LocalDate date) {
        try {
            return gomisDAO.getAppointmentsForDate(date);
        } catch (SQLException e) {
            logger.error("Error getting appointments for date", e);
            return List.of();
        }
    }
} 