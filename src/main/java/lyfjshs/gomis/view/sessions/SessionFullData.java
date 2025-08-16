package lyfjshs.gomis.view.sessions;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.utils.ErrorDialogUtils;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.view.incident.INCIDENT_fill_up.IncidentFillUpFormPanel;
import lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

/**
 * A form that displays the complete details of a counseling session. It
 * provides a comprehensive view of session information, including participants,
 * notes, and allows for actions like editing, ending the session, or creating
 * an incident report.
 *
 * @author Your Name
 */
public class SessionFullData extends Form {

	private static final Logger LOGGER = Logger.getLogger(SessionFullData.class.getName());
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	// UI Constants from SessionTEST
	private static final Color PRIMARY_COLOR = new Color(79, 70, 229);
	private static final Color SECONDARY_COLOR = new Color(100, 116, 139);
	private static final Color BACKGROUND_COLOR = new Color(241, 245, 249);
	private static final Color CARD_BACKGROUND_COLOR = Color.WHITE;
	private static final Color TEXT_PRIMARY_COLOR = new Color(30, 41, 59);
	private static final Color TEXT_SECONDARY_COLOR = new Color(71, 85, 105);
	private static final Color BORDER_COLOR = new Color(226, 232, 240);
	private static final Color STATUS_ENDED_BG = new Color(220, 252, 231);
	private static final Color STATUS_ENDED_TEXT = new Color(22, 163, 74);
	private static final Color STATUS_ACTIVE_BG = new Color(219, 234, 254);
	private static final Color STATUS_ACTIVE_TEXT = new Color(37, 99, 235);

	private final Connection conn;
	private final Sessions sessionData;
	private GTable participantsTable;

	/**
	 * Constructs the SessionFullData form.
	 *
	 * @param sessionData  The session data to display.
	 * @param counselor    The guidance counselor associated with the session.
	 * @param participants The list of participants in the session.
	 * @param conn         The database connection.
	 */
	public SessionFullData(Sessions sessionData, GuidanceCounselor counselor, List<Participants> participants,
			Connection conn) {
		this.sessionData = sessionData;
		this.conn = conn;

		initializeComponents();
		populateInitialData(counselor, participants);
	}

	/**
	 * Initializes the main panel and its layout.
	 */
	private void initializeComponents() {
		setLayout(new MigLayout("fill", "[grow]", "[][][grow]"));
		setOpaque(false);
	}

	/**
	 * Populates the form with the initial session data.
	 *
	 * @param counselor    The guidance counselor.
	 * @param participants The list of participants.
	 */
	private void populateInitialData(GuidanceCounselor counselor, List<Participants> participants) {
		removeAll();

		// Header Panel
		JPanel headerPanel = createHeaderPanel();
		add(headerPanel, "growx, wrap");

		// Overview Panel
		JPanel overviewPanel = createOverviewPanel(sessionData, counselor);
		add(overviewPanel, "growx, wrap");

		// Tabbed Pane
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD));
		tabbedPane.setOpaque(false);

		// Session Details Tab
		JPanel detailsPanel = createSessionDetailsPanel();
		JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
		detailsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		detailsScrollPane.setOpaque(false);
		detailsScrollPane.getViewport().setOpaque(false);
		tabbedPane.addTab("Session Details", new FlatSVGIcon("icons/info.svg", 0.5f), detailsScrollPane);

		// Participants Tab
		JPanel participantsPanel = createParticipantsPanel(participants);
		JScrollPane participantsScrollPane = new JScrollPane(participantsPanel);
		participantsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		participantsScrollPane.setOpaque(false);
		participantsScrollPane.getViewport().setOpaque(false);
		tabbedPane.addTab("Participants (" + participants.size() + ")", new FlatSVGIcon("icons/users.svg", 0.5f), participantsScrollPane);

		// Violation Info Tab
		JPanel violationPanel = createViolationPanel();
		JScrollPane violationScrollPane = new JScrollPane(violationPanel);
		violationScrollPane.setBorder(BorderFactory.createEmptyBorder());
		violationScrollPane.setOpaque(false);
		violationScrollPane.getViewport().setOpaque(false);
		tabbedPane.addTab("Violation Info", new FlatSVGIcon("icons/warning.svg", 0.5f), violationScrollPane);

		add(tabbedPane, "grow");
		revalidate();
		repaint();
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 0", "[left, grow]0[right]", ""));
		headerPanel.setOpaque(false);

		// Left side
		JPanel leftPanel = new JPanel(new MigLayout("insets 0, gap 15", "", "[center]"));
		leftPanel.setOpaque(false);

		JLabel titleLabel = new JLabel("Session Details");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 28f));
		leftPanel.add(titleLabel);

		// Right side (Action Buttons)
		JPanel rightPanel = new JPanel(new MigLayout("insets 0, gap 7", "", "[center]"));
		rightPanel.setOpaque(false);

		if ("Active".equalsIgnoreCase(sessionData.getSessionStatus())) {
			JButton editButton = createStyledButton("Edit Session", CARD_BACKGROUND_COLOR, UIManager.getColor("Button.foreground"));
			editButton.addActionListener(e -> editSession());
			rightPanel.add(editButton);

			JButton endButton = createStyledButton("End Session", STATUS_ENDED_BG, STATUS_ENDED_TEXT);
			endButton.addActionListener(e -> endSession());
			rightPanel.add(endButton);
		}

		JButton createReportButton = createStyledButton("Create Incident Report", PRIMARY_COLOR, UIManager.getColor("Button.default.foreground"));
		createReportButton.addActionListener(e -> createIncidentReport());
		rightPanel.add(createReportButton);

		headerPanel.add(leftPanel, "growx");
		headerPanel.add(rightPanel);
		return headerPanel;
	}

	private JPanel createOverviewPanel(Sessions session, GuidanceCounselor counselor) {
		JPanel overviewPanel = new JPanel();
		overviewPanel.setBackground(CARD_BACKGROUND_COLOR);
		overviewPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
			BorderFactory.createEmptyBorder(20, 20, 20, 20)
		));
		overviewPanel.setOpaque(false);
		overviewPanel.setLayout(new MigLayout("fillx, wrap 5, gap 15 25", "[grow,fill]"));

		// Add overview items
		overviewPanel.add(createOverviewItem("Session ID", String.valueOf(session.getSessionId())));
		overviewPanel.add(createOverviewItem("Session Title", session.getConsultationType()));
		overviewPanel.add(createOverviewItem("Session Date & Time", 
			session.getSessionDateTime() != null ? 
			DATE_TIME_FORMATTER.format(session.getSessionDateTime().toLocalDateTime()) : "N/A"));
		overviewPanel.add(createOverviewItem("Recorded By", 
			counselor != null ? counselor.getFirstName() + " " + counselor.getLastName() : "N/A"));
		overviewPanel.add(createStatusItem("Session Status", session.getSessionStatus(), 
			"Active".equals(session.getSessionStatus()) ? STATUS_ACTIVE_BG : STATUS_ENDED_BG,
			"Active".equals(session.getSessionStatus()) ? STATUS_ACTIVE_TEXT : STATUS_ENDED_TEXT));

		return overviewPanel;
	}

	private JPanel createSessionDetailsPanel() {
		JPanel detailsPanel = new JPanel(new MigLayout("wrap 1, fillx, insets 0", "[grow,center]", "[][][][]"));
		detailsPanel.setOpaque(false);

		// Session Summary Card
		JPanel summaryCard = createCardPanel();
		summaryCard.setLayout(new MigLayout("wrap 1, fillx", "[grow,fill]", "[][95.00,grow]"));
		summaryCard.setOpaque(false);
		
		JLabel summaryTitle = new JLabel("Session Summary");
		summaryTitle.setFont(summaryTitle.getFont().deriveFont(Font.BOLD, 18f));
		summaryCard.add(summaryTitle, "wrap 5");

		JTextArea summaryText = new JTextArea(sessionData.getSessionSummary());
		summaryText.setEditable(false);
		summaryText.setLineWrap(true);
		summaryText.setWrapStyleWord(true);
		summaryText.setOpaque(false);
		summaryText.setFont(UIManager.getFont("Label.font"));
		summaryCard.add(summaryText, "grow");

		detailsPanel.add(summaryCard, "cell 0 0,growx");

		// Session Notes Card
		JPanel notesCard = createCardPanel();
		notesCard.setLayout(new MigLayout("wrap 1, fillx", "[grow,fill]", "[][95]"));
		notesCard.setOpaque(false);
		
		JLabel notesTitle = new JLabel("Session Notes");
		notesTitle.setFont(notesTitle.getFont().deriveFont(Font.BOLD, 18f));
		notesCard.add(notesTitle, "wrap 5");

		JTextArea notesText = new JTextArea(sessionData.getSessionNotes());
		notesText.setEditable(false);
		notesText.setLineWrap(true);
		notesText.setWrapStyleWord(true);
		notesText.setOpaque(false);
		notesText.setFont(UIManager.getFont("Label.font"));
		notesCard.add(notesText, "grow");

		detailsPanel.add(notesCard, "cell 0 1,growx");

		// Additional Info Card
		JPanel additionalInfoCard = createCardPanel();
		additionalInfoCard.setLayout(new MigLayout("wrap 1, fillx", "[grow,fill]"));
		additionalInfoCard.setOpaque(false);

		JLabel additionalInfoTitle = new JLabel("Additional Information");
		additionalInfoTitle.setFont(additionalInfoTitle.getFont().deriveFont(Font.BOLD, 18f));
		additionalInfoCard.add(additionalInfoTitle, "wrap 15");

		JPanel additionalInfoGrid = new JPanel(new MigLayout("wrap 3, fillx, gap 20 30", "[grow,fill]"));
		additionalInfoGrid.setOpaque(false);

		additionalInfoGrid.add(createOverviewItem("Appointment Type", sessionData.getAppointmentType()));
		additionalInfoGrid.add(createOverviewItem("Consultation Type", sessionData.getConsultationType()));
		additionalInfoGrid.add(createOverviewItem("Last Updated", 
			sessionData.getUpdatedAt() != null ? 
			DATE_TIME_FORMATTER.format(sessionData.getUpdatedAt().toLocalDateTime()) : "N/A"));

		additionalInfoCard.add(additionalInfoGrid, "growx");
		detailsPanel.add(additionalInfoCard, "cell 0 2,growx");

		return detailsPanel;
	}

	private JPanel createParticipantsPanel(List<Participants> participants) {
		JPanel participantsPanel = new JPanel(new MigLayout("insets 10", "[grow]", "[][grow]"));
		participantsPanel.setBackground(CARD_BACKGROUND_COLOR);
		participantsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
				BorderFactory.createEmptyBorder(15, 15, 15, 15)));

		JLabel title = new JLabel("Participant List");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
		participantsPanel.add(title, "wrap");

		// Table setup
		String[] columnNames = { "#", "Participant Name", "Participant Type", "Violator", "Reporter", "Contact", "Actions" };
		Class<?>[] columnTypes = { Integer.class, String.class, String.class, Boolean.class, Boolean.class, String.class, Object.class };
		boolean[] editableColumns = { false, false, false, false, false, false, true }; // All false for display
		double[] columnWidths = { 0.05, 0.25, 0.15, 0.1, 0.1, 0.2, 0.15 }; // Adjusted widths
		int[] alignments = {
				SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER
		};

		DefaultTableActionManager actionManager = new DefaultTableActionManager();
		((DefaultTableActionManager) actionManager).addAction("View Details", (table, row) -> {
			Participants selectedParticipant = participants.get(row);
			showParticipantDetailsDialog(selectedParticipant.getParticipantFirstName() + " " + selectedParticipant.getParticipantLastName(), selectedParticipant.getParticipantType());
		}, UIManager.getColor("Component.accentColor"), new FlatSVGIcon("icons/view.svg", 0.5f));

		participantsTable = new GTable(new Object[0][columnNames.length], columnNames, columnTypes, editableColumns, columnWidths, alignments, false, actionManager);
		participantsTable.setRowHeight(40);
		actionManager.setupTableColumn(participantsTable, columnNames.length - 1); // Actions column is the last one

		JScrollPane scrollPane = new JScrollPane(participantsTable);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		participantsPanel.add(scrollPane, "grow");

		// Populate table
		DefaultTableModel model = (DefaultTableModel) participantsTable.getModel();
		model.setRowCount(0); // Clear existing data

		// Fetch violation information if exists
		Violation sessionViolation = null;
		if (sessionData.getViolationId() != null) {
			try {
				ViolationDAO violationDAO = new ViolationDAO(conn);
				sessionViolation = violationDAO.getViolationById(sessionData.getViolationId());
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "Error fetching violation details for session " + sessionData.getSessionId(), e);
				// Continue without violation data if error occurs
			}
		}

		for (int i = 0; i < participants.size(); i++) {
			Participants participant = participants.get(i);
			boolean isViolator = false;
			// Determine if this participant is the violator for this session
			if (sessionViolation != null &&					participant.getParticipantId() == sessionViolation.getParticipantId()) {
				isViolator = true;
			}
			// Reporter status is now persisted, so retrieve it from the participant object.
			boolean isReporter = participant.isReporter();

			model.addRow(new Object[]{
					i + 1,
					participant.getParticipantFirstName() + " " + participant.getParticipantLastName(),
					participant.getParticipantType(),
					isViolator,
					isReporter,
					participant.getContactNumber(),
					"actions" // Actions column
			});
		}
		return participantsPanel;
	}

	private JPanel createViolationPanel() {
		JPanel violationPanel = new JPanel(new MigLayout("fill, insets 0", "[grow,fill]", "[top]"));
		violationPanel.setOpaque(false);

		JPanel violationCard = createCardPanel();
		violationCard.setLayout(new MigLayout("wrap 3, fillx, gap 20 30", "[grow,fill]"));
		violationCard.setOpaque(false);

		JLabel violationTitle = new JLabel("Violation & Intervention Details");
		violationTitle.setFont(violationTitle.getFont().deriveFont(Font.BOLD, 18f));
		violationCard.add(violationTitle, "span 3, wrap 20");

		try {
			ViolationDAO violationDAO = new ViolationDAO(conn);
			List<Violation> violations = violationDAO.getViolationsBySessionId(sessionData.getSessionId());
			
			if (violations.isEmpty()) {
				JLabel noViolationsLabel = new JLabel("No violations recorded for this session.");
				violationCard.add(noViolationsLabel, "span 3, center");
			} else {
				for (Violation violation : violations) {
					violationCard.add(createOverviewItem("Violation Type", violation.getViolationType()));
					violationCard.add(createOverviewItem("Category", violation.getCategory() != null ? violation.getCategory().getCategoryName() : "N/A"));
					violationCard.add(createStatusItem("Status", violation.getStatus(),
						"Active".equals(violation.getStatus()) ? STATUS_ACTIVE_BG : STATUS_ENDED_BG,
						"Active".equals(violation.getStatus()) ? STATUS_ACTIVE_TEXT : STATUS_ENDED_TEXT));

					// Violation Description
					JPanel descriptionPanel = new JPanel(new MigLayout("wrap 1, fillx, insets 0, gapy 5"));
					descriptionPanel.setOpaque(false);
					JLabel descriptionTitle = new JLabel("Violation Description");
					descriptionTitle.setFont(descriptionTitle.getFont().deriveFont(Font.BOLD));
					descriptionPanel.add(descriptionTitle);
					JTextArea description = new JTextArea(violation.getViolationDescription());
					description.setEditable(false);
					description.setLineWrap(true);
					description.setWrapStyleWord(true);
					description.setOpaque(false);
					description.setFont(UIManager.getFont("Label.font"));
					descriptionPanel.add(description, "growx");
					violationCard.add(descriptionPanel, "span 3, growx, wrap");

					// Reinforcement Panel
					JPanel reinforcementPanel = new JPanel(new MigLayout("wrap 1, fillx, insets 0, gapy 5"));
					reinforcementPanel.setOpaque(false);
					JLabel reinforcementTitle = new JLabel("Reinforcement / Intervention");
					reinforcementTitle.setFont(reinforcementTitle.getFont().deriveFont(Font.BOLD));
					reinforcementPanel.add(reinforcementTitle);

					JTextArea reinforcementText = new JTextArea(violation.getReinforcement());
					reinforcementText.setEditable(false);
					reinforcementText.setLineWrap(true);
					reinforcementText.setWrapStyleWord(true);
					reinforcementText.setOpaque(false);
					reinforcementText.setFont(UIManager.getFont("Label.font"));
					reinforcementPanel.add(reinforcementText, "growx");
					violationCard.add(reinforcementPanel, "span 3, growx");
				}
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Error fetching violations", e);
			JLabel errorLabel = new JLabel("Error loading violation details.");
			violationCard.add(errorLabel, "span 3, center");
		}

		violationPanel.add(violationCard, "growx");
		return violationPanel;
	}

	// Helper methods for creating UI components
	private JPanel createCardPanel() {
		JPanel card = new JPanel();
		card.setBackground(CARD_BACKGROUND_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
			BorderFactory.createEmptyBorder(20, 20, 20, 20)
		));
		card.setOpaque(false);
		return card;
	}

	private JPanel createOverviewItem(String labelText, String valueText) {
		JPanel panel = new JPanel(new MigLayout("wrap 1, insets 0, gapy 0", "", ""));
		panel.setOpaque(false);

		JLabel label = new JLabel(labelText);
		label.setFont(label.getFont().deriveFont(13f));
		panel.add(label);

		JLabel value = new JLabel(valueText);
		value.setFont(value.getFont().deriveFont(Font.BOLD, 15f));
		panel.add(value);

	        return panel;
	    }

	private JPanel createStatusItem(String labelText, String statusText, Color bg, Color fg) {
		JPanel panel = createOverviewItem(labelText, "");
		panel.remove(1);
		JLabel statusLabel = new StatusBadge(statusText, bg, fg);
		panel.add(statusLabel);
		return panel;
	}

	private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
		JButton button = new JButton(text);
		button.setFont(button.getFont().deriveFont(Font.BOLD));
		button.setForeground(fgColor);
		button.setBackground(bgColor);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER_COLOR, 1),
			new EmptyBorder(9, 19, 9, 19)
		));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return button;
	}

	// Status Badge inner class
	class StatusBadge extends JLabel {
		private Color badgeBackgroundColor;

		public StatusBadge(String text, Color bg, Color fg) {
			super(text);
			this.badgeBackgroundColor = bg;
			setForeground(fg);
			setFont(getFont().deriveFont(Font.BOLD, 12f));
			setOpaque(false);
			setBorder(new EmptyBorder(5, 12, 5, 12));
		}

		@Override
		protected void paintComponent(java.awt.Graphics g) {
			java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
			g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(badgeBackgroundColor);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
			super.paintComponent(g2);
			g2.dispose();
		}
	}

	// =================================================================================
	// Event Handlers & Actions
	// =================================================================================

	/**
	 * Handles the creation of an incident report.
	 */
	private void createIncidentReport() {
		try {
			DefaultTableModel model = (DefaultTableModel) participantsTable.getModel();
			List<Participants> participants = new ArrayList<>();
			for (int i = 0; i < model.getRowCount(); i++) {
				String fullName = (String) model.getValueAt(i, 1);
				participants.add(findParticipantByName(fullName));
			}

			IncidentFillUpFormPanel incidentForm = new IncidentFillUpFormPanel(conn);
			incidentForm.populateFromSession(sessionData, participants);
			FormManager.showForm(incidentForm);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error creating incident report", e);
			ErrorDialogUtils.showError(this, "Error creating incident report: " + e.getMessage());
		}
	}

	/**
	 * Handles editing the current session.
	 */
	private void editSession() {
		try {
			SessionsFillUpFormPanel SessionsFillUpFormPanel = new SessionsFillUpFormPanel(conn);
			SessionsFillUpFormPanel.setEditingSession(sessionData);
			SessionsFillUpFormPanel.setSaveCallback(this::refreshView);
			FormManager.showForm(SessionsFillUpFormPanel);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error opening session edit form", e);
			ErrorDialogUtils.showError(this, "Error opening edit form: " + e.getMessage());
		}
	}

	/**
	 * Handles ending the current session.
	 */
	private void endSession() {
		int choice = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to end this session? This action cannot be undone.", "Confirm End Session",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (choice == JOptionPane.YES_OPTION) {
			performEndSession();
		}
	}

	/**
	 * Performs the database operations to end a session.
	 */
	private void performEndSession() {
		try {
			conn.setAutoCommit(false);
			SessionsDAO sessionsDAO = new SessionsDAO(conn);

			// Update Session
			sessionData.setSessionStatus("Ended");
			sessionsDAO.updateSession(sessionData);

			// Update associated Appointment if it exists
			if (sessionData.getAppointmentId() != null) {
				AppointmentDAO appointmentDAO = new AppointmentDAO(conn);
				Appointment appointment = appointmentDAO.getAppointmentById(sessionData.getAppointmentId());
				if (appointment != null) {
					appointment.setAppointmentStatus("Completed");
					appointmentDAO.updateAppointment(appointment);
					EventBus.publish("appointment_status_changed", appointment.getAppointmentId());
				}
			}
			conn.commit();
			ErrorDialogUtils.showInfo(this, "Session has been ended successfully.");
			refreshView();

		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				LOGGER.log(Level.SEVERE, "Failed to rollback transaction", ex);
			}
			LOGGER.log(Level.SEVERE, "Error ending session", e);
			ErrorDialogUtils.showError(this, "Error ending session: " + e.getMessage());
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "Failed to reset auto-commit", e);
			}
		}
	}

	// =================================================================================
	// Helper & Utility Methods
	// =================================================================================

	/**
	 * Refreshes the entire view with updated session data from the database.
	 */
	private void refreshView() {
		try {
			SessionsDAO sessionsDAO = new SessionsDAO(conn);
			Sessions updatedSession = sessionsDAO.getSessionById(sessionData.getSessionId());
			if (updatedSession != null) {
				GuidanceCounselor counselor = sessionsDAO.getCounselorById(updatedSession.getGuidanceCounselorId());
				List<Participants> participants = sessionsDAO.getParticipantsBySessionId(updatedSession.getSessionId());

				sessionData.setSessionStatus(updatedSession.getSessionStatus());
				sessionData.setSessionSummary(updatedSession.getSessionSummary());
				sessionData.setSessionNotes(updatedSession.getSessionNotes());
				sessionData.setAppointmentType(updatedSession.getAppointmentType());
				sessionData.setConsultationType(updatedSession.getConsultationType());
				sessionData.setSessionDateTime(updatedSession.getSessionDateTime());

				populateInitialData(counselor, participants);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Error refreshing session data", e);
			ErrorDialogUtils.showError(this, "Could not refresh session data.");
		}
	}

	/**
	 * Finds a participant from the database by their full name.
	 * 
	 * @param fullName The full name to search for.
	 * @return The found Participant object, or a new one if not found.
	 */
	private Participants findParticipantByName(String fullName) {
		try {
			SessionsDAO sessionsDAO = new SessionsDAO(conn);
			List<Participants> sessionParticipants = sessionsDAO.getParticipantsBySessionId(sessionData.getSessionId());
			for (Participants p : sessionParticipants) {
				String nameInDb = p.getParticipantFirstName() + " " + p.getParticipantLastName();
				if (nameInDb.equalsIgnoreCase(fullName)) {
					return p;
				}
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.WARNING, "Could not fetch full participant details from database", ex);
		}
		// Fallback if not found
		String[] nameParts = fullName.split(" ", 2);
		Participants p = new Participants();
		p.setParticipantFirstName(nameParts[0]);
		p.setParticipantLastName(nameParts.length > 1 ? nameParts[1] : "");
		return p;
	}

	/**
	 * Displays a modal dialog with participant details and their violation history.
	 * 
	 * @param fullName The full name of the participant.
	 * @param type     The type of the participant (e.g., Student).
	 */
	private void showParticipantDetailsDialog(String fullName, String type) {
		// Main content panel for the modal
		JPanel modalContent = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow]"));
		modalContent.setBackground(BACKGROUND_COLOR);

		// --- Participant Information Section ---
		JPanel participantSection = new JPanel(new MigLayout("wrap 2, fill", "[grow][grow]", "[]"));
		participantSection.setBackground(BACKGROUND_COLOR);
		participantSection.setBorder(BorderFactory.createTitledBorder("Participant Information"));
		participantSection.add(createDetailItem("Full Name", fullName), "grow");
		participantSection.add(createDetailItem("Participant Type", type), "grow");
		modalContent.add(participantSection, "growx, wrap, gaptop 10");

		// --- Violations Section ---
		JPanel violationsSection = createViolationsPanel(fullName);
		modalContent.add(violationsSection, "grow, push");

		// --- Show Modal ---
		SimpleModalBorder modal = new SimpleModalBorder(modalContent, "Participant Details",
				new SimpleModalBorder.Option[] {
						new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION) },
				(controller, action) -> {
					if (action == SimpleModalBorder.CLOSE_OPTION) {
						controller.close();
					}
				});

		java.awt.Window parentWindow = SwingUtilities.getWindowAncestor(this);
		if (parentWindow == null) {
			// Fallback to Main.gFrame if the current component is not yet in a window hierarchy
			parentWindow = lyfjshs.gomis.Main.gFrame;
		}

		ModalDialog.showModal(parentWindow, modal, "participant_details_dialog");
		ModalDialog.getDefaultOption().getLayoutOption().setSize(800, 600);
	}

	/**
	 * Creates a panel containing a table of violations for a given participant.
	 * 
	 * @param fullName The full name of the participant to fetch violations for.
	 * @return A JPanel containing the violations history.
	 */
	private JPanel createViolationsPanel(String fullName) {
		JPanel violationsSection = new JPanel(new MigLayout("fill", "[grow]", "[][grow]"));
		violationsSection.setBackground(BACKGROUND_COLOR);
		violationsSection.setBorder(BorderFactory.createTitledBorder("Violation History"));

		// Table setup
		String[] columnNames = { "Violation Type", "Description", "Status", "Date" };
		DefaultTableModel violationsModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable violationsTable = new JTable(violationsModel);
		violationsTable.setRowHeight(30);
		violationsTable.getTableHeader().setBackground(SECONDARY_COLOR);
		violationsTable.getTableHeader().setForeground(TEXT_SECONDARY_COLOR);
		violationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

		// Populate data
		try {
			ViolationDAO violationDAO = new ViolationDAO(conn);
			List<Violation> violations = violationDAO.getViolationsByParticipantName(fullName);
			if (violations.isEmpty()) {
				violationsSection.add(new JLabel("No violation history found."), "growx, wrap");
			} else {
				for (Violation violation : violations) {
					violationsModel.addRow(new Object[] { violation.getViolationType(),
							violation.getViolationDescription(), violation.getStatus(), violation.getUpdatedAt()
									.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) });
				}
				violationsSection.add(new JScrollPane(violationsTable), "grow");
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error fetching violations", e);
			violationsSection.add(new JLabel("Error loading violation history."), "growx, wrap");
		}

		return violationsSection;
	}

	/**
	 * Creates a styled key-value pair panel for the details modal.
	 * 
	 * @param label The label text.
	 * @param value The value text.
	 * @return The created JPanel.
	 */
	private JPanel createDetailItem(String label, String value) {
		JPanel item = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[][]"));
		item.setBackground(BACKGROUND_COLOR);

		JLabel labelLbl = new JLabel(label);
		labelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		labelLbl.setForeground(TEXT_SECONDARY_COLOR);
		item.add(labelLbl, "growx, wrap");

		JLabel valueLbl = new JLabel(value);
		valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
		valueLbl.setForeground(TEXT_PRIMARY_COLOR);
		item.add(valueLbl, "growx");

		return item;
	}
}
