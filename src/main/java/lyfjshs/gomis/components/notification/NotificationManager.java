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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

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
    
    private static final Color DEFAULT_ICON_COLOR = new Color(0, 120, 215); // Windows blue
    private static final int ICON_SIZE = 16;

    public static class Notification {
        private final String title;
        private final String message;
        private final String type;
        private final long timestamp;
        private boolean unread;

        public Notification(String title, String message, String type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
            this.unread = true;
        }

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public long getTimestamp() { return timestamp; }
        public boolean isUnread() { return unread; }
        public void markAsRead() { this.unread = false; }
    }

    private NotificationManager() {
        this("src/main/resources/GOMIS_Circle.png", 3000, 10000);
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
        
        Image icon = loadIcon();
        PopupMenu popup = setupTrayMenu();
        this.mainTrayIcon = new TrayIcon(icon, WINDOW_TITLE, popup);
        this.mainTrayIcon.setImageAutoSize(true);

        try {
            systemTray.add(mainTrayIcon);
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize notification system", e);
        }
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    private Image loadIcon() {
        try {
            File iconFile = new File(iconPath);
            if (iconFile.exists()) {
                BufferedImage originalImage = ImageIO.read(iconFile);
                return scaleImage(originalImage);
            } else {
                System.err.println("Icon file not found: " + iconPath);
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
        // Clean up all active notifications
        activeNotifications.forEach((icon, timer) -> {
            timer.cancel();
            SwingUtilities.invokeLater(() -> systemTray.remove(icon));
        });
        activeNotifications.clear();
        
        // Remove main tray icon and exit
        SwingUtilities.invokeLater(() -> {
            systemTray.remove(mainTrayIcon);
            System.exit(0);
        });
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

    private void addNotification(Notification notification) {
        // Add to the beginning of the list
        notifications.add(0, notification);
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
}