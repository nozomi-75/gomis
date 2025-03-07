package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class AppointmentOverview extends JPanel {
	private final DatePicker datePicker;
	private final JPanel appointmentsPanel;
	private final List<Appointment> appointments;
	private Connection connection;
	private final AppointmentDAO appointmentDAO;

	public AppointmentOverview(AppointmentDAO appointmentDAO, Connection conn) {
		this.appointmentDAO = appointmentDAO;
		this.connection = conn;
		this.appointments = new ArrayList<>();
		setLayout(new MigLayout("wrap 1", "[grow]", "[][grow]"));

		appointmentsPanel = new JPanel(new MigLayout("wrap 1, fill, gap 10", "[grow]"));

		// Setup UI components
		datePicker = createDatePicker();
		datePicker.setPreferredSize(new Dimension(datePicker.getPreferredSize().width, 200)); // Adjust height as needed

		this.add(datePicker, "cell 0 0, grow");
		this.add(createAppointmentsView(), "cell 0 1, grow");

		// Load today's appointments
		loadAppointments(LocalDate.now());
	}

	private DatePicker createDatePicker() {
		DatePicker picker = new DatePicker();
		picker.addDateSelectionListener(dateEvent -> {
			if (picker.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
				LocalDate selectedDate = picker.getSelectedDate();
				if (selectedDate != null) {
					loadAppointments(selectedDate);
				}
			}
		});
		picker.now();
		picker.setAnimationEnabled(true);
		return picker;
	}

	private JScrollPane createAppointmentsView() {
		appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Appointments:"));
		JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Disable horizontal scrolling
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Enable vertical scrolling
		return scrollPane;
	}

	private JPanel createAppointmentCard(Appointment appt) {
		JPanel card = new JPanel(new MigLayout("insets 5, wrap 1, fill", "[grow]", "[][][][][]"));
		card.setBorder(new LineBorder(Color.GRAY, 1, true));
		card.setBackground(new Color(245, 245, 245)); // Light gray background for contrast

		// Title Section
		JPanel titlePanel = new JPanel(new MigLayout("insets 0", "[grow][]", ""));
		titlePanel.setOpaque(false);
		JTextArea nameLabel = new JTextArea(appt.getAppointmentTitle());
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
		nameLabel.setLineWrap(true); // Enable line wrapping
		nameLabel.setWrapStyleWord(true); // Wrap at word boundaries
		nameLabel.setOpaque(false); // Make it transparent
		nameLabel.setEditable(false); // Make it non-editable
		nameLabel.setBorder(null); // Remove border for better appearance
		nameLabel.setPreferredSize(new Dimension(0, 40)); // Allow width to grow
		nameLabel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE)); // Set a maximum width
		titlePanel.add(nameLabel, "cell 0 0, growx");
		JButton viewButton = new JButton("View");
		viewButton.setPreferredSize(new Dimension(60, 25));
		viewButton.setBackground(new Color(33, 150, 243)); // Blue theme
		viewButton.setForeground(Color.WHITE);
		viewButton.setFocusPainted(false);
		viewButton.addActionListener(e -> showAppointmentDetailsPopup(appt));
		titlePanel.add(viewButton, "cell 0 0, align right");
		card.add(titlePanel, "growx");

		// Time Section
		String timeStr = appt.getAppointmentDateTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("h:mm a"));
		JLabel timeLabel = new JLabel("Time: " + timeStr);
		timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, 12f));
		timeLabel.setForeground(Color.DARK_GRAY);
		card.add(timeLabel, "growx");

		// Type Section
		JLabel typeLabel = new JLabel(
				"Type: " + (appt.getAppointmentType() != null ? appt.getAppointmentType() : "N/A"));
		typeLabel.setFont(typeLabel.getFont().deriveFont(Font.ITALIC));
		typeLabel.setForeground(new Color(139, 0, 139)); // Purple for type
		card.add(typeLabel, "growx");

		// Status Section
		JLabel statusLabel = new JLabel(
				"Status: " + (appt.getAppointmentStatus() != null ? appt.getAppointmentStatus() : "N/A"));
		statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
		statusLabel.setForeground(Color.BLACK);
		card.add(statusLabel, "growx");

		// Participants Summary Section
		JPanel participantsPanel = new JPanel(new MigLayout("insets 0", "[grow][]", ""));
		participantsPanel.setOpaque(false);
		int participantCount = (appt.getParticipants() != null) ? appt.getParticipants().size() : 0;
		String participantsSummary = "Participants: " + participantCount + " assigned";
		JLabel participantsLabel = new JLabel(participantsSummary);
		participantsLabel.setFont(participantsLabel.getFont().deriveFont(Font.PLAIN, 12f));
		participantsLabel.setForeground(Color.BLUE);
		participantsLabel.setToolTipText("Click to view participant details in the popup");
		participantsPanel.add(participantsLabel, "cell 0 0, growx");
		JButton deleteButton = new JButton("Delete");
		deleteButton.setPreferredSize(new Dimension(80, 25));
		deleteButton.setBackground(new Color(244, 67, 54)); // Red theme
		deleteButton.setForeground(Color.WHITE);
		deleteButton.setFocusPainted(false);
		deleteButton.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this appointment?",
					"Confirm Delete", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				try {
					if (appointmentDAO.deleteAppointment(appt.getAppointmentId())) {
						loadAppointments(appt.getAppointmentDateTime().toLocalDateTime().toLocalDate());
						JOptionPane.showMessageDialog(this, "Appointment deleted successfully", "Success",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (SQLException ex) {
					JOptionPane.showMessageDialog(this, "Error deleting appointment: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
		participantsPanel.add(deleteButton, "cell 0 0, align right");
		card.add(participantsPanel, "growx");

		return card;
	}

	private void showAppointmentDetailsPopup(Appointment appt) {
		StringBuilder details = new StringBuilder();
		details.append("<html><body style='width: 300px;'>Appointment Details:<br>")
				.append("ID: ").append(appt.getAppointmentId()).append("<br>")
				.append("Counselor ID: ")
				.append(appt.getGuidanceCounselorId() != null ? appt.getGuidanceCounselorId() : "Not assigned")
				.append("<br>")
				.append("Title: ").append(appt.getAppointmentTitle()).append("<br>")
				.append("Type: ").append(appt.getAppointmentType() != null ? appt.getAppointmentType() : "N/A")
				.append("<br>")
				.append("Date/Time: ").append(appt.getAppointmentDateTime().toLocalDateTime()
						.format(DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a")))
				.append("<br>")
				.append("Notes: ").append(appt.getAppointmentNotes() != null ? appt.getAppointmentNotes() : "N/A")
				.append("<br>")
				.append("Status: ").append(appt.getAppointmentStatus() != null ? appt.getAppointmentStatus() : "N/A")
				.append("<br>")
				.append("<b>Participants:</b><br>");

		if (appt.getParticipants() != null && !appt.getParticipants().isEmpty()) {
			for (Participants p : appt.getParticipants()) {
				details.append("  - ").append(p.getParticipantFirstName()).append(" ")
						.append(p.getParticipantLastName());
				if (p.getStudentUid() != null)
					details.append(" (Student UID: ").append(p.getStudentUid()).append(")");
				if (p.getContactNumber() != null)
					details.append(", Contact: ").append(p.getContactNumber());
				if (p.getEmail() != null)
					details.append(", Email: ").append(p.getEmail());
				details.append("<br>");
			}
		} else {
			details.append("  - No participants assigned<br>");
		}
		details.append("</body></html>");

		JOptionPane.showMessageDialog(this, details.toString(), "Appointment Details",
				JOptionPane.INFORMATION_MESSAGE, null);
	}

	private void loadAppointments(LocalDate date) {
		SwingUtilities.invokeLater(() -> {
			appointmentsPanel.removeAll();
			List<Appointment> loadedAppointments = null;
			try {
				loadedAppointments = appointmentDAO.getAppointmentsForDate(date);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, "Error fetching appointments: " + e.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			appointments.clear();
			if (loadedAppointments != null) {
				appointments.addAll(loadedAppointments);
			}

			if (appointments.isEmpty()) {
				JLabel noAppointmentsLabel = new JLabel("No appointments for this date", SwingConstants.CENTER);
				appointmentsPanel.add(noAppointmentsLabel, "grow");
			} else {
				for (Appointment appt : appointments) {
					appointmentsPanel.add(createAppointmentCard(appt), "grow");
				}
			}

			appointmentsPanel.revalidate();
			appointmentsPanel.repaint();
		});
	}
}