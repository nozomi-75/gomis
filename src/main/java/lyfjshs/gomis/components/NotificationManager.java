package lyfjshs.gomis.components;

import java.awt.TrayIcon;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import lyfjshs.gomis.components.settings.SettingsManager;

public class NotificationManager {
    private final Connection connection;
    private final AppointmentDAO appointmentDAO;
    private final ViolationCRUD violationCRUD;
    private final TrayIcon trayIcon;
    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;

    public NotificationManager(Connection connection, TrayIcon trayIcon) {
        this.connection = connection;
        this.appointmentDAO = new AppointmentDAO(connection);
        this.violationCRUD = new ViolationCRUD(connection);
        this.trayIcon = trayIcon;
        
        // Add settings listener to handle notification toggle
        SettingsManager.addSettingsListener(state -> {
            if (state.notifications && !isRunning) {
                startNotificationServices();
            } else if (!state.notifications && isRunning) {
                stopNotificationServices();
            }
        });
    }

    public synchronized void startNotificationServices() {
        if (!isRunning && SettingsManager.getCurrentState().notifications) {
            // Create a new scheduler if needed
            if (scheduler == null || scheduler.isShutdown()) {
                scheduler = Executors.newScheduledThreadPool(2);
            }
            
            try {
                // Check for upcoming appointments every 5 minutes
                scheduler.scheduleAtFixedRate(this::checkUpcomingAppointments, 0, 5, TimeUnit.MINUTES);
                
                // Check for pending violations once per day
                scheduler.scheduleAtFixedRate(this::checkPendingViolations, 0, 1, TimeUnit.DAYS);
                
                isRunning = true;
            } catch (RejectedExecutionException e) {
                System.err.println("Failed to start notification services: " + e.getMessage());
                stopNotificationServices(); // Clean up if startup fails
            }
        }
    }

    public synchronized void stopNotificationServices() {
        if (isRunning && scheduler != null) {
            try {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                isRunning = false;
                scheduler = null;
            }
        }
    }

    private void checkUpcomingAppointments() {
        if (!SettingsManager.getCurrentState().notifications) return;
        
        try {
            List<Appointment> todayAppointments = appointmentDAO.getTodayAppointments();
            LocalDateTime now = LocalDateTime.now();

            for (Appointment appointment : todayAppointments) {
                LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
                long minutesUntil = ChronoUnit.MINUTES.between(now, appointmentTime);

                // Notify if appointment is within the next hour
                if (minutesUntil > 0 && minutesUntil <= 60) {
                    String message = String.format("Upcoming appointment in %d minutes: %s",
                            minutesUntil, appointment.getAppointmentTitle());
                    displayNotification("Upcoming Appointment", message, TrayIcon.MessageType.INFO);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            displayNotification("Error", "Failed to check upcoming appointments", TrayIcon.MessageType.ERROR);
        }
    }

    private void checkPendingViolations() {
        if (!SettingsManager.getCurrentState().notifications) return;
        
        try {
            List<ViolationRecord> activeViolations = violationCRUD.getActiveViolations();
            
            if (!activeViolations.isEmpty()) {
                String message = String.format("%d violation(s) need follow-up", activeViolations.size());
                displayNotification("Pending Violations", message, TrayIcon.MessageType.WARNING);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            displayNotification("Error", "Failed to check pending violations", TrayIcon.MessageType.ERROR);
        }
    }

    private void displayNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null && SettingsManager.getCurrentState().notifications) {
            trayIcon.displayMessage(title, message, type);
        }
    }
} 