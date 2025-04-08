package lyfjshs.gomis.components.alarm;

import java.awt.Toolkit;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.SwingUtilities;

public class AlarmManagement {
    private static final AlarmManagement INSTANCE = new AlarmManagement(null);
    private static final Object LOCK = new Object();
    
    public static AlarmManagement getInstance() {
        return INSTANCE;
    }
    
    public static AlarmManagement createInstance(AlarmCallback callback) {
        synchronized (LOCK) {
            INSTANCE.setCallback(callback);
            return INSTANCE;
        }
    }

    public interface AlarmCallback {
        void onAlarmTriggered();
        void onAlarmScheduled(LocalDateTime dateTime);
        void onAlarmStopped();
        void onAlarmSnoozed(LocalDateTime newDateTime);
    }

    private LocalDateTime alarmDateTime;
    private Timer alarmTimer;
    private AlarmCallback callback;
    private Clip soundClip;
    private Set<LocalDateTime> scheduledAlarms = new HashSet<>();
    private boolean isAlarmActive = false;

    private AlarmManagement(AlarmCallback callback) {
        this.callback = callback;
    }

    public void setCallback(AlarmCallback callback) {
        this.callback = callback;
    }

    // Schedule the alarm with synchronization
    public void scheduleFinalAlarm(LocalDateTime dateTime) {
        synchronized (LOCK) {
            // Cancel any existing alarm first
            cancelAlarm();
            
            this.alarmDateTime = dateTime.withSecond(0).withNano(0);

            // Prevent scheduling the same alarm time multiple times
            if (scheduledAlarms.contains(alarmDateTime)) {
                return;
            }
            
            scheduledAlarms.add(alarmDateTime);
            isAlarmActive = true;

            alarmTimer = new Timer(true);
            alarmTimer.schedule(new TimerTask() {
                public void run() {
                    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
                    if (!now.isBefore(alarmDateTime)) {
                        SwingUtilities.invokeLater(() -> {
                            if (isAlarmActive) {
                                playAlarmSound(true);
                                if (callback != null) {
                                    callback.onAlarmTriggered();
                                }
                            }
                        });
                    }
                }
            }, java.sql.Timestamp.valueOf(alarmDateTime), 60000);

            if (callback != null) {
                callback.onAlarmScheduled(alarmDateTime);
            }
        }
    }

    private void playAlarmSound(boolean loop) {
        synchronized (LOCK) {
            stopSound();
            try {
                File soundFile = new File("src/main/resources/sounds/reminder.wav");
                if (soundFile.exists()) {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                    soundClip = AudioSystem.getClip();
                    soundClip.open(audioInputStream);
                    if (loop) {
                        soundClip.loop(Clip.LOOP_CONTINUOUSLY);
                    } else {
                        soundClip.start();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } catch (Exception e) {
                System.err.println("Error playing alarm sound: " + e.getMessage());
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public void snooze(int minutes) {
        synchronized (LOCK) {
            stopSound();
            if (alarmDateTime != null) {
                alarmDateTime = LocalDateTime.now().plusMinutes(minutes).withSecond(0).withNano(0);
                scheduleFinalAlarm(alarmDateTime);
                if (callback != null) {
                    callback.onAlarmSnoozed(alarmDateTime);
                }
            }
        }
    }

    public void stopSound() {
        synchronized (LOCK) {
            if (soundClip != null) {
                if (soundClip.isRunning()) {
                    soundClip.stop();
                }
                if (soundClip.isOpen()) {
                    soundClip.close();
                }
                soundClip = null;
            }
        }
    }

    public void cancelAlarm() {
        synchronized (LOCK) {
            stopSound();
            isAlarmActive = false;
            if (alarmTimer != null) {
                alarmTimer.cancel();
                alarmTimer = null;
            }
            if (alarmDateTime != null) {
                scheduledAlarms.remove(alarmDateTime);
                alarmDateTime = null;
            }
            if (callback != null) {
                callback.onAlarmStopped();
            }
        }
    }

    public LocalDateTime getAlarmDateTime() {
        synchronized (LOCK) {
            return alarmDateTime;
        }
    }

    public boolean isAlarmActive() {
        synchronized (LOCK) {
            return isAlarmActive;
        }
    }
}
