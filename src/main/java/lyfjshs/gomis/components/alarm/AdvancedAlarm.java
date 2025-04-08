package lyfjshs.gomis.components.alarm;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class AdvancedAlarm extends JFrame {
    private static AdvancedAlarm instance;
    private static final Object LOCK = new Object();
    private JFormattedTextField timeField;
    private JSpinner snoozeSpinner;
    private JButton startButton, stopButton;
    private JLabel statusLabel;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Timer alarmTimer;
    private LocalDateTime alarmDateTime;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private JFormattedTextField dateField;
    private static JDialog currentDialog;

    public static AdvancedAlarm getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new AdvancedAlarm();
                }
            }
        }
        return instance;
    }

    private AdvancedAlarm() {
        setTitle("Alarm Clock");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(350, 250);
        getContentPane().setLayout(new MigLayout("wrap 2", "[grow][grow]", "[]20[]20[]20[][][][]"));

        initComponents();
        initListeners();
        setupTray();
        
        // Use the singleton AlarmManagement instance
        AlarmManagement.createInstance(new AlarmManagement.AlarmCallback() {
            @Override
            public void onAlarmTriggered() {
                showAlarmPopup();
            }

            @Override
            public void onAlarmScheduled(LocalDateTime dateTime) {
                updateStatus("Alarm set for: " + dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                if (trayIcon != null) {
                    trayIcon.displayMessage("Alarm Set", 
                        "Will trigger at " + dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), 
                        TrayIcon.MessageType.INFO);
                }
            }

            @Override
            public void onAlarmStopped() {
                updateStatus("Alarm stopped.");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                closeCurrentDialog();
            }

            @Override
            public void onAlarmSnoozed(LocalDateTime newDateTime) {
                updateStatus("Snoozed until: " + newDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                if (trayIcon != null) {
                    trayIcon.displayMessage("Snoozed", 
                        "Next alarm at " + newDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), 
                        TrayIcon.MessageType.INFO);
                }
            }
        });
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    private void closeCurrentDialog() {
        synchronized (LOCK) {
            if (currentDialog != null && currentDialog.isDisplayable()) {
                SwingUtilities.invokeLater(() -> {
                    currentDialog.dispose();
                    currentDialog = null;
                });
            }
        }
    }

    protected void initComponents() {
        // Initialize date and time pickers
        datePicker = new DatePicker();
        timePicker = new TimePicker();
        
        // Set default date to today
        datePicker.setSelectedDate(java.time.LocalDate.now());
        
        // Set default time to current time + 1 minute
        timePicker.setSelectedTime(LocalTime.now().plusMinutes(1));

        timeField = new JFormattedTextField();
        timePicker.setEditor(timeField);
        
        dateField = new JFormattedTextField();
        datePicker.setEditor(dateField);
        
        startButton = new JButton("Set Alarm");
        stopButton = new JButton("Stop Alarm");
        stopButton.setEnabled(false);

        statusLabel = new JLabel("Alarm not set.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Layout components
        getContentPane().add(new JLabel("Time:"), "cell 0 0,alignx center");
        getContentPane().add(timeField, "cell 1 0,growx");
        getContentPane().add(new JLabel("Date:"), "cell 0 1,alignx center");
        getContentPane().add(dateField, "cell 1 1,growx");
        
        JLabel label = new JLabel("Snooze (min):");
        getContentPane().add(label, "cell 0 2");
        snoozeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        getContentPane().add(snoozeSpinner, "cell 1 2");
        
        getContentPane().add(startButton, "cell 0 3 2 1,grow");
        getContentPane().add(stopButton, "cell 0 4 2 1,grow");
        getContentPane().add(statusLabel, "cell 0 5 2 1,alignx center");
    }

    protected void initListeners() {
        startButton.addActionListener(e -> startAlarm());
        stopButton.addActionListener(e -> stopAlarm());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                minimizeToTray();
            }
        });
    }

    protected void startAlarm() {
        if (datePicker.getSelectedDate() == null || timePicker.getSelectedTime() == null) {
            statusLabel.setText("Please select both date and time!");
            return;
        }

        LocalDateTime dateTime = LocalDateTime.of(datePicker.getSelectedDate(), timePicker.getSelectedTime());
        if (dateTime.isBefore(LocalDateTime.now())) {
            statusLabel.setText("Cannot set alarm in the past!");
            return;
        }

        AlarmManagement.getInstance().scheduleFinalAlarm(dateTime);
    }
    

    protected void stopAlarm() {
        AlarmManagement.getInstance().cancelAlarm();
    }
    

    protected void showAlarmPopup() {
        SwingUtilities.invokeLater(() -> {
            synchronized (LOCK) {
                closeCurrentDialog();

                JDialog popup = new JDialog();
                currentDialog = popup;
                popup.setTitle("⏰ Alarm");
                popup.getContentPane().setLayout(new MigLayout("fill, wrap 1", "[center]", "[]20[]20[]"));
                popup.setSize(400, 200);
                popup.setLocationRelativeTo(null);
                popup.setAlwaysOnTop(true);

                JLabel msg = new JLabel("⏰ Time to Wake Up!", SwingConstants.CENTER);
                msg.setFont(new Font("Segoe UI", Font.BOLD, 24));
                msg.setForeground(Color.RED);

                JButton snoozeBtn = new JButton("Snooze");
                JButton dismissBtn = new JButton("Dismiss");

                snoozeBtn.addActionListener(e -> {
                    int snoozeMin = (int) snoozeSpinner.getValue();
                    AlarmManagement.getInstance().snooze(snoozeMin);
                    popup.dispose();
                });

                dismissBtn.addActionListener(e -> {
                    AlarmManagement.getInstance().cancelAlarm();
                    popup.dispose();
                });

                popup.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        AlarmManagement.getInstance().stopSound();
                        synchronized (LOCK) {
                            if (currentDialog == popup) {
                                currentDialog = null;
                            }
                        }
                    }
                });

                popup.getContentPane().add(msg, "grow");
                popup.getContentPane().add(snoozeBtn, "grow");
                popup.getContentPane().add(dismissBtn, "grow");

                popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                restoreFromTray();
                popup.setVisible(true);
            }
        });
    }

    protected void restartAlarm() {
        if (alarmTimer != null) {
            alarmTimer.cancel();
        }
        
        alarmTimer = new Timer();
        alarmTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
                LocalDateTime alarmCheck = alarmDateTime.withSecond(0).withNano(0);
                
                if (now.equals(alarmCheck)) {
                    SwingUtilities.invokeLater(() -> showAlarmPopup());
                    stopAlarm();
                }
            }
        }, 0, 1000);

        statusLabel.setText("Snoozed until: " + alarmDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        
        // Show notification in tray
        if (trayIcon != null) {
            trayIcon.displayMessage(
                "Alarm Snoozed", 
                "Alarm will trigger again at " + alarmDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), 
                TrayIcon.MessageType.INFO
            );
        }
    }

    private void setupTray() {
        if (!SystemTray.isSupported()) {
            return;
        }

        tray = SystemTray.getSystemTray();
        
        // Try multiple ways to load the icon
        Image img = null;
        try {
            // Try loading from resources first
            img = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/alarm.png"));
            
            // If that fails, try loading from file system
            if (img == null) {
                img = Toolkit.getDefaultToolkit().getImage("src/main/resources/icons/alarm.png");
            }
            
            // If still null, create a default icon
            if (img == null) {
                img = createDefaultIcon();
            }
        } catch (Exception e) {
            e.printStackTrace();
            img = createDefaultIcon();
        }

        PopupMenu menu = new PopupMenu();

        MenuItem openItem = new MenuItem("Open Alarm");
        openItem.addActionListener(e -> restoreFromTray());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            stopAlarm(); // Ensure alarm is stopped
            if (tray != null) {
                tray.remove(trayIcon);
            }
            System.exit(0);
        });

        menu.add(openItem);
        menu.add(exitItem);

        trayIcon = new TrayIcon(img, "Alarm Clock", menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> restoreFromTray());
    }

    private Image createDefaultIcon() {
        // Create a simple 16x16 icon
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(16, 16, 
            java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillOval(0, 0, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawLine(8, 4, 8, 8);
        g2d.drawLine(8, 8, 11, 8);
        g2d.dispose();
        return img;
    }

    public void minimizeToTray() {
        if (tray != null && trayIcon != null) {
            try {
                tray.add(trayIcon);
                setVisible(false);
                
                // Show current alarm status in tray if alarm is set
                if (alarmTimer != null && alarmDateTime != null) {
                    trayIcon.displayMessage(
                        "Alarm Active", 
                        "Alarm will trigger at " + alarmDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), 
                        TrayIcon.MessageType.INFO
                    );
                }
            } catch (AWTException ex) {
                ex.printStackTrace();
                setState(JFrame.ICONIFIED);
            }
        } else {
            setState(JFrame.ICONIFIED);
        }
    }

    private void restoreFromTray() {
        setVisible(true);
        setExtendedState(JFrame.NORMAL);
        setAlwaysOnTop(true);
        toFront();
        setAlwaysOnTop(false);
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
        }
    }

    public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			AdvancedAlarm app = new AdvancedAlarm();
			app.setLocationRelativeTo(null);
			app.setVisible(true);
		});
	}
} 