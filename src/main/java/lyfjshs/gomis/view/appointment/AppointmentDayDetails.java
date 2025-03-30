package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import net.miginfocom.swing.MigLayout;

public class AppointmentDayDetails extends JPanel {
	private Connection connection;
	private JPanel bodyPanel;
	private Consumer<Participants> onParticipantSelect;
	private Consumer<Appointment> onRedirectToSession;
	private Appointment currentAppointment;
	private Consumer<Appointment> onEditAppointment;
	private AppointmentDAO appointmentDAO;

	public AppointmentDayDetails(Connection connection, Consumer<Participants> onParticipantSelect,
			Consumer<Appointment> onRedirectToSession) {
		this(connection, onParticipantSelect, onRedirectToSession, null);
	}

	public AppointmentDayDetails(Connection connection, Consumer<Participants> onParticipantSelect,
			Consumer<Appointment> onRedirectToSession, Consumer<Appointment> onEditAppointment) {
		this.connection = connection;
		this.onParticipantSelect = onParticipantSelect;
		this.onRedirectToSession = onRedirectToSession;
		this.onEditAppointment = onEditAppointment;
		this.appointmentDAO = new AppointmentDAO(connection);

		setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
		bodyPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
		add(bodyPanel, "grow");
	}

	public void loadAppointmentsForDate(LocalDate date) throws SQLException {
		List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(date);
		bodyPanel.removeAll();
		
		if (appointments != null && !appointments.isEmpty()) {
			// Take the first appointment as current
			this.currentAppointment = appointments.get(0);
			displayAppointmentDetails(currentAppointment);
		} else {
			// Show empty state
			JLabel emptyLabel = new JLabel("No appointments for this date");
			emptyLabel.setHorizontalAlignment(JLabel.CENTER);
			bodyPanel.add(emptyLabel, "center");
		}
		
		bodyPanel.revalidate();
		bodyPanel.repaint();
	}

	public Appointment getCurrentAppointment() {
		return currentAppointment;
	}

	public void loadAppointmentDetails(Appointment appointment) {
		this.currentAppointment = appointment;
		bodyPanel.removeAll();
		displayAppointmentDetails(appointment);
		bodyPanel.revalidate();
		bodyPanel.repaint();
	}

	/**
	 * @wbp.parser.constructor
	 */
	private void displayAppointmentDetails(Appointment appointment) {
		// Main container with smooth scrolling
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
		scrollPane.setBorder(null);
		
		// Content panel
		JPanel contentPanel = new JPanel(new MigLayout("wrap, fillx", "[grow]", "[]20[]20[180px]20[]20[][]20[][]"));
		contentPanel.setBackground(UIManager.getColor("Panel.background"));
		
		// Details grid
		JPanel detailsGrid = new JPanel(new MigLayout("wrap 2, gap 15", "[grow][grow]"));
		detailsGrid.setOpaque(false);

		// Title
		detailsGrid.add(createDetailItem("Appointment Title", appointment.getAppointmentTitle()), "grow");
		
		// Type
		detailsGrid.add(createDetailItem("Consultation Type", appointment.getConsultationType()), "grow");
		
		// Date & Time
		String dateTimeStr = appointment.getAppointmentDateTime().toLocalDateTime()
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
		detailsGrid.add(createDetailItem("Date & Time", dateTimeStr), "grow");
		
		// Status with badge
		JPanel statusPanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		statusPanel.setOpaque(false);
		JLabel statusLabel = new JLabel("Status");
		statusLabel.setForeground(new Color(44, 62, 80));
		statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
		statusPanel.add(statusLabel, "growx");
		
		JLabel statusBadge = new JLabel(appointment.getAppointmentStatus());
		statusBadge.setOpaque(true);
		Color statusColor = getStatusColor(appointment.getAppointmentStatus());
		statusBadge.setBackground(statusColor);
		statusBadge.setForeground(Color.WHITE);
		statusBadge.setHorizontalAlignment(JLabel.CENTER);
		statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		statusPanel.add(statusBadge, "growx");
		detailsGrid.add(statusPanel, "grow");

		contentPanel.add(detailsGrid, "growx");

		// Participants section
		JPanel participantsHeader = createSectionHeader("Participants", null);
		contentPanel.add(participantsHeader, "growx");

		// Participants table
		String[] columnNames = { "Name", "Participant Type", "Contact Number", "Gender" };
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		// Add participants to table
		if (appointment.getParticipants() != null) {
			for (Participants participant : appointment.getParticipants()) {
				model.addRow(new Object[] {
					participant.getParticipantFirstName() + " " + participant.getParticipantLastName(),
					participant.getParticipantType(),
					participant.getContactNumber(),
					participant.getSex()
				});
			}
		}

		JTable participantsTable = new JTable(model);
		participantsTable.setRowHeight(40);
		participantsTable.setShowGrid(true);
		participantsTable.setGridColor(new Color(221, 221, 221));
		styleTable(participantsTable);

		JScrollPane tableScrollPane = new JScrollPane(participantsTable);
		contentPanel.add(tableScrollPane, "growx");

		
		JPanel notesSectionPanel = new JPanel(new MigLayout("wrap, insets 15", "[grow]", "[]10[]"));
		notesSectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		notesSectionPanel.setBackground(UIManager.getColor("Panel.background"));
		notesSectionPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

		JLabel notesLabel = new JLabel("Appointment Notes");
		notesLabel.setForeground(UIManager.getColor("Label.foreground"));
		notesLabel.setFont(new Font("Arial", Font.BOLD, 12));

		JTextArea notesArea = new JTextArea(appointment.getAppointmentNotes() != null ? 
			appointment.getAppointmentNotes() : "No notes available");
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.setEditable(false);
		notesArea.setOpaque(false);
		notesArea.setBorder(null);
		notesArea.setFont(new Font("Arial", Font.PLAIN, 12));
		notesArea.setForeground(UIManager.getColor("TextArea.foreground"));
		notesArea.setBackground(UIManager.getColor("TextArea.background"));

		notesSectionPanel.add(notesLabel, "growx");
		notesSectionPanel.add(notesArea, "growx");

		contentPanel.add(notesSectionPanel, "growx");
		
		// Counselor section
		JPanel counselorHeader = createSectionHeader("Guidance Counselor Information", null);
		contentPanel.add(counselorHeader, "growx");

		// Counselor details grid
		JPanel counselorGrid = new JPanel(new MigLayout("wrap 2, gap 15", "[grow][grow]"));
		counselorGrid.setOpaque(false);

		// Get counselor details
		try {
			GuidanceCounselor counselor = Main.formManager.getCounselorObject();
			if (counselor != null) {
				counselorGrid.add(createDetailItem("Name", 
					String.format("%s %s %s", counselor.getFirstName(), counselor.getMiddleName(), counselor.getLastName())), "grow");
				counselorGrid.add(createDetailItem("Specialization", counselor.getSpecialization()), "grow");
				counselorGrid.add(createDetailItem("Contact Number", counselor.getContactNum()), "grow");
				counselorGrid.add(createDetailItem("Email", counselor.getEmail()), "grow");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		contentPanel.add(counselorGrid, "growx");


		

		// Add content panel to scroll pane
		scrollPane.setViewportView(contentPanel);
		bodyPanel.add(scrollPane, "grow");
	}

	private JPanel createSectionHeader(String title, String badge) {
		JPanel headerPanel = new JPanel(new MigLayout("insets 12 15, fillx"));
		headerPanel.putClientProperty("FlatLaf.style", "arc: 8");
		headerPanel.setBackground(UIManager.getColor("Button.default.startBackground"));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setForeground(UIManager.getColor("Label.foreground"));
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

		headerPanel.add(titleLabel, "grow, pushx");

		if (badge != null) {
			JLabel badgeLabel = new JLabel(badge);
			badgeLabel.setForeground(UIManager.getColor("Label.foreground"));
			Color bgColor = UIManager.getColor("Button.default.startBackground");
			if (bgColor != null) {
				badgeLabel.setBackground(bgColor.darker());
			} else {
				badgeLabel.setBackground(new Color(60, 60, 60));
			}
			badgeLabel.setOpaque(true);
			badgeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			headerPanel.add(badgeLabel);
		}

		return headerPanel;
	}

	private JPanel createDetailItem(String label, String value) {
		JPanel item = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		item.setOpaque(false);

		JLabel labelLbl = new JLabel(label);
		labelLbl.setFont(new Font("Arial", Font.BOLD, 12));
		labelLbl.setForeground(UIManager.getColor("Label.foreground"));
		item.add(labelLbl, "growx");

		JLabel valueLbl = new JLabel(value != null ? value : "N/A");
		valueLbl.setFont(new Font("Arial", Font.PLAIN, 14));
		valueLbl.setOpaque(true);
		valueLbl.setBackground(UIManager.getColor("TextField.background"));
		valueLbl.setForeground(UIManager.getColor("TextField.foreground"));
		valueLbl.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
			BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		item.add(valueLbl, "growx");

		return item;
	}

	private void styleTable(JTable table) {
		// Custom header renderer
		table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
				label.setBackground(new Color(241, 243, 245));
				label.setForeground(new Color(44, 62, 80));
				label.setFont(new Font("Arial", Font.BOLD, 12));
				label.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 221, 221)),
					BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				return label;
			}
		});

		// Custom cell renderer
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
				label.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 221, 221)),
					BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				return label;
			}
		});
	}

	private Color getStatusColor(String status) {
		return switch (status.toLowerCase()) {
			case "on-going" -> new Color(243, 156, 18); // Orange
			case "ended" -> new Color(231, 76, 60);     // Red
			case "rescheduled" -> new Color(52, 152, 219); // Blue
			case "cancelled" -> new Color(231, 76, 60);  // Red
			default -> new Color(149, 165, 166);        // Gray
		};
	}
}