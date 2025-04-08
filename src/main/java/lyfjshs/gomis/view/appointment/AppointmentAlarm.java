package lyfjshs.gomis.view.appointment;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.alarm.AlarmManagement;
import lyfjshs.gomis.components.notification.NotificationManager;

public class AppointmentAlarm {
    private final Appointment appointment;
    private final Runnable onStartSession;
    private final AlarmManagement alarmManager; // For the interactive alarm
    private Timer alarmTimer;
    private static JDialog currentDialog; // Track current dialog
    private static final Object dialogLock = new Object(); // Lock for dialog synchronization
    
    public AppointmentAlarm(Appointment appointment, Runnable onStartSession) {
        this.appointment = appointment;
        this.onStartSession = onStartSession;
        
        // Use AlarmManagement singleton instance
        this.alarmManager = AlarmManagement.createInstance(new AlarmManagement.AlarmCallback() {
            @Override
            public void onAlarmTriggered() {
                showCustomAlarmPopup(); // Show the interactive dialog
            }

            @Override
            public void onAlarmScheduled(LocalDateTime dateTime) { /* No action needed for alarm scheduling */ }

            @Override
            public void onAlarmStopped() { 
                closeCurrentDialog(); // Close any existing dialog
            }

            @Override
            public void onAlarmSnoozed(LocalDateTime newDateTime) {
                // Update NotificationManager that snooze happened (optional)
                showSystemNotification("Appointment Snoozed", true); 
            }
        });
        
        // Set up the single alarm
        setupAlarm();
    }
    
    private void setupAlarm() {
        LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        // Cancel any existing timer first
        cancelTimer();

        // Only schedule if the appointment is in the future
        if (appointmentTime.isAfter(now)) {
            // Schedule the interactive alarm using AlarmManagement
            alarmManager.scheduleFinalAlarm(appointmentTime);
            
            // Also schedule a system notification at the same time
            alarmTimer = new Timer(true); // Use daemon thread
            long delay = ChronoUnit.MILLIS.between(now, appointmentTime);
            if (delay < 0) delay = 0; // Ensure non-negative delay

            alarmTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    showSystemNotification("Appointment Time", true);
                }
            }, delay);
        }
    }

    // Close any existing dialog
    private void closeCurrentDialog() {
        synchronized (dialogLock) {
            if (currentDialog != null && currentDialog.isDisplayable()) {
                SwingUtilities.invokeLater(() -> {
                    currentDialog.dispose();
                    currentDialog = null;
                });
            }
        }
    }
    
    // Shows the interactive alarm dialog with looping sound managed by AlarmManagement
    private void showCustomAlarmPopup() {
        SwingUtilities.invokeLater(() -> {
            synchronized (dialogLock) {
                // Close any existing dialog first
                closeCurrentDialog();
                
                // Create new dialog using AppointmentAlarmDialog
                AppointmentAlarmDialog popup = new AppointmentAlarmDialog(appointment, onStartSession, this);
                currentDialog = popup; // Set as current dialog
            }
        });
    }
    
    // Shows a non-interactive system tray notification
    private void showSystemNotification(String reminderType, boolean isUrgent) {
        SwingUtilities.invokeLater(() -> {
            // Check cooldown via NotificationManager
            if (NotificationManager.isAppointmentNotified(appointment.getAppointmentId())) {
                return;
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
            long minutesUntil = ChronoUnit.MINUTES.between(now, appointmentTime);
            
            String message = String.format("%s: %s\nScheduled for: %s\n(%d minutes from now)",
                reminderType,
                appointment.getAppointmentTitle(),
                appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
                minutesUntil);
                
            if (Main.gFrame != null && Main.gFrame.getNotificationManager() != null) {
                NotificationManager notificationManager = Main.gFrame.getNotificationManager();
                if (isUrgent) {
                    notificationManager.showWarningNotification("Urgent Appointment Reminder", message);
                } else {
                    notificationManager.showInfoNotification("Appointment Reminder", message);
                }
                // Mark as notified *after* successfully showing the notification
                NotificationManager.markAppointmentAsNotified(appointment.getAppointmentId()); 
            }
        });
    }
    
    private void cancelTimer() {
        if (alarmTimer != null) {
            alarmTimer.cancel();
            alarmTimer = null;
        }
    }
    
    // Stop all timers and the alarm
    public void stop() {
        cancelTimer();
        if (alarmManager != null) {
            alarmManager.cancelAlarm();
        }
        closeCurrentDialog(); // Ensure dialog is closed
    }

    // Add getter for appointment
    public Appointment getAppointment() {
        return appointment;
    }

    // Method to update the appointment in the database
    public void updateAppointmentInDatabase(LocalDateTime newDateTime, String notes) {
        try {
            // Update appointment time in the database
            appointment.setAppointmentDateTime(java.sql.Timestamp.valueOf(newDateTime));
            appointment.setAppointmentNotes((appointment.getAppointmentNotes() != null ? 
                appointment.getAppointmentNotes() + "\n" : "") + notes);
            
            // Get database connection
            java.sql.Connection connection = lyfjshs.gomis.Database.DBConnection.getConnection();
            if (connection != null) {
                // Save to database using AppointmentDAO
                lyfjshs.gomis.Database.DAO.AppointmentDAO appointmentDAO = 
                    new lyfjshs.gomis.Database.DAO.AppointmentDAO(connection);
                
                if (appointmentDAO.updateAppointment(appointment)) {
                    // Notify that appointment was updated
                    lyfjshs.gomis.utils.EventBus.publish("appointment_updated", appointment.getAppointmentId());
                }
                
                // Release the connection back to the pool
                lyfjshs.gomis.Database.DBConnection.releaseConnection(connection);
            } else {
                throw new Exception("Database connection not available");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Handle error (e.g., show a message dialog)
        }
    }

    // Getter for AlarmManagement
    public AlarmManagement getAlarmManager() {
        return alarmManager;
    }
} 