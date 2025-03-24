package lyfjshs.gomis.test;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundApp {
    private JFrame frame;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private Timer backgroundTimer;

    public BackgroundApp() {
        frame = new JFrame("Background App");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent default close action
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Minimize to tray with timer & notifications");
        frame.add(label);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> exitApplication());
        frame.add(exitButton);

        // Window close event (Minimize instead of exiting)
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray();
            }
        });

        setupSystemTray();
        startBackgroundTask(); // Start timer-based background task
        frame.setVisible(true);
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(frame, "System Tray not supported!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        systemTray = SystemTray.getSystemTray();
        Image trayImage = Toolkit.getDefaultToolkit().getImage("icon.png"); // Replace with your icon file

        PopupMenu popupMenu = new PopupMenu();

        // Restore option
        MenuItem restoreItem = new MenuItem("Restore");
        restoreItem.addActionListener(e -> restoreFromTray());
        popupMenu.add(restoreItem);

        // Exit option
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        popupMenu.add(exitItem);

        trayIcon = new TrayIcon(trayImage, "Background App Running", popupMenu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> restoreFromTray());
    }

    private void minimizeToTray() {
        try {
            systemTray.add(trayIcon);
            frame.setVisible(false); // Hide main window
            showTrayMessage("App is running in background!", TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void restoreFromTray() {
        frame.setVisible(true);
        systemTray.remove(trayIcon);
    }

    private void showTrayMessage(String message, TrayIcon.MessageType type) {
        if (systemTray != null && trayIcon != null) {
            trayIcon.displayMessage("Notification", message, type);
        }
    }

    private void startBackgroundTask() {
        backgroundTimer = new Timer();
        backgroundTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                showTrayMessage("Reminder: Your app is still running!", TrayIcon.MessageType.INFO);
            }
        }, 5000, 10000); // First notification after 5s, then every 10s
    }

    private void exitApplication() {
        if (backgroundTimer != null) {
            backgroundTimer.cancel();
        }
        systemTray.remove(trayIcon);
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BackgroundApp::new);
    }
}
