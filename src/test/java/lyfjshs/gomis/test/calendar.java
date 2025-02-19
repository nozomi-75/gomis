package lyfjshs.gomis.test;


    
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
 
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeMap;
 
public class calendar extends JPanel {
    private JLabel monthYearLabel;
    private JPanel calendarPanel;
    private JPanel[][] dayPanels;
    private JButton[][] dayButtons;
    private Calendar currentCalendar;
    private JButton prevButton, nextButton;
    private final String[] DAYS_OF_WEEK = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private final Color HIGHLIGHT_COLOR = new Color(97, 49, 237);
    private final Color INACTIVE_COLOR = new Color(169, 169, 169);
    private final Color ACTIVE_COLOR = new Color(68, 68, 68);
    private JPanel appointmentsPanel;
    private final Color PRIMARY_COLOR = new Color(99, 102, 241); // Purple color
    private final Color LIGHT_PURPLE = new Color(237, 238, 252);
    private final Color TEXT_GRAY = new Color(107, 114, 128);
    private final Color EVENT_RED = new Color(254, 226, 226);
    private final Color EVENT_GREEN = new Color(220, 252, 231);
    private JToggleButton monthViewBtn, weekViewBtn, dayViewBtn;
    private Map<String, List<Appointment>> appointments = new HashMap<>();
    private JButton appointmentOverviewBtn;
    private JDialog overviewDialog;
 
    public class Appointment {
        String title;
        String time;
        Color color;
        public String appointmentId;
        public String participantId;
        public String counselorId;
        public String appointmentType;
        public String updatedAt;
        public String endTime;
        public String startTime;
 
        public Appointment(String title, String time, Color color) {
            this.title = title;
            this.time = time;
            this.color = color;
        }
 
        public Appointment(String eventName, String startTime, String status, Object object) {
            // TODO Auto-generated constructor stub
        }
    }
 
    public calendar() {
        setLayout(new BorderLayout(0, 10));
        currentCalendar = Calendar.getInstance();
        initComponents();
        updateCalendar();
    }
 
    private void initComponents() {
        setBackground(Color.WHITE);
 
        // Navigation Panel with View Selector
        JPanel navigationPanel = new JPanel(new BorderLayout(10, 0));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        navigationPanel.setBackground(Color.WHITE);
 
        // Left side: Month/Year and Navigation
        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftNav.setBackground(Color.WHITE);
 
        // Add view selector buttons
        JPanel viewSelectorPanel = new JPanel();
        viewSelectorPanel.setBackground(Color.WHITE);
        viewSelectorPanel.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
 
        dayViewBtn = createViewButton("Day");
        weekViewBtn = createViewButton("Week");
        monthViewBtn = createViewButton("Month");
        monthViewBtn.setSelected(true); // Default to month view
 
        viewSelectorPanel.add(dayViewBtn);
        viewSelectorPanel.add(weekViewBtn);
        viewSelectorPanel.add(monthViewBtn);
 
        leftNav.add(viewSelectorPanel);
 
        // Add existing navigation components
        prevButton = createNavButton("←");
        nextButton = createNavButton("→");
        monthYearLabel = new JLabel("", SwingConstants.LEFT);
        monthYearLabel.setFont(new Font("Inter", Font.BOLD, 18));
        monthYearLabel.setForeground(TEXT_GRAY);
 
        leftNav.add(monthYearLabel);
        leftNav.add(prevButton);
        leftNav.add(nextButton);
 
        // Right side: Appointment Overview button
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightNav.setBackground(Color.WHITE);
 
        appointmentOverviewBtn = new JButton("Appointment Overview");
        appointmentOverviewBtn.setFont(new Font("Inter", Font.PLAIN, 14));
        appointmentOverviewBtn.setBackground(PRIMARY_COLOR);
        appointmentOverviewBtn.setForeground(Color.WHITE);
        appointmentOverviewBtn.setBorderPainted(false);
        appointmentOverviewBtn.setFocusPainted(false);
        appointmentOverviewBtn.addActionListener(e -> showAppointmentOverview());
 
        rightNav.add(appointmentOverviewBtn);
 
        navigationPanel.add(leftNav, BorderLayout.WEST);
        navigationPanel.add(rightNav, BorderLayout.EAST);
 
        // Calendar Grid
        calendarPanel = new JPanel(new GridLayout(7, 7, 1, 1));
        calendarPanel.setBackground(new Color(243, 244, 246));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
        dayPanels = new JPanel[6][7];
        dayButtons = new JButton[6][7];
 
        // Add day headers
        for (String day : DAYS_OF_WEEK) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Inter", Font.BOLD, 12));
            label.setForeground(TEXT_GRAY);
            calendarPanel.add(label);
        }
 
        // Add appointments panel
        appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setPreferredSize(new Dimension(400, 100));
 
        // Create day panels and buttons
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                dayPanels[row][col] = new JPanel();
                dayPanels[row][col].setBackground(Color.WHITE);
 
                dayButtons[row][col] = new JButton();
                dayButtons[row][col].setBorderPainted(false);
                dayButtons[row][col].setContentAreaFilled(false);
                dayButtons[row][col].setFocusPainted(false);
 
                final int finalRow = row;
                final int finalCol = col;
 
                // Add mouse listeners for hover effect
                dayButtons[row][col].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (dayButtons[finalRow][finalCol].isEnabled()) {
                            dayButtons[finalRow][finalCol].setContentAreaFilled(true);
                            dayButtons[finalRow][finalCol].setBackground(LIGHT_PURPLE);
                        }
                    }
 
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!dayButtons[finalRow][finalCol].isSelected()) {
                            dayButtons[finalRow][finalCol].setContentAreaFilled(false);
                            dayButtons[finalRow][finalCol].setBackground(null);
                        }
                    }
                });
 
                // Add click listener
                dayButtons[row][col].addActionListener(e -> {
                    JButton clickedButton = (JButton) e.getSource();
                    String dayText = clickedButton.getText();
 
                    Calendar selectedDate = (Calendar) currentCalendar.clone();
                    selectedDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayText));
 
                    showAppointmentDialog(selectedDate);
                });
 
                dayPanels[row][col].add(dayButtons[row][col]);
                calendarPanel.add(dayPanels[row][col]);
            }
        }
 
        // Add components
        add(navigationPanel, BorderLayout.NORTH);
        add(calendarPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
 
        // Add listeners
        prevButton.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });
 
        nextButton.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }
 
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 16));
        button.setForeground(TEXT_GRAY);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }
 
    private JToggleButton createViewButton(String text) {
        JToggleButton button = new JToggleButton(text);
        button.setFont(new Font("Inter", Font.PLAIN, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
 
 
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.isSelected()) {
                    button.setBackground(LIGHT_PURPLE);
                }
            }
 
            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.isSelected()) {
                    button.setBackground(Color.WHITE);
                }
            }
        });
 
        button.addActionListener(e -> updateViewSelection(button));
        return button;
    }
 
    private void updateViewSelection(JToggleButton selected) {
        dayViewBtn.setSelected(false);
        weekViewBtn.setSelected(false);
        monthViewBtn.setSelected(false);
        dayViewBtn.setBackground(Color.WHITE);
        weekViewBtn.setBackground(Color.WHITE);
        monthViewBtn.setBackground(Color.WHITE);
 
 
 
        if (selected == monthViewBtn) {
            // Implement month view logic
            updateCalendar();
        } else if (selected == weekViewBtn) {
            // Implement week view logic
            // TODO: Add week view implementation
        } else {
            // Implement day view logic
            // TODO: Add day view implementation
        }
    }
 
    private void updateCalendar() {
        // Update month/year label
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        monthYearLabel.setText(sdf.format(currentCalendar.getTime()));
     
        // Get current date info
        Calendar today = Calendar.getInstance();
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentYear = currentCalendar.get(Calendar.YEAR);
     
        // Reset calendar to first day of month
        Calendar temp = (Calendar) currentCalendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK) - 1;
        temp.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);
     
        // Fill calendar
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                JButton button = dayButtons[row][col];
                JPanel panel = dayPanels[row][col];
     
                // Set the day number
                button.setText(String.valueOf(temp.get(Calendar.DAY_OF_MONTH)));
     
                // Enable/disable buttons based on current month
                boolean isCurrentMonth = temp.get(Calendar.MONTH) == currentMonth;
                button.setEnabled(isCurrentMonth);
     
                // Style button based on whether it's in current month
                button.setForeground(isCurrentMonth ? TEXT_GRAY : Color.LIGHT_GRAY);
                button.setCursor(isCurrentMonth ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
     
                // Clear previous content
                panel.removeAll();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                panel.setBackground(Color.WHITE);
     
                // Add the day button
                button.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(button);
     
                // Highlight today's date
                boolean isToday = temp.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                    temp.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                    temp.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
     
                if (isToday) {
                    button.setForeground(Color.WHITE);
                    button.setBackground(PRIMARY_COLOR);
                    button.setOpaque(true);
                } else {
                    button.setBackground(null);
                    button.setOpaque(false);
                }
     
                // Add appointments for this date
                String dateKey = String.format("%d-%d-%d", 
                    temp.get(Calendar.YEAR),
                    temp.get(Calendar.MONTH),
                    temp.get(Calendar.DAY_OF_MONTH));
     
                List<Appointment> dateAppointments = appointments.get(dateKey);
                if (dateAppointments != null) {
                    for (Appointment apt : dateAppointments) {
                        JLabel event = new JLabel(apt.title + " - " + apt.time);
                        event.setOpaque(true);
                        event.setBackground(apt.color);
                        event.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
                        event.setFont(new Font("Inter", Font.PLAIN, 11));
                        event.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panel.add(Box.createVerticalStrut(2));
                        panel.add(event);
                    }
                }
     
                temp.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
     
        // Revalidate and repaint the calendar panel to reflect changes
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }
 
 
    private void showAppointmentDialog(Calendar selectedDate) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add Appointment");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
 
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 
        // Date Label
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        JLabel dateLabel = new JLabel(sdf.format(selectedDate.getTime()));
        dateLabel.setFont(new Font("Inter", Font.BOLD, 16));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(dateLabel);
        mainPanel.add(Box.createVerticalStrut(10));
 
        JTextField titleField = new JTextField(20);
        JTextField timeField = new JTextField("hh:mm", 20); // Default time format
 
        addFormField(mainPanel, "Title", titleField);
        addFormField(mainPanel, "Time", timeField);
 
 
        // Participant Table
        String[] columns = {"Participant Name", "Participant Type"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane);
 
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Left align buttons
        JButton addStudentButton = new JButton("Add Student");
        JButton addNonStudentButton = new JButton("Add Non-Student");
 
        addStudentButton.addActionListener(e -> showSearchStudentDialog(dialog, model));
        addNonStudentButton.addActionListener(e -> showAddNonStudentDialog(dialog, model));
 
        buttonPanel.add(addStudentButton);
        buttonPanel.add(addNonStudentButton);
        mainPanel.add(buttonPanel);
 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Right align Save/Cancel
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
 
        saveButton.addActionListener(e -> {
            String title = titleField.getText();
            String time = timeField.getText();
 
            if (title.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
 
            String dateKey = String.format("%d-%d-%d",
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
 
            List<Appointment> dayAppointments = appointments.computeIfAbsent(dateKey, k -> new ArrayList<>());
 
            for (int i = 0; i < table.getRowCount(); i++) {
                String name = (String) table.getValueAt(i, 0);
                String type = (String) table.getValueAt(i, 1);
                Color color = type.equals("Student") ? EVENT_GREEN : EVENT_RED; // Color by type
                dayAppointments.add(new Appointment(title, time, color));
            }
 
            updateCalendar();
            dialog.dispose();
        });
 
        cancelButton.addActionListener(e -> dialog.dispose());
 
        bottomPanel.add(saveButton);
        bottomPanel.add(cancelButton);
        mainPanel.add(bottomPanel);
 
        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this); // Center relative to calendar
        dialog.setVisible(true);
    }
 
 
 
    private void showSearchStudentDialog(JDialog parent, DefaultTableModel model) {
        JDialog searchDialog = new JDialog(parent, "Search Student", true);
        searchDialog.setSize(300, 150);
        searchDialog.setLayout(new FlowLayout());
 
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
 
        searchDialog.add(new JLabel("Enter Student Name:"));
        searchDialog.add(searchField);
        searchDialog.add(searchButton);
 
        searchButton.addActionListener(e -> {
            String studentName = searchField.getText().trim();
            if (!studentName.isEmpty()) {
                model.addRow(new Object[]{studentName, "Student"});
                searchDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(searchDialog, "Please enter a student name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
 
        searchDialog.setLocationRelativeTo(parent);
        searchDialog.setVisible(true);
    }
 
    private void showAddNonStudentDialog(JDialog parent, DefaultTableModel model) {
        JDialog dialog = new JDialog(parent, "Add Non-Student", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridBagLayout());
 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
 
        JLabel lblFirstName = new JLabel("First Name");
        JTextField txtFirstName = new JTextField(20);
        JLabel lblLastName = new JLabel("Last Name");
        JTextField txtLastName = new JTextField(20);
        JLabel lblEmail = new JLabel("Email");
        JTextField txtEmail = new JTextField(20);
        JLabel lblContact = new JLabel("Contact Number");
        JTextField txtContact = new JTextField(20);
        JButton btnAddParticipant = new JButton("Add Participant");
 
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(lblFirstName, gbc);
        gbc.gridx = 1;
        dialog.add(txtFirstName, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(lblLastName, gbc);
        gbc.gridx = 1;
        dialog.add(txtLastName, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(lblEmail, gbc);
        gbc.gridx = 1;
        dialog.add(txtEmail, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(lblContact, gbc);
        gbc.gridx = 1;
        dialog.add(txtContact, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(btnAddParticipant, gbc);
 
        btnAddParticipant.addActionListener(e -> {
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                model.addRow(new Object[]{firstName + " " + lastName, "Non-Student"});
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "First Name and Last Name are required.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
 
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
 
 
    private void addFormField(JPanel panel, String label, JComponent field) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
        JLabel labelComponent = new JLabel(label);
        labelComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
 
        fieldPanel.add(labelComponent);
        fieldPanel.add(Box.createVerticalStrut(5));
        fieldPanel.add(field);
        fieldPanel.add(Box.createVerticalStrut(10));
 
        panel.add(fieldPanel);
    }
 
    private void showAppointmentOverview() {
        overviewDialog = new JDialog();
        overviewDialog.setTitle("Appointment Overview");
        overviewDialog.setModal(true);
        overviewDialog.setLayout(new BorderLayout());
 
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
 
        // Header
        JLabel headerLabel = new JLabel("Your Appointments");
        headerLabel.setFont(new Font("Inter", Font.BOLD, 24));
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createVerticalStrut(20));
 
        // Appointments list
        JPanel appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        appointmentsPanel.setBackground(Color.WHITE);
 
        // Group appointments by date
        Map<String, List<Appointment>> sortedAppointments = new TreeMap<>(appointments);
 
        for (Map.Entry<String, List<Appointment>> entry : sortedAppointments.entrySet()) {
            String dateKey = entry.getKey();
            List<Appointment> dateAppointments = entry.getValue();
 
            // Create date section
            Calendar cal = Calendar.getInstance();
            String[] dateParts = dateKey.split("-");
            cal.set(Integer.parseInt(dateParts[0]), 
                   Integer.parseInt(dateParts[1]), 
                   Integer.parseInt(dateParts[2]));
 
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            JLabel dateLabel = new JLabel(sdf.format(cal.getTime()));
            dateLabel.setFont(new Font("Inter", Font.BOLD, 16));
            dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            appointmentsPanel.add(dateLabel);
            appointmentsPanel.add(Box.createVerticalStrut(10));
 
            // Add appointments for this date
            for (Appointment apt : dateAppointments) {
                JPanel appointmentCard = createAppointmentCard(dateKey, apt);
                appointmentCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                appointmentsPanel.add(appointmentCard);
                appointmentsPanel.add(Box.createVerticalStrut(10));
            }
 
            appointmentsPanel.add(Box.createVerticalStrut(20));
        }
 
        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane);
 
        overviewDialog.add(mainPanel);
        overviewDialog.setSize(600, 500);
        overviewDialog.setLocationRelativeTo(null);
        overviewDialog.setVisible(true);
    }
 
    private JPanel createAppointmentCard(String dateKey, Appointment apt) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
 
        // Left: Appointment info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setBackground(Color.WHITE);
 
        JLabel titleLabel = new JLabel(apt.title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
 
        JLabel timeLabel = new JLabel(apt.time);
        timeLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        timeLabel.setForeground(TEXT_GRAY);
 
        infoPanel.add(titleLabel);
        infoPanel.add(timeLabel);
 
        // Right: Management buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
 
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editAppointment(dateKey, apt));
 
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteAppointment(dateKey, apt));
 
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
 
        card.add(infoPanel, BorderLayout.WEST);
        card.add(buttonPanel, BorderLayout.EAST);
 
        return card;
    }
 
    private void editAppointment(String dateKey, Appointment apt) {
        // Show edit dialog
        JDialog editDialog = new JDialog(overviewDialog);
        editDialog.setTitle("Edit Appointment");
        editDialog.setModal(true);
        editDialog.setLayout(new BorderLayout());
 
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
        JTextField titleField = new JTextField(apt.title, 20);
        JTextField timeField = new JTextField(apt.time, 20);
 
        addFormField(panel, "Title", titleField);
        addFormField(panel, "Time", timeField);
 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
 
        saveButton.addActionListener(e -> {
            apt.title = titleField.getText();
            apt.time = timeField.getText();
            updateCalendar();
            editDialog.dispose();
            overviewDialog.dispose();
            showAppointmentOverview();
        });
 
        cancelButton.addActionListener(e -> editDialog.dispose());
 
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);
 
        editDialog.add(panel);
        editDialog.pack();
        editDialog.setLocationRelativeTo(overviewDialog);
        editDialog.setVisible(true);
    }
 
    private void deleteAppointment(String dateKey, Appointment apt) {
        int confirm = JOptionPane.showConfirmDialog(
            overviewDialog,
            "Are you sure you want to delete this appointment?",
            "Delete Appointment",
            JOptionPane.YES_NO_OPTION
        );
 
        if (confirm == JOptionPane.YES_OPTION) {
            appointments.get(dateKey).remove(apt);
            if (appointments.get(dateKey).isEmpty()) {
                appointments.remove(dateKey);
            }
            updateCalendar();
            overviewDialog.dispose();
            showAppointmentOverview();
        }
    }
 
    public static void main(String[] args) {
        JFrame frame = new JFrame("Calendar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new calendar());
        frame.pack();
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}