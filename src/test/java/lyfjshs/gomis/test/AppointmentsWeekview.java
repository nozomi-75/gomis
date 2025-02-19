package lyfjshs.gomis.test;



import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AppointmentsWeekview {

    private static final Map<String, JPanel> dayPanels = new HashMap<>(); // To track panels for each day

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();

            JFrame frame = new JFrame("Guidance Office Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.getContentPane().setLayout(new BorderLayout());

            // Main content panel
            JPanel mainContent = new JPanel(new MigLayout("wrap 1, fill", "[grow]", "[grow][]"));
//
            // Appointments overview section
            JPanel overviewPanel = new JPanel(new MigLayout("wrap 6, fill", "[grow][grow][grow][grow][grow][grow]", "[grow]"));
            overviewPanel.setBackground(Color.WHITE);
            overviewPanel.setBorder(BorderFactory.createTitledBorder("Appointments Overview"));

            // Get current date and calculate week
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);

            String[] columns = new String[6];
            for (int i = 0; i < 6; i++) {
                LocalDate day = startOfWeek.plusDays(i);
                columns[i] = day.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
            }

            for (String column : columns) {
                // Create a panel for each day
                JPanel dayPanel = new JPanel(new MigLayout("wrap 1, fillx", "[grow]", ""));
                dayPanel.setBackground(new Color(245, 245, 245));
                dayPanel.setBorder(BorderFactory.createTitledBorder(column));

                JScrollPane scrollPane = new JScrollPane(dayPanel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                overviewPanel.add(scrollPane, "grow");

                dayPanels.put(column, dayPanel);
            }

            mainContent.add(overviewPanel, "grow");

            // Control panel for actions
            JPanel controlPanel = new JPanel(new MigLayout("align center"));
            JButton addAppointmentButton = new JButton("Add Appointment");

            addAppointmentButton.addActionListener(e -> {
                JDialog dialog = new JDialog(frame, "Add Appointment", true);
                dialog.setSize(400, 300);
                dialog.getContentPane().setLayout(new MigLayout("wrap 2, fillx", "[grow][grow]", ""));

                JLabel dayLabel = new JLabel("Day:");
                JComboBox<String> dayComboBox = new JComboBox<>(columns);
                JLabel titleLabel = new JLabel("Appointment Title:");
                JTextField titleField = new JTextField();
                JLabel timeLabel = new JLabel("Time:");
                JTextField timeField = new JTextField();
                JLabel detailsLabel = new JLabel("Details:");
                JTextArea detailsArea = new JTextArea(3, 20);

                JButton saveButton = new JButton("Save");
                JButton cancelButton = new JButton("Cancel");

                saveButton.addActionListener(saveEvent -> {
                    String selectedDay = (String) dayComboBox.getSelectedItem();
                    String title = titleField.getText().trim();
                    String time = timeField.getText().trim();
                    String details = detailsArea.getText().trim();

                    if (selectedDay != null && !title.isEmpty() && !time.isEmpty()) {
                        String appointmentDetails = time + " - " + title;

                        JPanel dayPanel = dayPanels.get(selectedDay);
                        if (dayPanel != null) {
                            JButton appointmentButton = new JButton(appointmentDetails);
                            appointmentButton.putClientProperty("Details", details);
                            appointmentButton.setBackground(new Color(220, 245, 220));
                            appointmentButton.setFocusPainted(false);
                            appointmentButton.addActionListener(event -> {
                                JOptionPane.showMessageDialog(frame,
                                        "Time: " + time + "\nTitle: " + title + "\nDetails: " + details,
                                        "Appointment Details",
                                        JOptionPane.INFORMATION_MESSAGE);
                            });

                            dayPanel.add(appointmentButton, "growx");
                            dayPanel.revalidate();
                            dayPanel.repaint();
                            dialog.dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                cancelButton.addActionListener(cancelEvent -> dialog.dispose());

                dialog.getContentPane().add(dayLabel);
                dialog.getContentPane().add(dayComboBox);
                dialog.getContentPane().add(titleLabel);
                dialog.getContentPane().add(titleField);
                dialog.getContentPane().add(timeLabel);
                dialog.getContentPane().add(timeField);
                dialog.getContentPane().add(detailsLabel, "top");
                dialog.getContentPane().add(new JScrollPane(detailsArea), "span, growx");
                dialog.getContentPane().add(saveButton, "span, split 2, align center");
                dialog.getContentPane().add(cancelButton);

                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
            });

            controlPanel.add(addAppointmentButton);
            mainContent.add(controlPanel);

            frame.getContentPane().add(mainContent, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}