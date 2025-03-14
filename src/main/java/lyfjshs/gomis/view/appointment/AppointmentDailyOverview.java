package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class AppointmentDailyOverview extends JPanel {
    private LocalDate selectedDate;
    private JPanel appointmentsPanel;
    private JDateChooser dateChooser;
    private final AppointmentDAO appointmentDAO;
    private final Connection connection;

    public AppointmentDailyOverview(AppointmentDAO appointmentDAO, Connection connection) {
        this.appointmentDAO = appointmentDAO;
        this.connection = connection;
        setLayout(new BorderLayout());
        selectedDate = LocalDate.now();
        initUI();
        updateAppointmentsDisplay();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap, fill", "[grow]", "[][][grow]"));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JLabel titleLabel = new JLabel("Daily Appointments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel subtitleLabel = new JLabel("View and manage your schedule");
        subtitleLabel.setForeground(Color.GRAY);

        JPanel headerPanel = new JPanel(new MigLayout("wrap 2", "[][right]", ""));
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(java.sql.Date.valueOf(selectedDate));
        dateChooser.addPropertyChangeListener("date", evt -> {
            if (evt.getNewValue() != null) {
                selectedDate = ((java.util.Date) evt.getNewValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                updateAppointmentsDisplay();
            }
        });

        JButton addButton = new JButton("Add Appointment");
        addButton.setPreferredSize(new Dimension(120, 25));
        addButton.addActionListener(e -> showAddAppointmentDialog());

        headerPanel.add(titleLabel, "span 2");
        headerPanel.add(subtitleLabel, "span 2");
        headerPanel.add(dateChooser);
        headerPanel.add(addButton, "gapy 5");

        // Appointments display
        appointmentsPanel = new JPanel(new MigLayout("wrap 1", "[grow]", ""));
        appointmentsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        mainPanel.add(headerPanel, "growx");
        mainPanel.add(new JSeparator(), "growx, gapy 5");
        mainPanel.add(appointmentsPanel, "grow");

        add(mainPanel);
    }

    public void updateAppointmentsDisplay() {
        appointmentsPanel.removeAll();
        
        List<Appointment> appointments = null;
        try {
            appointments = appointmentDAO.getAppointmentsForDate(selectedDate);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching appointments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (appointments == null || appointments.isEmpty()) {
            JPanel emptyPanel = new JPanel(new MigLayout("wrap, align center", "[grow]", "[]"));
            emptyPanel.add(new JLabel("No appointments scheduled for this day"));
            JButton addButton = new JButton("Add Your First Appointment");
            addButton.setPreferredSize(new Dimension(150, 25));
            addButton.addActionListener(e -> showAddAppointmentDialog());
            emptyPanel.add(addButton, "gapy 5");
            appointmentsPanel.add(emptyPanel, "growx");
        } else {
            for (Appointment app : appointments) {
                appointmentsPanel.add(createAppointmentCard(app), "growx");
            }
        }
        
        appointmentsPanel.revalidate();
        appointmentsPanel.repaint();
    }

    private JPanel createAppointmentCard(Appointment app) {
        JPanel card = new JPanel(new MigLayout("wrap 2", "[grow][]", ""));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, getTypeColor(app.getConsultationType())),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        card.setBackground(getTypeBackground(app.getConsultationType()));

        JLabel title = new JLabel(app.getAppointmentTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));

        card.add(title, "span 2");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        LocalDateTime appointmentDateTime = app.getAppointmentDateTime().toLocalDateTime();
        String timeSlot = appointmentDateTime.format(timeFormatter);
        card.add(new JLabel(timeSlot + " • " + app.getAppointmentStatus()) {
            { setFont(new Font("Segoe UI", Font.PLAIN, 12)); }
        }, "span 2");

        if (app.getGuidanceCounselorId() != null) {
            card.add(new JLabel("Counselor ID: " + app.getGuidanceCounselorId()) {
                { setFont(new Font("Segoe UI", Font.PLAIN, 12)); }
            }, "span 2");
        }
        
        if (app.getAppointmentNotes() != null && !app.getAppointmentNotes().isEmpty()) {
            String truncatedNotes = app.getAppointmentNotes().length() > 100 ? 
                app.getAppointmentNotes().substring(0, 100) + "..." : 
                app.getAppointmentNotes();
            JLabel notesLabel = new JLabel("<html>Notes: " + truncatedNotes + "</html>") {
                { 
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    setPreferredSize(new Dimension(300, 50));
                    setVerticalAlignment(SwingConstants.TOP);
                }
            };
            card.add(notesLabel, "span 2");
        }

        // Display participants if available
        if (app.getParticipants() != null && !app.getParticipants().isEmpty()) {
            StringBuilder participantsText = new StringBuilder("Participants: ");
            for (int i = 0; i < app.getParticipants().size(); i++) {
                Participants p = app.getParticipants().get(i);
                participantsText.append(p.getParticipantFirstName()).append(" ").append(p.getParticipantLastName());
                if (i < app.getParticipants().size() - 1) {
                    participantsText.append(", ");
                }
            }
            card.add(new JLabel(participantsText.toString()) {
                { setFont(new Font("Segoe UI", Font.PLAIN, 12)); }
            }, "span 2");
        }

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showAppointmentDetailsPopup(app);
            }
        });

        return card;
    }

    private void showAppointmentDetailsPopup(Appointment appointment) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Appointment Details", true);
        AppointmentDayDetails appointmentDetails = new AppointmentDayDetails(connection);
        try {
            appointmentDetails.loadAppointmentsForDate(appointment.getAppointmentDateTime().toLocalDateTime().toLocalDate());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dialog.getContentPane().add(appointmentDetails);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAddAppointmentDialog() {

      
        AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(new Appointment(), appointmentDAO, connection);

			ModalDialog.showModal(this,
					new SimpleModalBorder(addAppointmentPanel, "Add Appointment", new SimpleModalBorder.Option[] {
						new SimpleModalBorder.Option("Add Appointment", SimpleModalBorder.YES_OPTION),
						new SimpleModalBorder.Option("Cancel", SimpleModalBorder.NO_OPTION)
					}, (controller, action) -> {
						if (action == SimpleModalBorder.YES_OPTION) {
							controller.consume();
                            if (addAppointmentPanel.isConfirmed()) {
                                Appointment newAppointment = addAppointmentPanel.getAppointment();
                                try {
                                    List<Integer> participantIds = addAppointmentPanel.getParticipantIds();
                                    int generatedId = appointmentDAO.insertAppointment(
                                        newAppointment.getGuidanceCounselorId(),
                                        newAppointment.getAppointmentTitle(),
                                        newAppointment.getConsultationType(),
                                        newAppointment.getAppointmentDateTime(),
                                        newAppointment.getAppointmentNotes(),
                                        newAppointment.getAppointmentStatus(),
                                        participantIds
                                    );
                                    if (generatedId > 0) {
                                        updateAppointmentsDisplay();
                                    } else {
                                        JOptionPane.showMessageDialog(this, "Failed to add appointment",
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } catch (SQLException e) {
                                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                    e.printStackTrace();
                                }
                            }
                        } else if (action == SimpleModalBorder.NO_OPTION) {
                            //a condition to ask if the user wants to cancel the operation
                            // if the participants are already added
                            controller.close();
							
                        } else if (action == SimpleModalBorder.CLOSE_OPTION
								|| action == SimpleModalBorder.CANCEL_OPTION) {
							controller.close();
							// actions todo next after Close or Cancel
						}
					}),
					"input");
				// set size of modal dialog to 800x800
				ModalDialog.getDefaultOption().getLayoutOption().setSize(800, 800);

    }

    private Color getTypeColor(String type) {
        switch (type) {
            case "Academic Consultation": return new Color(59, 130, 246);
            case "Career Guidance": return new Color(147, 51, 234);
            case "Personal Counseling": return new Color(16, 185, 129);
            case "Behavioral Counseling": return new Color(202, 138, 4);
            case "Group Counseling": return new Color(239, 68, 68);
            default: return Color.GRAY;
        }
    }

    private Color getTypeBackground(String type) {
        switch (type) {
            case "Academic Consultation": return new Color(219, 234, 254);
            case "Career Guidance": return new Color(233, 213, 255);
            case "Personal Counseling": return new Color(209, 250, 229);
            case "Behavioral Counseling": return new Color(254, 243, 199);
            case "Group Counseling": return new Color(254, 226, 226);
            default: return new Color(243, 244, 246);
        }
    }
}