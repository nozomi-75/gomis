package lyfjshs.gomis.components.notification;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.components.settings.SettingsState;
import lyfjshs.gomis.utils.EventBus;

/**
 * Manages system tray notifications with support for different notification types and custom icons.
 */
public class NotificationManager {
    private static final String WINDOW_TITLE = "GOMIS Notification Demo";
    private static NotificationManager instance;
    private final TrayIcon mainTrayIcon;
    private final SystemTray systemTray;
    private final ConcurrentHashMap<TrayIcon, Timer> activeNotifications;
    private final String iconPath;
    private final int defaultNotificationDuration;
    private final int interactiveNotificationDuration;
    private final List<NotificationPopup> notificationPopups;
    private final List<Notification> notifications;
    private final Set<String> shownNotificationIds;
    private static final Set<Integer> notifiedAppointments = new HashSet<>();
    
    private static final Color DEFAULT_ICON_COLOR = new Color(0, 120, 215); // Windows blue
    private static final int ICON_SIZE = 16;
    private static final long NOTIFICATION_COOLDOWN = 300000; // 5 minutes cooldown between identical notifications
    private static final Set<String> recentNotifications = new HashSet<>();
    private static final Map<Integer, Long> appointmentNotificationTimes = new HashMap<>();

    public static class Notification {
        private final String id;
        private final String title;
        private final String message;
        private final String type;
        private final long timestamp;
        private boolean unread;

        public Notification(String title, String message, String type) {
            this.id = UUID.randomUUID().toString();
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
            this.unread = true;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public long getTimestamp() { return timestamp; }
        public boolean isUnread() { return unread; }
        public void markAsRead() { this.unread = false; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Notification that = (Notification) obj;
            return title.equals(that.title) && message.equals(that.message) && type.equals(that.type);
        }
        
        @Override
        public int hashCode() {
            int result = title.hashCode();
            result = 31 * result + message.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }

    /**
     * Checks if an appointment has already been notified
     * @param appointmentId The ID of the appointment
     * @return true if the appointment has already been notified
     */
    public static boolean isAppointmentNotified(Integer appointmentId) {
        if (appointmentId == null) {
            return false;
        }
        return notifiedAppointments.contains(appointmentId);
    }

    /**
     * Marks an appointment as notified to prevent duplicate notifications
     * @param appointmentId The ID of the appointment
     */
    public static void markAppointmentAsNotified(Integer appointmentId) {
        if (appointmentId != null) {
            notifiedAppointments.add(appointmentId);
        }
    }

    /**
     * Clears the list of notified appointments, typically done at midnight
     */
    public static void clearNotifiedAppointments() {
        notifiedAppointments.clear();
    }

    /**
     * Clears the list of notified appointments, typically done at midnight
     */
    public void setupDailyCleanup() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                NotificationManager.clearNotifiedAppointments();
            }
        }, calendar.getTime(), 24 * 60 * 60 * 1000); // Daily
    }

    private NotificationManager() {
        this("/GOMIS_Circle.png", 3000, 10000);
    }
    
    private NotificationManager(String iconPath, int defaultDuration, int interactiveDuration) {
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException("SystemTray is not supported on this platform");
        }
        
        this.iconPath = iconPath;
        this.defaultNotificationDuration = defaultDuration;
        this.interactiveNotificationDuration = interactiveDuration;
        this.activeNotifications = new ConcurrentHashMap<>();
        this.systemTray = SystemTray.getSystemTray();
        this.notificationPopups = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.shownNotificationIds = new HashSet<>();
        
        Image icon = loadIcon();
        PopupMenu popup = setupTrayMenu();
        this.mainTrayIcon = new TrayIcon(icon, WINDOW_TITLE, popup);
        this.mainTrayIcon.setImageAutoSize(true);

        try {
            systemTray.add(mainTrayIcon);
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize notification system", e);
        }
        
        // Setup daily cleanup
        setupDailyCleanup();

        // Subscribe to relevant events
        subscribeToEvents();
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    private Image loadIcon() {
        try {
            // Load icon from resources using Class.getResource()
            var iconUrl = getClass().getResource(iconPath);
            if (iconUrl != null) {
                BufferedImage originalImage = ImageIO.read(iconUrl);
                return scaleImage(originalImage);
            } else {
                System.err.println("Icon resource not found: " + iconPath);
                return createDefaultIcon();
            }
        } catch (IOException e) {
            System.err.println("Error loading icon: " + e.getMessage());
            return createDefaultIcon();
        }
    }

    private Image scaleImage(BufferedImage original) {
        BufferedImage scaledImage = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(original, 0, 0, ICON_SIZE, ICON_SIZE, null);
            return scaledImage;
        } finally {
            g2d.dispose();
        }
    }

    private Image createDefaultIcon() {
        BufferedImage defaultIcon = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = defaultIcon.createGraphics();
        try {
            g2d.setColor(DEFAULT_ICON_COLOR);
            g2d.fillOval(0, 0, ICON_SIZE - 1, ICON_SIZE - 1);
            return defaultIcon;
        } finally {
            g2d.dispose();
        }
    }

    private PopupMenu setupTrayMenu() {
        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> cleanup());
        popup.add(exitItem);
        return popup;
    }

    public void cleanup() {
        cleanup(true); // Default behavior is to exit
    }
    
    /**
     * Cleans up all notification resources and optionally exits the application
     * @param exitApplication Whether to exit the application after cleanup
     */
    public void cleanup(boolean exitApplication) {
        System.out.println("[NOTIFICATION] Starting cleanup...");
        
        // Clean up all active notifications
        activeNotifications.forEach((icon, timer) -> {
            try {
                timer.cancel();
                SwingUtilities.invokeLater(() -> {
                    try {
                        systemTray.remove(icon);
                    } catch (Exception e) {
                        System.err.println("[NOTIFICATION] Error removing notification icon: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("[NOTIFICATION] Error canceling notification timer: " + e.getMessage());
            }
        });
        activeNotifications.clear();
        
        // Clear all notification collections
        notifications.clear();
        shownNotificationIds.clear();
        recentNotifications.clear();
        appointmentNotificationTimes.clear();
        notifiedAppointments.clear();
        
        // Unregister all popups
        for (NotificationPopup popup : new ArrayList<>(notificationPopups)) {
            try {
                popup.hide();
            } catch (Exception e) {
                System.err.println("[NOTIFICATION] Error hiding popup: " + e.getMessage());
            }
        }
        notificationPopups.clear();
        
        // Remove main tray icon
        SwingUtilities.invokeLater(() -> {
            try {
                if (mainTrayIcon != null) {
                    systemTray.remove(mainTrayIcon);
                }
                
                // Only exit if specifically requested
                if (exitApplication) {
                    System.exit(0);
                }
            } catch (Exception e) {
                System.err.println("[NOTIFICATION] Error removing main tray icon: " + e.getMessage());
            }
        });
        
        System.out.println("[NOTIFICATION] Cleanup complete");
    }
    
    /**
     * Comprehensive shutdown method that cleans up all resources
     * This should be called when the application is shutting down
     */
    public void shutdown() {
        System.out.println("[NOTIFICATION] Shutting down NotificationManager...");
        
        // Cancel all timers
        for (Timer timer : activeNotifications.values()) {
            try {
                timer.cancel();
            } catch (Exception e) {
                System.err.println("[NOTIFICATION] Error canceling timer during shutdown: " + e.getMessage());
            }
        }
        
        // Clear all collections
        activeNotifications.clear();
        notifications.clear();
        shownNotificationIds.clear();
        recentNotifications.clear();
        appointmentNotificationTimes.clear();
        notifiedAppointments.clear();
        
        // Dispose all popups
        for (NotificationPopup popup : new ArrayList<>(notificationPopups)) {
            try {
                popup.hide();
            } catch (Exception e) {
                System.err.println("[NOTIFICATION] Error hiding popup during shutdown: " + e.getMessage());
            }
        }
        notificationPopups.clear();
        
        // Remove tray icons
        SwingUtilities.invokeLater(() -> {
            try {
                if (mainTrayIcon != null) {
                    systemTray.remove(mainTrayIcon);
                }
            } catch (Exception e) {
                System.err.println("[NOTIFICATION] Error removing main tray icon during shutdown: " + e.getMessage());
            }
        });
        
        System.out.println("[NOTIFICATION] NotificationManager shutdown complete");
    }
    
    /**
     * Get the current number of active notifications for monitoring
     * @return The number of active notifications
     */
    public int getActiveNotificationCount() {
        return activeNotifications.size();
    }
    
    /**
     * Get the current number of stored notifications for monitoring
     * @return The number of stored notifications
     */
    public int getStoredNotificationCount() {
        return notifications.size();
    }
    
    /**
     * Get the current number of registered popups for monitoring
     * @return The number of registered popups
     */
    public int getPopupCount() {
        return notificationPopups.size();
    }

    public void registerPopup(NotificationPopup popup) {
        notificationPopups.add(popup);
    }

    public void unregisterPopup(NotificationPopup popup) {
        notificationPopups.remove(popup);
    }

    private void notifyPopups(Notification notification) {
        SwingUtilities.invokeLater(() -> {
            for (NotificationPopup popup : notificationPopups) {
                popup.addNotification(notification);
            }
        });
    }

    public List<Notification> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.markAsRead();
        }
        updatePopups();
    }

    private void updatePopups() {
        SwingUtilities.invokeLater(() -> {
            for (NotificationPopup popup : notificationPopups) {
                popup.updateNotifications();
            }
        });
    }

    /**
     * Adds a notification to the list and prevents duplicates
     */
    private void addNotification(Notification notification) {
        // Check for duplicate recent notifications (with same title, message, and type)
        boolean isDuplicate = false;
        
        for (Notification existing : notifications) {
            // If we find an identical notification that's less than 1 minute old
            if (existing.equals(notification) && 
                (System.currentTimeMillis() - existing.getTimestamp() < NOTIFICATION_COOLDOWN)) {
                isDuplicate = true;
                break;
            }
        }
        
        if (isDuplicate) {
            // Don't add duplicate recent notifications
            System.out.println("Skipping duplicate notification: " + notification.getTitle());
            return;
        }
        
        // Add to the beginning of the list
        notifications.add(0, notification);
        
        // Track the notification ID to prevent duplicate displays
        shownNotificationIds.add(notification.getId());
        
        // Schedule cleanup of old IDs after cooldown period
        Timer cleanupTimer = new Timer(true);
        cleanupTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                shownNotificationIds.remove(notification.getId());
            }
        }, NOTIFICATION_COOLDOWN);
        
        // Notify all popups about the new notification
        notifyPopups(notification);
        
        // Notify that a new notification is being displayed
        for (NotificationPopup popup : notificationPopups) {
            NotificationCallback callback = popup.getCallback();
            if (callback != null) {
                callback.onNotificationDisplayed(notification);
            }
        }
    }

    public void showInfoNotification(String title, String message) {
        Notification notification = new Notification(title, message, "INFO");
        addNotification(notification);
        ensureEventDispatchThread(() -> 
            mainTrayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO));
    }

    public void showWarningNotification(String title, String message) {
        Notification notification = new Notification(title, message, "WARNING");
        addNotification(notification);
        ensureEventDispatchThread(() -> 
            mainTrayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING));
    }

    public void showErrorNotification(String title, String message) {
        Notification notification = new Notification(title, message, "ERROR");
        addNotification(notification);
        ensureEventDispatchThread(() -> 
            mainTrayIcon.displayMessage(title, message, TrayIcon.MessageType.ERROR));
    }

    public void showCustomNotification(String title, String message) {
        Notification notification = new Notification(title, message, "CUSTOM");
        addNotification(notification);
        ensureEventDispatchThread(() -> {
        try {
            Image customIcon = loadIcon();
            if (customIcon != null) {
                TrayIcon notificationIcon = new TrayIcon(customIcon);
                notificationIcon.setImageAutoSize(true);
                systemTray.add(notificationIcon);

                    Timer timer = new Timer();
                    activeNotifications.put(notificationIcon, timer);
                    
                notificationIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

                    timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                            cleanup(notificationIcon);
                    }
                    }, defaultNotificationDuration);
            }
        } catch (AWTException e) {
            System.err.println("Failed to show custom notification: " + e.getMessage());
                mainTrayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
        });
    }

    public void showInteractiveNotification(String title, String message) {
        Notification notification = new Notification(title, message, "INTERACTIVE");
        addNotification(notification);
        ensureEventDispatchThread(() -> {
            try {
        TrayIcon interactiveTrayIcon = new TrayIcon(loadIcon(), title);
        interactiveTrayIcon.setImageAutoSize(true);
            systemTray.add(interactiveTrayIcon);
                
                Timer timer = new Timer();
                activeNotifications.put(interactiveTrayIcon, timer);
                
        interactiveTrayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

                timer.schedule(new TimerTask() {
            @Override
                    public void run() {
                        cleanup(interactiveTrayIcon);
                    }
                }, interactiveNotificationDuration);
                
            } catch (AWTException e) {
                System.err.println("Failed to show interactive notification: " + e.getMessage());
                mainTrayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            }
        });
    }

    private void cleanup(TrayIcon icon) {
        ensureEventDispatchThread(() -> {
            Timer timer = activeNotifications.remove(icon);
            if (timer != null) {
                timer.cancel();
            }
            systemTray.remove(icon);
        });
    }

    private void ensureEventDispatchThread(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    public TrayIcon getMainTrayIcon() {
        return mainTrayIcon;
    }
    
    public boolean isNotificationsEnabled() {
        // Check settings if available
        try {
            lyfjshs.gomis.components.settings.SettingsState state = 
                lyfjshs.gomis.components.settings.SettingsManager.getCurrentState();
            return state != null && state.notifications;
        } catch (Exception e) {
            // If settings aren't available, default to true
            return true;
        }
    }

    /**
     * Subscribes to relevant events from EventBus and shows notifications.
     * Call this method during NotificationManager initialization.
     */
    public void subscribeToEvents() {
        EventBus.subscribe("appointmentCreated", (data) -> {
            SettingsState state = SettingsManager.getCurrentState();
            if (state != null && state.notifications && state.notifyOnAppointmentCreated) {
                Appointment appt = (Appointment) data;
                String msg = String.format("%s\n%s at %s", appt.getAppointmentTitle(),
                        appt.getAppointmentDateTime().toLocalDateTime().toLocalDate(),
                        appt.getAppointmentDateTime().toLocalDateTime().toLocalTime());
                NotificationManager.getInstance().showInfoNotification("Appointment Created", msg);
            }
        });
        EventBus.subscribe("appointmentReminder", (data) -> {
            SettingsState state = SettingsManager.getCurrentState();
            if (state != null && state.notifications && state.notifyOnAppointmentReminder) {
                Appointment appt = (Appointment) data;
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime appointmentTime = appt.getAppointmentDateTime().toLocalDateTime();
                long minutesUntil = java.time.temporal.ChronoUnit.MINUTES.between(now, appointmentTime);
                String msg = String.format("Upcoming Appointment: %s\nScheduled for: %s\n(In %d minutes)",
                        appt.getAppointmentTitle(),
                        appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")), minutesUntil);
                NotificationManager.getInstance().showInfoNotification("Appointment Reminder", msg);
            }
        });
        EventBus.subscribe("appointmentMissed", (data) -> {
            if (SettingsManager.getCurrentState() != null && SettingsManager.getCurrentState().notifications) {
                Appointment appt = (Appointment) data;
                String msg = String.format("Missed Appointment: %s\n%s at %s", appt.getAppointmentTitle(),
                        appt.getAppointmentDateTime().toLocalDateTime().toLocalDate(),
                        appt.getAppointmentDateTime().toLocalDateTime().toLocalTime());
                NotificationManager.getInstance().showWarningNotification("Missed Appointment", msg);
            }
        });
        // Add more event subscriptions as needed (e.g., incidentReported, sessionEnded)
    }
}