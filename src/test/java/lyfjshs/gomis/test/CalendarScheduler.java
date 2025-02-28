package lyfjshs.gomis.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
 
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
 
import com.formdev.flatlaf.FlatLightLaf;
 
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
 
public class CalendarScheduler {
    private static final Map<String, JPanel> eventSlots = new LinkedHashMap<>();
    private static final Map<String, String> events = new HashMap<>();
    private static final String[] times = {
            "9 AM", "10 AM", "11 AM", "12 PM", "1 PM", "2 PM", "3 PM"
    };
 
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(CalendarScheduler::createAndShowGUI);
    }
 
    /**
     * @wbp.parser.entryPoint
     */
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Daily Schedule");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new MigLayout("fill"));
 
        // Top Panel
        JPanel topPanel = new JPanel(new MigLayout("insets 5 10 5 10"));
        topPanel.setBackground(new Color(240, 240, 240));
 
        JButton menuButton = new JButton("☰");
        styleButton(menuButton);
        JButton dayButton = new JButton("Day");
        styleButton(dayButton);
        JButton weekButton = new JButton("Week");
        styleButton(weekButton);
        JButton monthButton = new JButton("Month");
        styleButton(monthButton);
 
        JLabel dateLabel = new JLabel("February 12, 2025");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
        topPanel.add(menuButton, "split 5");
        topPanel.add(dayButton);
        topPanel.add(weekButton);
        topPanel.add(monthButton);
        topPanel.add(dateLabel, "push, align center");
 
        frame.add(topPanel, "north");
 
        // Schedule Panel
        JPanel schedulePanel = new JPanel(new MigLayout("insets 0", "[50][grow,fill]"));
        schedulePanel.setBackground(Color.WHITE);
 
        // Time labels panel with grid lines
        JPanel timeLabelsPanel = new JPanel(new MigLayout("insets 0, gap 0", "[50]", ""));
        timeLabelsPanel.setBackground(Color.WHITE);
 
        // Add time labels with grid lines
        for (int i = 0; i < times.length; i++) {
            JPanel timeSlotPanel = new JPanel(new MigLayout("insets 0, gap 0"));
            timeSlotPanel.setBackground(Color.WHITE);
            timeSlotPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
 
            JLabel timeLabel = new JLabel(times[i]);
            timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            timeLabel.setPreferredSize(new Dimension(45, 50));
 
            timeSlotPanel.add(timeLabel, "gapright 5");
            timeLabelsPanel.add(timeSlotPanel, "wrap, height 50!");
        }
 
        schedulePanel.add(timeLabelsPanel, "cell 0 0, growy");
 
        // Events panel with grid lines
        JPanel eventsPanel = new JPanel(null);
        eventsPanel.setBackground(Color.WHITE);
        eventsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        eventsPanel.setPreferredSize(new Dimension(630, times.length * 50));
 
        // Add horizontal grid lines to events panel
        for (int i = 0; i < times.length; i++) {
            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setForeground(Color.LIGHT_GRAY);
            separator.setBounds(0, (i + 1) * 50 - 1, 630, 1);
            eventsPanel.add(separator);
        }
 
        schedulePanel.add(eventsPanel, "cell 1 0, grow");
 
        JScrollPane scrollPane = new JScrollPane(schedulePanel);
        frame.add(scrollPane, "center, grow");
 
        // Bottom Panel
        JPanel bottomPanel = new JPanel(new MigLayout("insets 5 10 5 10, center"));
        bottomPanel.setBackground(new Color(240, 240, 240));
 
        JButton addEventButton = new JButton("Add Event");
        styleButton(addEventButton);
        JButton removeEventButton = new JButton("Remove Event");
        styleButton(removeEventButton);
 
        bottomPanel.add(addEventButton, "gap 20");
        bottomPanel.add(removeEventButton);
 
        frame.add(bottomPanel, "south");
 
        addEventButton.addActionListener(e -> addNewEvent(schedulePanel));
        removeEventButton.addActionListener(e -> removeExistingEvent(schedulePanel));
 
        frame.setVisible(true);
    }
 
    private static void styleButton(JButton button) {
        button.setBackground(new Color(70, 130, 180)); // Steel blue
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 30)); // Dagdagan ang width ng button
 
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 149, 237)); // Cornflower blue
            }
 
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
    }
 
    private static void addNewEvent(JPanel schedulePanel) {
        JDialog dialog = new JDialog((Frame) null, "Add New Event", true);
        dialog.setLayout(new MigLayout("wrap 2", "[grow,fill]", "[]10[]10[]10[]"));
 
        JLabel dateLabel = new JLabel("Select Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.now();
        JFormattedTextField dateEditor = new JFormattedTextField();
        datePicker.setEditor(dateEditor);
 
        JLabel startTimeLabel = new JLabel("Start Time:");
        TimePicker startTimePicker = new TimePicker();
        startTimePicker.now();
        JFormattedTextField startTimeEditor = new JFormattedTextField();
        startTimePicker.setEditor(startTimeEditor);
 
        JLabel endTimeLabel = new JLabel("End Time:");
        TimePicker endTimePicker = new TimePicker();
        endTimePicker.now();
        JFormattedTextField endTimeEditor = new JFormattedTextField();
        endTimePicker.setEditor(endTimeEditor);
 
        JLabel eventNameLabel = new JLabel("Event Name:");
        JTextField eventNameField = new JTextField();
        JButton submitButton = new JButton("Add Event");
 
        dialog.add(dateLabel);
        dialog.add(dateEditor);
        dialog.add(startTimeLabel);
        dialog.add(startTimeEditor, "width 250");
        dialog.add(endTimeLabel);
        dialog.add(endTimeEditor, "width 250");
        dialog.add(eventNameLabel);
        dialog.add(eventNameField);
        dialog.add(submitButton, "span, center");
 
        submitButton.addActionListener((ActionEvent e) -> {
            LocalDate selectedDate = datePicker.getSelectedDate();
            LocalTime startTime = startTimePicker.getSelectedTime();
            LocalTime endTime = endTimePicker.getSelectedTime();
            String eventName = eventNameField.getText().trim();
 
            if (selectedDate == null || startTime == null || endTime == null || eventName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled out!");
                return;
            }
 
            if (endTime.isBefore(startTime)) {
                JOptionPane.showMessageDialog(dialog, "End time must be after start time!");
                return;
            }
 
            // Find the events panel
            Component[] components = schedulePanel.getComponents();
            JPanel eventsPanel = (JPanel) components[1];
 
            // Create event panel
            JPanel eventPanel = new JPanel(new MigLayout("fillx, insets 5"));
            eventPanel.setBackground(new Color(135, 206, 250));
            eventPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
 
            // Calculate position and height based on actual times
            int startHour = startTime.getHour();
            int startMinute = startTime.getMinute();
            int endHour = endTime.getHour();
            int endMinute = endTime.getMinute();
 
            // Calculate pixels from top (each hour = 50 pixels)
            int startY = (startHour - 9) * 50 + (startMinute * 50 / 60);
            int endY = (endHour - 9) * 50 + (endMinute * 50 / 60);
            int height = endY - startY;
 
            // Add event information
            JLabel eventLabel = new JLabel(eventName);
            eventLabel.setFont(new Font("Arial", Font.BOLD, 12));
 
            String timeRange = String.format("%s - %s", 
                startTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                endTime.format(DateTimeFormatter.ofPattern("h:mm a")));
            JLabel timeLabel = new JLabel(timeRange);
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
 
            eventPanel.add(eventLabel, "wrap");
            eventPanel.add(timeLabel);
 
            eventsPanel.add(eventPanel);
            int width = eventsPanel.getWidth() - 10;
            eventPanel.setBounds(5, startY, width, height);
 
            // Store the event
            events.put(timeRange, eventName);
 
            eventsPanel.revalidate();
            eventsPanel.repaint();
            schedulePanel.revalidate();
            schedulePanel.repaint();
 
            dialog.dispose();
        });
 
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
 
    private static String convertTo12HourFormat(int hour) {
        if (hour == 0) return "12 AM";
        if (hour == 12) return "12 PM";
        if (hour > 12) return (hour - 12) + " PM";
        return hour + " AM";
    }
 
    private static void removeExistingEvent(JPanel schedulePanel) {
        JDialog dialog = new JDialog((Frame) null, "Remove Event", true);
        dialog.setLayout(new MigLayout("wrap 2", "[grow,fill]", "[]10[]10[]10[]"));
 
        JLabel dateLabel = new JLabel("Select Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.now();
        JFormattedTextField dateEditor = new JFormattedTextField();
        datePicker.setEditor(dateEditor);
 
        JLabel startTimeLabel = new JLabel("Start Time:");
        TimePicker startTimePicker = new TimePicker();
        startTimePicker.now();
        JFormattedTextField startTimeEditor = new JFormattedTextField();
        startTimePicker.setEditor(startTimeEditor);
 
        JLabel endTimeLabel = new JLabel("End Time:");
        TimePicker endTimePicker = new TimePicker();
        endTimePicker.now();
        JFormattedTextField endTimeEditor = new JFormattedTextField();
        endTimePicker.setEditor(endTimeEditor);
 
        JButton removeButton = new JButton("Remove Event");
        styleButton(removeButton);
 
        dialog.add(dateLabel);
        dialog.add(dateEditor);
        dialog.add(startTimeLabel);
        dialog.add(startTimeEditor);
        dialog.add(endTimeLabel);
        dialog.add(endTimeEditor);
        dialog.add(removeButton, "span, center");
 
        removeButton.addActionListener((ActionEvent e) -> {
            LocalDate selectedDate = datePicker.getSelectedDate();
            LocalTime startTime = startTimePicker.getSelectedTime();
            LocalTime endTime = endTimePicker.getSelectedTime();
 
            if (selectedDate == null || startTime == null || endTime == null) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled out!");
                return;
            }
 
            if (endTime.isBefore(startTime)) {
                JOptionPane.showMessageDialog(dialog, "End time must be after start time!");
                return;
            }
 
            // Convert times to hour only for slot matching
            int startHour = startTime.getHour();
            int endHour = endTime.getHour();
 
            // Convert 24-hour format to slot format strings
            String startSlot = convertTo12HourFormat(startHour);
            String endSlot = convertTo12HourFormat(endHour);
 
            int startIndex = Arrays.asList(times).indexOf(startSlot);
            int endIndex = Arrays.asList(times).indexOf(endSlot);
 
            if (startIndex == -1 || endIndex == -1) {
                JOptionPane.showMessageDialog(dialog, "Selected time must be between 9 AM and 3 PM!");
                return;
            }
 
            // Check if there are any events in the selected time range
            boolean hasEvents = false;
            for (int i = startIndex; i <= endIndex; i++) {
                if (events.containsKey(times[i])) {
                    hasEvents = true;
                    break;
                }
            }
 
            if (!hasEvents) {
                JOptionPane.showMessageDialog(dialog, "No events found in the selected time range!");
                return;
            }
 
            // Find the events panel
            Component[] components = schedulePanel.getComponents();
            JPanel eventsPanel = (JPanel) components[1];
 
            // Remove events from the selected time slots
            for (int i = startIndex; i <= endIndex; i++) {
                JPanel timeSlot = eventSlots.get(times[i]);
                timeSlot.removeAll();
                timeSlot.setBackground(Color.WHITE); // Reset background color
 
                // Remove from events map
                events.remove(times[i]);
 
                timeSlot.revalidate();
                timeSlot.repaint();
            }
 
            dialog.dispose();
        });
 
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
