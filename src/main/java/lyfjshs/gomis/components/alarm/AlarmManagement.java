/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.alarm;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import javazoom.jl.player.Player;
import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.components.settings.SettingsState;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.view.appointment.AppointmentAlarmDialog;
import lyfjshs.gomis.view.appointment.AppointmentManagement;

public class AlarmManagement {
    private static final Logger logger = Logger.getLogger(AlarmManagement.class.getName());
    private static final AlarmManagement INSTANCE = new AlarmManagement(null, null);
    private static final Object LOCK = new Object();
    private static final long NOTIFICATION_CHECK_INTERVAL = 10000; // 10 seconds
    private static final int MAX_SOUND_PLAYBACK_DURATION = 60 * 1000; // 1 minute in milliseconds

    static {
        // Initialize JavaSound system
        try {
            // Get the default mixer info
            javax.sound.sampled.Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            if (mixerInfo.length == 0) {
                System.out.println("[ALARM] WARNING: No audio mixers found!");
            }

            // Try to get the default line
            javax.sound.sampled.Line.Info lineInfo = new javax.sound.sampled.DataLine.Info(
                    javax.sound.sampled.SourceDataLine.class, null);
            if (!AudioSystem.isLineSupported(lineInfo)) {
                System.out.println("[ALARM] WARNING: Default audio line not supported!");
            }
        } catch (Exception e) {
            System.out.println("[ALARM] ERROR initializing JavaSound system: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private final ScheduledExecutorService scheduler;
    private final Set<Integer> notifiedAppointments = new HashSet<>();
    private final Map<Integer, LocalDateTime> lastNotifiedMap = new HashMap<>();
    private final Set<Integer> debouncingAlarms = new HashSet<>(); // Debounce set to prevent re-triggering
    private JDialog alarmDialog;
    private Timer missedAppointmentTimer;
    private Timer soundStopTimer; // New timer for stopping sound after 1 minute
    private Clip currentWavClip;
    private Player currentMp3Player;
    private Thread soundThread;
    private volatile boolean isPlaying = false;
    private LocalDateTime alarmDateTime;
    private Timer alarmTimer;
    private AlarmCallback callback;
    private Set<LocalDateTime> scheduledAlarms = new HashSet<>();
    private boolean isAlarmActive = false;
    private Appointment currentAppointment; // Add this field to track current appointment
    private AppointmentManagement appointmentManagementRef; // New field
    private String currentSoundFile;
    // Flag to indicate if we are snoozing (so stopAlarm doesn't stop the new timer)
    private boolean snoozingInProgress = false;

    public interface AlarmCallback {
        void onAlarmTriggered(Appointment appointment);

        void onAlarmScheduled(LocalDateTime dateTime);

        void onAlarmStopped();

        void onAlarmSnoozed(LocalDateTime newDateTime);
    }

    public static AlarmManagement getInstance() {
        return INSTANCE;
    }

    public static AlarmManagement createInstance(AlarmCallback callback, AppointmentManagement appointmentManagement) {
        synchronized (LOCK) {
            if (INSTANCE.callback == null) {
                INSTANCE.callback = callback;
                INSTANCE.appointmentManagementRef = appointmentManagement; // Assign the reference here
            }
            return INSTANCE;
        }
    }

    private AlarmManagement(AlarmCallback callback, AppointmentManagement appointmentManagement) {
        this.callback = callback;
        this.appointmentManagementRef = appointmentManagement; // Assign the reference here
        this.scheduler = Executors.newScheduledThreadPool(1);
        start();
    }

    public void setCallback(AlarmCallback callback) {
        this.callback = callback;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkUpcomingAppointments, 0, NOTIFICATION_CHECK_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    private void checkUpcomingAppointments() {
        try {
            if (Main.formManager == null || Main.formManager.getCounselorObject() == null) {
                return;
            }

            SettingsState state = SettingsManager.getCurrentState();
            if (state == null || !state.notifications) {
                System.out.println("[ALARM] Notifications are disabled in settings.");
                return;
            }

            // Get upcoming appointments using AppointmentDAO directly
            java.sql.Connection connection = lyfjshs.gomis.Database.DBConnection.getConnection();
            if (connection == null) {
                System.out.println("[ALARM] ERROR: No database connection available");
                logger.warning("No database connection available for appointment check");
                return;
            }

            try {
                lyfjshs.gomis.Database.DAO.AppointmentDAO appointmentDAO = new lyfjshs.gomis.Database.DAO.AppointmentDAO(
                        connection);

                // Get appointments that are due right now (within 1 minute)
                LocalDateTime now = LocalDateTime.now();
                String dueQuery = "SELECT APPOINTMENT_ID FROM APPOINTMENTS "
                        + "WHERE APPOINTMENT_DATE_TIME BETWEEN ? AND ? "
                        + "AND (APPOINTMENT_STATUS = 'Scheduled' OR APPOINTMENT_STATUS = 'Active' OR APPOINTMENT_STATUS = 'Rescheduled') "
                        + "AND APPOINTMENT_STATUS != 'Snoozed' "
                        + "ORDER BY APPOINTMENT_DATE_TIME ASC";

                try (PreparedStatement stmt = connection.prepareStatement(dueQuery)) {
                    stmt.setTimestamp(1, java.sql.Timestamp.valueOf(now));
                    stmt.setTimestamp(2, java.sql.Timestamp.valueOf(now.plusMinutes(2)));

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int appointmentId = rs.getInt("APPOINTMENT_ID");
                            if (debouncingAlarms.contains(appointmentId)) {
                                continue; // Skip if this alarm is currently being debounced
                            }
                            System.out.println("[ALARM] Found due appointment ID: " + appointmentId);
                            Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
                            LocalDateTime lastNotified = lastNotifiedMap.get(appointmentId);
                            if (lastNotified != null && ChronoUnit.MINUTES.between(lastNotified, now) < 2) continue;
                            if (appointment != null && !isAlarmActive) {
                                triggerImmediateAlarm(appointment);
                            }
                            lastNotifiedMap.put(appointmentId, now);
                        }
                    }
                }

                // Get upcoming appointments - Modified query to include Rescheduled status
                String upcomingQuery = "SELECT * FROM APPOINTMENTS "
                        + "WHERE (APPOINTMENT_STATUS = 'Scheduled' OR APPOINTMENT_STATUS = 'Active' OR APPOINTMENT_STATUS = 'Rescheduled') "
                        + "AND APPOINTMENT_STATUS != 'Snoozed' "
                        + "AND APPOINTMENT_DATE_TIME > ? " + "ORDER BY APPOINTMENT_DATE_TIME ASC";

                try (PreparedStatement stmt = connection.prepareStatement(upcomingQuery)) {
                    stmt.setTimestamp(1, java.sql.Timestamp.valueOf(now));

                    try (ResultSet rs = stmt.executeQuery()) {
                        List<Appointment> appointments = new ArrayList<>();
                        while (rs.next()) {
                            Appointment appointment = new Appointment();
                            appointment.setAppointmentId(rs.getInt("APPOINTMENT_ID"));
                            appointment.setGuidanceCounselorId(rs.getInt("GUIDANCE_COUNSELOR_ID"));
                            appointment.setAppointmentTitle(rs.getString("APPOINTMENT_TITLE"));
                            appointment.setConsultationType(rs.getString("CONSULTATION_TYPE"));
                            appointment.setAppointmentDateTime(rs.getTimestamp("APPOINTMENT_DATE_TIME"));
                            appointment.setAppointmentStatus(rs.getString("APPOINTMENT_STATUS"));
                            appointment.setAppointmentNotes(rs.getString("APPOINTMENT_NOTES"));
                            appointments.add(appointment);
                        }

                        for (Appointment appointment : appointments) {
                            LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
                            long minutesUntil = ChronoUnit.MINUTES.between(now, appointmentTime);

                            // Check if we should show an early reminder
                            if (minutesUntil > 0 && minutesUntil <= state.notificationTimeMinutes) {
                                // Check if we've already notified for this appointment
                                if (!notifiedAppointments.contains(appointment.getAppointmentId())) {
                                    triggerEarlyReminder(appointment);
                                    notifiedAppointments.add(appointment.getAppointmentId());
                                }
                            }
                        }
                    }
                }
            } finally {
                // Release the connection back to the pool
                lyfjshs.gomis.Database.DBConnection.releaseConnection(connection);
            }
        } catch (Exception ex) {
            System.out.println("[ALARM] ERROR checking appointments: " + ex.getMessage());
            logger.log(Level.SEVERE, "Error checking upcoming appointments: " + ex.getMessage(), ex);
        }
    }

    private void triggerImmediateAlarm(Appointment appointment) {
        // Add to debounce set and schedule its removal to prevent immediate re-triggering
        final int appointmentId = appointment.getAppointmentId();
        debouncingAlarms.add(appointmentId);
        scheduler.schedule(() -> debouncingAlarms.remove(appointmentId), 61, TimeUnit.SECONDS);

        System.out.println("[ALARM] Triggering immediate alarm for: " + appointment.getAppointmentTitle());
        isAlarmActive = true; // Set active status immediately

        // Get sound settings before showing dialog
        SettingsState state = SettingsManager.getCurrentState();
        String soundFile = state != null && state.soundEnabled ? state.soundFile : null;

        // Play sound first if enabled
        if (soundFile != null) {
            playSound(soundFile);
        } else {
            System.out.println("[ALARM] Sound is disabled or no sound file configured");
        }

        SwingUtilities.invokeLater(() -> {
            // Show the alarm dialog
            showAlarmDialog(appointment);
            System.out.println("[ALARM] Alarm marked as active");
        });
    }

    private void triggerEarlyReminder(Appointment appointment) {
        SwingUtilities.invokeLater(() -> {
            // Publish an event for the early reminder instead of direct notification
            EventBus.publish("appointmentReminder", appointment);

            // Play notification sound if enabled
            SettingsState state = SettingsManager.getCurrentState();
            if (state != null && state.soundEnabled) {
                // For early reminders/notifications, use the fixed notification sound
                String notificationSoundPath = "sounds/notification/reminder.wav";
                playSound(notificationSoundPath);
            }
        });
    }

    public void playSound(String soundFile) {
        synchronized (LOCK) {
            // Stop any existing sound first
            stopSound();

            try {
                // Validate input
                if (soundFile == null || soundFile.trim().isEmpty()) {
                    System.out.println("[ALARM] ERROR: Sound file path is null or empty");
                    logger.warning("Sound file path is null or empty");
                    return;
                }

                // The soundFile from settings is already in the format like
                // "sounds/alarm_tone/Homecoming.mp3"
                // Prepend '/' to make it an absolute path for getResourceAsStream
                String resourcePath = soundFile.startsWith("/") ? soundFile : "/" + soundFile;

                // Verify the resource exists and can be read
                InputStream testStream = getClass().getResourceAsStream(resourcePath);
                if (testStream == null) {
                    System.out.println("[ALARM] ERROR: Sound file not found in resources: " + resourcePath);
                    logger.severe("Sound file not found in resources: " + resourcePath);
                    return;
                }

                // Try to read a few bytes to verify the file is accessible
                byte[] buffer = new byte[1024];
                int bytesRead = testStream.read(buffer);
                testStream.close();

                if (bytesRead <= 0) {
                    System.out.println("[ALARM] ERROR: Sound file is empty or cannot be read: " + resourcePath);
                    logger.severe("Sound file is empty or cannot be read: " + resourcePath);
                    return;
                }

                currentSoundFile = soundFile;
                isPlaying = true;

                if (resourcePath.toLowerCase().endsWith(".wav")) {
                    System.out.println("[ALARM] Playing WAV sound");
                    playWavSound(resourcePath);
                } else if (resourcePath.toLowerCase().endsWith(".mp3")) {
                    System.out.println("[ALARM] Playing MP3 sound");
                    playMp3Sound(resourcePath);
                } else {
                    System.out.println("[ALARM] Unsupported sound format: " + soundFile);
                    logger.warning("Unsupported sound format: " + soundFile);
                    isPlaying = false;
                }
            } catch (Exception e) {
                System.out.println("[ALARM] ERROR playing sound: " + e.getMessage());
                logger.log(Level.SEVERE, "Error playing sound: " + e.getMessage(), e);
                e.printStackTrace();
                isPlaying = false;
            }
        }
    }

    private void playWavSound(String soundFile) {
        try (InputStream audioSrc = getClass().getResourceAsStream(soundFile)) {
            if (audioSrc == null) {
                System.out.println("[ALARM] WAV file not found in resources: " + soundFile);
                logger.severe("WAV file not found in resources: " + soundFile);
                return;
            }

            // Create a buffered input stream that supports mark/reset
            // This is required by AudioSystem.getAudioInputStream
            java.io.BufferedInputStream bufferedStream = new java.io.BufferedInputStream(audioSrc);
            bufferedStream.mark(Integer.MAX_VALUE); // Mark the beginning of the stream

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream);
            AudioFormat format = audioInputStream.getFormat();
            System.out.println("[ALARM] WAV original format: " + format);

            DataLine.Info info = new DataLine.Info(Clip.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("[ALARM] WAV original format not supported. Attempting conversion...");
                AudioFormat targetFormat = getCompatibleFormat(format);
                if (targetFormat != null) {
                    AudioInputStream convertedAudioInputStream = AudioSystem.getAudioInputStream(targetFormat,
                            audioInputStream);
                    format = convertedAudioInputStream.getFormat();
                    audioInputStream = convertedAudioInputStream;
                    info = new DataLine.Info(Clip.class, format);
                } else {
                    System.out.println("[ALARM] No compatible WAV format found. Cannot play.");
                    logger.warning("No compatible WAV format found for " + soundFile);
                    audioInputStream.close();
                    return;
                }
            }

            currentWavClip = (Clip) AudioSystem.getLine(info);
            currentWavClip.open(audioInputStream);
            currentWavClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    stopSound(); // Ensure resources are released
                }
            });
            currentWavClip.start();

            // Start a timer to stop the sound after a maximum duration
            if (soundStopTimer != null) {
                soundStopTimer.stop();
            }
            soundStopTimer = new Timer(MAX_SOUND_PLAYBACK_DURATION, e -> {
                stopSound();
            });
            soundStopTimer.setRepeats(false);
            soundStopTimer.start();

        } catch (LineUnavailableException e) {
            System.out.println("[ALARM] ERROR (WAV Line Unavailable): " + e.getMessage());
            logger.log(Level.SEVERE, "WAV playback failed: Line unavailable for format " + e.getMessage(), e);
            stopSound(); // Ensure resources are released even on error
        } catch (UnsupportedAudioFileException e) {
            System.out.println("[ALARM] ERROR (WAV Unsupported Audio File): " + e.getMessage());
            logger.log(Level.SEVERE, "WAV playback failed: Unsupported audio file format " + e.getMessage(), e);
            stopSound();
        } catch (IOException e) {
            System.out.println("[ALARM] ERROR (WAV IO): " + e.getMessage());
            logger.log(Level.SEVERE, "WAV playback failed: IO Error " + e.getMessage(), e);
            stopSound();
        } catch (Exception e) {
            System.out.println("[ALARM] ERROR (WAV General): " + e.getMessage());
            logger.log(Level.SEVERE, "WAV playback failed: General Error " + e.getMessage(), e);
            e.printStackTrace();
            stopSound();
        }
    }

    private AudioFormat getCompatibleFormat(AudioFormat originalFormat) {
        System.out.println("[ALARM] Searching for compatible format for: " + originalFormat);
        AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.PCM_SIGNED;

        // Try to keep original channels and sample rate, but convert bit depth to 16 or
        // 8
        int[] sampleSizesInBits = { 16, 8 };
        boolean bigEndian = originalFormat.isBigEndian();
        float originalSampleRate = originalFormat.getSampleRate();
        int originalChannels = originalFormat.getChannels();

        // --- Phase 1: Try converting bit depth, keep sample rate and channels ---
        for (int sampleSizeInBits : sampleSizesInBits) {
            int frameSize = originalChannels * (sampleSizeInBits / 8);
            if (frameSize == 0)
                continue; // Avoid division by zero

            AudioFormat candidateFormat = new AudioFormat(targetEncoding, originalSampleRate, sampleSizeInBits,
                    originalChannels, frameSize, originalSampleRate, bigEndian);

            DataLine.Info info = new DataLine.Info(Clip.class, candidateFormat);
            if (AudioSystem.isLineSupported(info)) {
                System.out.println("[ALARM] Found compatible format (bit-depth converted): " + candidateFormat);
                return candidateFormat;
            }
        }

        if (originalChannels == 2) { // Only attempt if original was stereo
            int monoChannels = 1;
            for (int sampleSizeInBits : sampleSizesInBits) {
                int frameSize = monoChannels * (sampleSizeInBits / 8);
                if (frameSize == 0)
                    continue;

            }
        }

        // --- Phase 3: Broader search with common sample rates and channels ---
        float[] commonSampleRates = { 44100.0F, 22050.0F, 11025.0F, 8000.0F };
        int[] commonChannels = { 1, 2 }; // Mono, Stereo

        for (float sampleRate : commonSampleRates) {
            for (int channels : commonChannels) {
                for (int sampleSizeInBits : sampleSizesInBits) {
                    int frameSize = channels * (sampleSizeInBits / 8);
                    if (frameSize == 0)
                        continue;

                    AudioFormat genericCandidate = new AudioFormat(targetEncoding, sampleRate, sampleSizeInBits,
                            channels, frameSize, sampleRate, // Frame rate usually matches sample rate for PCM
                            bigEndian);

                    DataLine.Info info = new DataLine.Info(Clip.class, genericCandidate);
                    if (AudioSystem.isLineSupported(info)) {
                        return genericCandidate;
                    }
                }
            }
        }

        System.out.println("[ALARM] No suitable compatible format found for " + originalFormat);
        return null;
    }

    private void playMp3Sound(String soundFile) {
        if (soundFile == null || soundFile.trim().isEmpty()) {
            System.out.println("[ALARM] ERROR: Sound file path is null or empty");
            logger.warning("MP3 sound file path is null or empty");
            return;
        }

        // Create a new thread for MP3 playback with proper error handling
        soundThread = new Thread(() -> {
            Player localPlayer = null;
            try {
                // Get the resource as a stream
                String resourcePath = soundFile.startsWith("/") ? soundFile : "/" + soundFile;

                InputStream inputStream = getClass().getResourceAsStream(resourcePath);
                if (inputStream == null) {
                    System.out.println("[ALARM] ERROR: Could not load MP3 file from resource path: " + resourcePath);
                    logger.log(Level.SEVERE, "Could not load MP3 file from resource path: " + resourcePath);
                    return;
                }

                // Play the sound up to 3 times while alarm is active
                int loopCount = 0;
                while (loopCount < 3 && !Thread.currentThread().isInterrupted()) {
                    try {
                        // Create a new player for each loop
                        if (localPlayer != null) {
                            try {
                                localPlayer.close();
                            } catch (Exception e) {
                                System.out.println("[ALARM] Warning: Error closing previous MP3 player: " + e.getMessage());
                            }
                        }
                        
                        inputStream = getClass().getResourceAsStream(resourcePath);
                        if (inputStream == null) {
                            System.out.println("[ALARM] ERROR: Could not reload MP3 file for loop " + (loopCount + 1));
                            logger.log(Level.SEVERE, "Could not reload MP3 file for loop " + (loopCount + 1));
                            break;
                        }

                        // Create a new player with error handling
                        try {
                            localPlayer = new Player(inputStream);
                        } catch (Exception e) {
                            System.out.println("[ALARM] ERROR creating MP3 player: " + e.getMessage());
                            logger.log(Level.SEVERE, "Error creating MP3 player: " + e.getMessage(), e);
                            break;
                        }

                        synchronized (LOCK) {
                            currentMp3Player = localPlayer;
                        }

                        // Set the volume to 50% to avoid interfering with other audio
                        try {
                            javax.sound.sampled.Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                            for (javax.sound.sampled.Mixer.Info info : mixerInfo) {
                                javax.sound.sampled.Mixer mixer = AudioSystem.getMixer(info);
                                javax.sound.sampled.Line.Info[] sourceLines = mixer.getSourceLineInfo();
                                if (sourceLines.length > 0) {
                                    javax.sound.sampled.Line line = mixer.getLine(sourceLines[0]);
                                    if (line instanceof javax.sound.sampled.FloatControl) {
                                        javax.sound.sampled.FloatControl volumeControl = (javax.sound.sampled.FloatControl) line;
                                        volumeControl.setValue(volumeControl.getValue() * 0.5f);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("[ALARM] Could not adjust volume: " + e.getMessage());
                            // Continue playing even if volume adjustment fails
                        }

                        // Play with error handling
                        try {
                            localPlayer.play();
                        } catch (Exception e) {
                            System.out.println("[ALARM] ERROR playing MP3: " + e.getMessage());
                            logger.log(Level.SEVERE, "Error playing MP3: " + e.getMessage(), e);
                            break;
                        }

                        // Wait for the player to finish, or if the thread is interrupted
                        while (!Thread.currentThread().isInterrupted()) {
                            synchronized (LOCK) {
                                if (currentMp3Player == null || currentMp3Player.isComplete()) {
                                    break;
                                }
                            }
                            Thread.sleep(100);
                        }

                        // If the thread was interrupted, break out of the loop
                        if (Thread.currentThread().isInterrupted()) {
                            System.out.println("[ALARM] MP3 playback interrupted.");
                            break;
                        }

                    } catch (Exception e) {
                        System.out.println("[ALARM] ERROR during MP3 playback loop " + (loopCount + 1) + ": " + e.getMessage());
                        logger.log(Level.SEVERE, "Error during MP3 playback loop " + (loopCount + 1) + ": " + e.getMessage(), e);
                        break;
                    }
                    loopCount++;

                    // Wait a short time between loops, or if the thread is interrupted
                    if (loopCount < 3 && !Thread.currentThread().isInterrupted()) {
                        Thread.sleep(100);
                    }
                }

            } catch (Exception e) {
                System.out.println("[ALARM] ERROR in MP3 playback thread: " + e.getMessage());
                logger.log(Level.SEVERE, "Error in MP3 playback thread: " + e.getMessage(), e);
                e.printStackTrace();
            } finally {
                try {
                    if (localPlayer != null) {
                        localPlayer.close();
                    }
                    synchronized (LOCK) {
                        if (currentMp3Player == localPlayer) {
                            currentMp3Player = null;
                        }
                        isPlaying = false;
                    }
                } catch (Exception e) {
                    System.out.println("[ALARM] ERROR closing MP3 player: " + e.getMessage());
                    logger.log(Level.WARNING, "Error closing MP3 player: " + e.getMessage(), e);
                }
            }
        });

        // Set thread name for debugging
        soundThread.setName("AlarmManagement-MP3Player");
        
        // Set as daemon thread to prevent blocking JVM shutdown
        soundThread.setDaemon(true);
        
        // Start the playback thread
        soundThread.start();
    }

    public void stopSound() {
        synchronized (LOCK) {
            try {
                isPlaying = false; // Set this first to stop any loops
                currentSoundFile = null;

                if (currentWavClip != null) {
                    currentWavClip.stop();
                    currentWavClip.close();
                    currentWavClip = null;
                }

                if (currentMp3Player != null) {
                    currentMp3Player.close();
                    currentMp3Player = null;
                }

                if (soundThread != null && soundThread.isAlive()) {
                    soundThread.interrupt();
                    soundThread = null;
                }

                // Check for missed appointments after sound stops
                try {
                    if (appointmentManagementRef != null && appointmentManagementRef.getAppointmentDAO() != null) {
                        int updatedCount = appointmentManagementRef.getAppointmentDAO()
                                .checkAndUpdateMissedWithoutSessions();
                        if (updatedCount > 0) {
                            // Refresh views if any appointments were marked as missed
                            EventBus.publish("appointment_status_changed", null);
                        }
                    }
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error checking missed appointments after sound stop: " + e.getMessage(),
                            e);
                }
            } catch (Exception e) {
                System.out.println("[ALARM] ERROR stopping sound: " + e.getMessage());
                logger.log(Level.SEVERE, "Error stopping sound: " + e.getMessage(), e);
            }
        }
    }

    public boolean isPlayingSound() {
        return isPlaying;
    }

    public void showAlarmDialog(Appointment appointment) {
        // Aggressively stop any existing alarm dialog before attempting to show a new
        // one.
        // This handles cases where a dialog might linger due to unexpected closures or
        // timing issues.
        if (alarmDialog != null) {
            stopAlarm(); // This will dispose the dialog and nullify alarmDialog
        }

        if (alarmDialog == null) { // Simplified check: only proceed if the reference is null
            currentAppointment = appointment;
            // Pass this (AlarmManagement) instance to the dialog
            AppointmentAlarmDialog dialog = new AppointmentAlarmDialog(appointment, () -> {
                /*
                 * onStartSession logic will be handled by
                 * appointmentManagement.openSessionFromAlarm
                 */ }, this, // Pass AlarmManagement instance
                    appointmentManagementRef);
            alarmDialog = dialog;
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setResizable(false);
            dialog.setModal(true);

            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    // Always ensure stopAlarm() is called when this specific dialog instance is
                    // closed.
                    // stopAlarm() will handle the cleanup of timers, sound, and nulling the
                    // alarmDialog reference in AlarmManagement.
                    AlarmManagement.this.stopAlarm();
                }
            });

            if (appointment.getAppointmentDateTime().toLocalDateTime().isBefore(LocalDateTime.now())) {
                dialog.showMissedLabel();
            }
            dialog.setVisible(true);
        }
    }

    private void markAppointmentAsDismissed(Appointment appointment) {
        notifiedAppointments.add(appointment.getAppointmentId()); // Mark as notified
        isAlarmActive = false; // Reset alarm active state
        stopSound(); // Stop any playing alarm sound
        if (alarmDialog != null) {
            alarmDialog.dispose();
            alarmDialog = null;
        }
        // Re-schedule alarms to ensure any later alarms are still handled.
        // No, this is handled by the regular checkUpcomingAppointments, or the next alarm scheduling from snoozing.
        // If the appointment is being dismissed, we don't want to immediately re-alarm it.
    }

    public void shutdown() {
        System.out.println("[ALARM] Shutting down AlarmManagement...");
        
        // Stop all sounds first
        stopSound();
        
        // Shutdown scheduler with proper timeout
        if (scheduler != null && !scheduler.isShutdown()) {
            try {
                System.out.println("[ALARM] Shutting down scheduler...");
                scheduler.shutdown();
                
                // Wait for tasks to complete with timeout
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("[ALARM] Forcing scheduler shutdown...");
                    scheduler.shutdownNow();
                    
                    // Wait again for forced shutdown
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.err.println("[ALARM] WARNING: Scheduler did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("[ALARM] Interrupted while shutting down scheduler: " + e.getMessage());
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Stop all timers
        if (alarmTimer != null) {
            alarmTimer.stop();
            alarmTimer = null;
        }
        if (missedAppointmentTimer != null) {
            missedAppointmentTimer.stop();
            missedAppointmentTimer = null;
        }
        if (soundStopTimer != null) {
            soundStopTimer.stop();
            soundStopTimer = null;
        }
        
        // Dispose dialog
        if (alarmDialog != null) {
            alarmDialog.dispose();
            alarmDialog = null;
        }
        
        // Clear collections
        notifiedAppointments.clear();
        lastNotifiedMap.clear();
        debouncingAlarms.clear();
        scheduledAlarms.clear();
        
        // Reset state
        isAlarmActive = false;
        isPlaying = false;
        currentAppointment = null;
        currentSoundFile = null;
        alarmDateTime = null;
        
        System.out.println("[ALARM] AlarmManagement shutdown complete");
    }

    // Add new methods for alarm management
    public void scheduleFinalAlarm(LocalDateTime dateTime, Appointment appointment) {
        if (alarmTimer != null) {
            System.out.println("[ALARM] Stopping previous alarmTimer before scheduling new one.");
            alarmTimer.stop();
            alarmTimer = null;
        }
        if (scheduledAlarms.contains(dateTime)) {
            logger.info("Alarm for " + dateTime + " already scheduled.");
            return; // Don't reschedule if already scheduled
        }

        LocalDateTime now = LocalDateTime.now();
        long delay = ChronoUnit.MILLIS.between(now, dateTime);
        System.out.println("[ALARM] Calculated delay for scheduleFinalAlarm: " + delay + " ms (now=" + now + ", target=" + dateTime + ")");

        // Define a small grace period for appointments slightly in the past
        long GRACE_PERIOD_MILLIS = 5000; // 5 seconds

        if (delay < -GRACE_PERIOD_MILLIS) { // If appointment time is significantly in the past
            logger.warning("Appointment time (" + dateTime + ") is more than " + (GRACE_PERIOD_MILLIS / 1000)
                    + " seconds in the past. Not scheduling alarm.");
            return; // Do not schedule the alarm
        } else if (delay < 0) { // If appointment time is slightly in the past (within grace period)
            delay = 0; // Trigger immediately
            logger.info("Appointment time (" + dateTime + ") is slightly in the past, triggering alarm immediately.");
        }

        alarmTimer = new Timer((int) delay, e -> {
            System.out.println("[ALARM] Timer fired for snoozed appointment: " + (appointment != null ? appointment.getAppointmentTitle() : "null"));
            snoozingInProgress = false; // Clear the flag now that the timer has fired
            SwingUtilities.invokeLater(() -> {
                if (callback != null && appointment != null) {
                    System.out.println("[ALARM] Calling onAlarmTriggered for: " + appointment.getAppointmentTitle());
                    try {
                        callback.onAlarmTriggered(appointment);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("[ALARM] Exception in onAlarmTriggered: " + ex.getMessage());
                    }
                } else {
                    System.out.println("[ALARM] Callback or appointment is null in timer!");
                }
            });
        });
        System.out.println("[ALARM] Created new alarmTimer with delay: " + delay + " ms");
        alarmTimer.setRepeats(false);
        alarmTimer.start();

        scheduledAlarms.add(dateTime); // Add to scheduled alarms
        this.alarmDateTime = dateTime; // Set the scheduled alarm date-time
        logger.info("Final alarm scheduled for: " + dateTime);

        if (callback != null) {
            callback.onAlarmScheduled(dateTime);
        }
    }

    public void cancelAlarm() {
        isAlarmActive = false;
        stopSound();
        if (alarmDialog != null) {
            alarmDialog.dispose();
            alarmDialog = null;
        }
        if (missedAppointmentTimer != null) { // Ensure missed timer is stopped on cancel
            missedAppointmentTimer.stop();
        }
        if (soundStopTimer != null) { // Ensure sound stop timer is stopped on cancel
            soundStopTimer.stop();
        }
        if (callback != null) {
            callback.onAlarmStopped();
        }
    }

    public void snooze(int minutes) {
        if (currentAppointment != null) {
            // For debugging, use 10 seconds instead of minutes
            int snoozeSeconds = 10;
            LocalDateTime newAlarmTime = LocalDateTime.now().plusSeconds(snoozeSeconds);
            Appointment snoozedAppointment = currentAppointment;
            System.out.println("[ALARM] Snoozing alarm for: " + snoozedAppointment.getAppointmentTitle() + " until " + newAlarmTime + " (" + snoozeSeconds + " seconds)");
            stopSound(); // Stop current sound
            // Set snoozing flag BEFORE closing dialog
            snoozingInProgress = true;
            if (alarmDialog != null) {
                alarmDialog.dispose(); // Close the current dialog
                alarmDialog = null; // Clear the reference
            }
            isAlarmActive = false; // Reset alarm active state

            // Allow this alarm to be re-triggered after the snooze period by removing it from the debounce set
            debouncingAlarms.remove(snoozedAppointment.getAppointmentId());

            // Remove from notified appointments to allow re-triggering at new time
            notifiedAppointments.remove(snoozedAppointment.getAppointmentId());

            if (callback != null) {
                callback.onAlarmSnoozed(newAlarmTime);
            }

            // --- FIX: Schedule a one-off alarm for the snoozed time with the correct appointment ---
            scheduleFinalAlarm(newAlarmTime, snoozedAppointment);
        }
    }

    public LocalDateTime getAlarmDateTime() {
        return alarmDateTime;
    }

    public void stopAlarm() {
        synchronized (LOCK) {
            System.out.println("[ALARM] stopAlarm() called. Stopping alarmTimer and cleaning up.");
            isAlarmActive = false; // Set this first to stop sound loops
            stopSound();
            // Only stop the timer if not snoozing
            if (!snoozingInProgress && alarmTimer != null) {
                System.out.println("[ALARM] stopAlarm() is stopping alarmTimer (not snoozing).");
                alarmTimer.stop();
                alarmTimer = null;
            } else if (snoozingInProgress) {
                System.out.println("[ALARM] stopAlarm() detected snoozingInProgress, NOT stopping alarmTimer.");
            }
            if (alarmDialog != null) {
                alarmDialog.dispose();
                alarmDialog = null;
            }
            if (missedAppointmentTimer != null) {
                missedAppointmentTimer.stop();
                missedAppointmentTimer = null;
            }
            if (soundStopTimer != null) {
                soundStopTimer.stop();
                soundStopTimer = null;
            }
            currentAppointment = null;
            SwingUtilities.invokeLater(() -> {
                if (callback != null) {
                    callback.onAlarmStopped();
                }
            });
        }
    }

    // New centralized method to handle starting a session from the alarm
    public void handleStartSession(Appointment appointment) {
        try {
            // Set appointment status to "Starting"
            appointment.setAppointmentStatus("Starting");
            Main.appointmentCalendar.appointmentDAO.updateAppointment(appointment);
            Main.appointmentCalendar.refreshViews(); // Refresh views to reflect status change

            if (appointmentManagementRef != null) {
                appointmentManagementRef.openSessionFromAlarm(appointment);
            } else {
                JOptionPane.showMessageDialog(alarmDialog, // Use alarmDialog as parent for consistent behavior
                        "Appointment management form is not available. Please try opening the session manually.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error starting session from alarm", ex);
            JOptionPane.showMessageDialog(alarmDialog, "Error starting session: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            stopAlarm(); // Always stop the alarm after handling
        }
    }

    // New centralized method to handle snoozing from the alarm
    public void handleSnooze(int minutes) {
        snooze(minutes); // Call the existing snooze logic
        // stopAlarm() is already called within the snooze method's finally block
    }

    // New centralized method to handle canceling from the alarm
    public void handleCancel(Appointment appointment) {
        JDialog currentAlarmDialog = alarmDialog; // Store reference to the alarm dialog before it's disposed

        int confirm = JOptionPane.showConfirmDialog(currentAlarmDialog, // Use the stored reference as parent
                "Are you sure you want to cancel this appointment?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        stopAlarm(); // Dismiss the alarm dialog immediately after user interaction

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Update appointment status to 'Cancelled'
                appointment.setAppointmentStatus("Cancelled");
                Main.appointmentCalendar.appointmentDAO.updateAppointment(appointment);
                Main.appointmentCalendar.refreshViews(); // Refresh views
                JOptionPane.showMessageDialog(null, "Appointment cancelled successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE); // Use null parent
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error cancelling appointment from alarm", ex);
                JOptionPane.showMessageDialog(null, "Error cancelling appointment: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE); // Use null parent
            }
        }
    }

    public void testAudioSystem() {
        InputStream mp3Stream = null;
        Player player = null;

        try {
            System.out.println("[ALARM] Testing audio system...");

            // Test MP3 playback
            System.out.println("[ALARM] Testing MP3 playback...");
            String testMp3Path = "/sounds/alarm_tone/Advanced_Bell.mp3";
            mp3Stream = getClass().getResourceAsStream(testMp3Path);
            if (mp3Stream == null) {
                System.out.println("[ALARM] ERROR: Test MP3 file not found!");
                return; // Exit early
            }

            player = new Player(mp3Stream);
            player.play();
            System.out.println("[ALARM] MP3 test sound started");

            // Wait for the player to finish
            while (!player.isComplete()) {
                Thread.sleep(100);
            }
            System.out.println("[ALARM] MP3 test completed");

        } catch (Exception e) { // Catch any unexpected exceptions during MP3 playback
            System.out.println("[ALARM] ERROR testing audio system: " + e.getMessage());
            logger.log(Level.SEVERE, "General error during audio test: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            // Ensure all resources are closed
            try {
                if (player != null) {
                    player.close();
                }
                if (mp3Stream != null) {
                    mp3Stream.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Error closing audio streams in testAudioSystem: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Clears the notified status for a specific appointment, allowing its alarm to be re-triggered.
     * This is useful when an appointment's details (like date/time or status) are updated.
     * @param appointmentId The ID of the appointment to clear the notified status for.
     */
    public void clearNotifiedStatus(int appointmentId) {
        notifiedAppointments.remove(appointmentId);
        System.out.println("[ALARM] Cleared notified status for appointment ID: " + appointmentId);
    }

    // TODO: Persist lastNotifiedMap to DB for robustness across restarts
}
