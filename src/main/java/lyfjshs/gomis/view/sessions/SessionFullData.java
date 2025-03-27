package lyfjshs.gomis.view.sessions;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.view.incident.IncidentFillUpForm;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Location;
import raven.modal.option.Option;

import java.sql.Connection;
import java.sql.SQLException;

public class SessionFullData extends Form {
	private static final Logger LOGGER = Logger.getLogger(SessionFullData.class.getName());
	private final Connection conn;

	// Colors from the CSS
	private static final Color CONTAINER_BG = Color.WHITE;
	private static final Color CARD_BG = new Color(0xF8F9FA);
	private static final Color BORDER_COLOR = new Color(0xE9ECEF);
	private static final Color HEADER_COLOR = new Color(0x2C3E50);
	private static final Color TEXT_COLOR = new Color(0x212529);
	private static final Color SUBTEXT_COLOR = new Color(0x495057);
	private static final Color TABLE_HEADER_BG = new Color(0xF1F3F5);
	private static final Color BLUE_BUTTON = new Color(0x3498DB);
	private static final Color GREEN_BUTTON = new Color(0x2ECC71);
	private static final Color RED_BUTTON = new Color(0xE74C3C);
	private static final Color MODAL_CLOSE_COLOR = new Color(0x6C757D);

	// Fonts
	private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
	private static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.PLAIN, 12);
	private static final Font CARD_VALUE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);
	private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 12);
	private static final Font MODAL_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
	private static final Font MODAL_LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
	private static final Font MODAL_VALUE_FONT = new Font("Segoe UI", Font.BOLD, 14);

	private JTable participantsTable;
	private JButton printSessionReportBtn;
	private JButton editSessionBtn;
	private JButton endSessionBtn;
	private final Sessions sessionData;

	public SessionFullData(Sessions sessionData, GuidanceCounselor counselor, List<Participants> participants, Connection conn) {
		this.sessionData = sessionData;
		this.conn = conn;
		
		// Set FlatLaf look and feel
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e) {
			LOGGER.log(Level.SEVERE, "Failed to set FlatLaf look and feel", e);
		}

		// Panel setup
		setLayout(new MigLayout("fill, insets 30", "[grow]", "[][][][][][]"));
		setBackground(CONTAINER_BG);
		setBorder(BorderFactory.createLineBorder(new Color(0xE7E9EC), 2));

		// Build UI components
		add(createHeaderPanel(), "growx, wrap");
		add(createSessionGrid(sessionData, counselor), "growx, wrap");
		add(createSessionSummary(sessionData), "growx, wrap, gaptop 20");
		add(createSessionNotes(sessionData), "growx, wrap, gaptop 20");
		add(createParticipantsTable(participants), "growx, wrap, gaptop 20");
		add(createActionButtons(), "growx, gaptop 20");
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new MigLayout("fill", "[grow][]", "[]"));
		headerPanel.setBackground(CONTAINER_BG);
		headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

		JLabel headerLabel = new JLabel("Session Details");
		headerLabel.setFont(HEADER_FONT);
		headerLabel.setForeground(HEADER_COLOR);
		headerPanel.add(headerLabel, "growx");

		return headerPanel;
	}

	private JPanel createSessionGrid(Sessions sessionData, GuidanceCounselor counselor) {
		JPanel sessionGrid = new JPanel(new MigLayout("wrap 3, fill", "[grow][grow][grow]", "[][]"));
		sessionGrid.setBackground(CONTAINER_BG);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String sessionDateTime = sessionData.getSessionDateTime() != null ? 
			sessionData.getSessionDateTime().toLocalDateTime().format(formatter) : "N/A";
		String updatedAt = sessionData.getUpdatedAt() != null ? 
			sessionData.getUpdatedAt().toLocalDateTime().format(formatter) : "N/A";
		String counselorName = counselor != null ? 
			counselor.getFirstName() + " " + counselor.getLastName() : "N/A";

		// Add all session data in a grid layout
		sessionGrid.add(createSessionCard("SESSION DATE/TIME", sessionDateTime), "grow");
		sessionGrid.add(createSessionCard("SESSION STATUS", sessionData.getSessionStatus()), "grow");
		sessionGrid.add(createSessionCard("APPOINTMENT TYPE", sessionData.getAppointmentType()), "grow");
		sessionGrid.add(createSessionCard("COUNSELOR NAME", counselorName), "grow");
		sessionGrid.add(createSessionCard("CONSULTATION TYPE", sessionData.getConsultationType()), "grow");
		sessionGrid.add(createSessionCard("UPDATED AT", updatedAt), "grow");

		return sessionGrid;
	}

	private JPanel createSessionCard(String title, String value) {
		JPanel card = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
		card.setBackground(CARD_BG);
		card.setBorder(new LineBorder(BORDER_COLOR, 1));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(CARD_TITLE_FONT);
		titleLabel.setForeground(SUBTEXT_COLOR);
		card.add(titleLabel, "growx, wrap");

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(CARD_VALUE_FONT);
		valueLabel.setForeground(TEXT_COLOR);
		card.add(valueLabel, "growx");

		return card;
	}

	private JPanel createSessionSummary(Sessions sessionData) {
		JPanel sessionSummary = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
		sessionSummary.setBackground(CARD_BG);
		sessionSummary.setBorder(new LineBorder(BORDER_COLOR, 1));
		sessionSummary.setPreferredSize(new Dimension(0, 80));

		JLabel summaryLabel = new JLabel("SESSION SUMMARY");
		summaryLabel.setFont(CARD_TITLE_FONT);
		summaryLabel.setForeground(SUBTEXT_COLOR);
		sessionSummary.add(summaryLabel, "growx, wrap");

		String summaryText = sessionData.getSessionSummary();
		if (summaryText == null || summaryText.trim().isEmpty()) {
			summaryText = "No summary available from the session.";
		}

		JTextArea summaryTextArea = new JTextArea(summaryText);
		summaryTextArea.setFont(CARD_VALUE_FONT);
		summaryTextArea.setForeground(TEXT_COLOR);
		summaryTextArea.setLineWrap(true);
		summaryTextArea.setWrapStyleWord(true);
		summaryTextArea.setEditable(false);
		summaryTextArea.setBackground(CARD_BG);
		summaryTextArea.setBorder(null);
		sessionSummary.add(summaryTextArea, "growx");

		return sessionSummary;
	}

	private JPanel createSessionNotes(Sessions sessionData) {
		JPanel sessionNotes = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
		sessionNotes.setBackground(CARD_BG);
		sessionNotes.setBorder(new LineBorder(BORDER_COLOR, 1));
		sessionNotes.setPreferredSize(new Dimension(0, 120));

		JLabel notesLabel = new JLabel("SESSION NOTES");
		notesLabel.setFont(CARD_TITLE_FONT);
		notesLabel.setForeground(SUBTEXT_COLOR);
		sessionNotes.add(notesLabel, "growx, wrap");

		String notesText = sessionData.getSessionNotes();
		if (notesText == null || notesText.trim().isEmpty()) {
			notesText = "No notes available from the session.";
		}

		JTextArea notesTextArea = new JTextArea(notesText);
		notesTextArea.setFont(CARD_VALUE_FONT);
		notesTextArea.setForeground(TEXT_COLOR);
		notesTextArea.setLineWrap(true);
		notesTextArea.setWrapStyleWord(true);
		notesTextArea.setEditable(false);
		notesTextArea.setBackground(CARD_BG);
		notesTextArea.setBorder(null);
		sessionNotes.add(notesTextArea, "growx");

		return sessionNotes;
	}

	private JScrollPane createParticipantsTable(List<Participants> participants) {
		String[] columnNames = {"#", "Participant Name", "Participant Type", "Actions"};
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 3; // Only allow editing of Actions column
			}
		};

		if (participants != null && !participants.isEmpty()) {
			for (int i = 0; i < participants.size(); i++) {
				Participants participant = participants.get(i);
				String fullName = participant.getParticipantFirstName() + " " + participant.getParticipantLastName();
				Object[] rowData = {
					i + 1,
					fullName,
					participant.getParticipantType(),
					"Actions"
				};
				model.addRow(rowData);
			}
		}

		participantsTable = new JTable(model);
		participantsTable.setRowHeight(40);
		participantsTable.getTableHeader().setBackground(TABLE_HEADER_BG);
		participantsTable.getTableHeader().setForeground(SUBTEXT_COLOR);
		participantsTable.getTableHeader().setFont(TABLE_HEADER_FONT);
		participantsTable.setBorder(new LineBorder(new Color(0xDEE2E6), 1));

		setupTableActions();

		return new JScrollPane(participantsTable);
	}

	private void setupTableActions() {
		TableActionManager actionManager = new TableActionManager();
		actionManager.addAction("View", (table, row) -> {
			String fullName = (String) table.getValueAt(row, 1);
			String type = (String) table.getValueAt(row, 2);
			showParticipantDetailsDialog(fullName, type);
		}, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.5f));

		actionManager.applyTo(participantsTable, 3);
	}

	private void showParticipantDetailsDialog(String fullName, String type) {
		JPanel modalContent = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][][][]"));
		modalContent.setBackground(CONTAINER_BG);

		// Participant Details Section
		JPanel participantSection = new JPanel(new MigLayout("wrap 2, fill", "[grow][grow]", "[][]"));
		participantSection.setBackground(CONTAINER_BG);
		participantSection.setBorder(BorderFactory.createTitledBorder("Participant Information"));

		participantSection.add(createDetailItem("Full Name", fullName), "grow");
		participantSection.add(createDetailItem("Participant Type", type), "grow");

		modalContent.add(participantSection, "growx, wrap");

		// Violations Section
		JPanel violationsSection = new JPanel(new MigLayout("fill", "[grow]", "[][grow]"));
		violationsSection.setBackground(CONTAINER_BG);
		violationsSection.setBorder(BorderFactory.createTitledBorder("Violations"));

		// Create table for violations
		String[] columnNames = {"Violation Type", "Description", "Status", "Date"};
		DefaultTableModel violationsModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable violationsTable = new JTable(violationsModel);
		violationsTable.setRowHeight(30);
		violationsTable.getTableHeader().setBackground(TABLE_HEADER_BG);
		violationsTable.getTableHeader().setForeground(SUBTEXT_COLOR);
		violationsTable.getTableHeader().setFont(TABLE_HEADER_FONT);
		violationsTable.setBorder(new LineBorder(new Color(0xDEE2E6), 1));

		// Add violations data
		try {
			ViolationCRUD violationDAO = new ViolationCRUD(conn);
			List<ViolationRecord> violations = violationDAO.getViolationsByParticipantName(fullName);
			
			for (ViolationRecord violation : violations) {
				violationsModel.addRow(new Object[] {
					violation.getViolationType(),
					violation.getViolationDescription(),
					violation.getStatus(),
					violation.getUpdatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
				});
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error fetching violations", e);
		}

		violationsSection.add(new JLabel("Violation History"), "growx, wrap");
		violationsSection.add(new JScrollPane(violationsTable), "grow");

		modalContent.add(violationsSection, "growx, wrap");

		// Show modal using ModalDialog
		ModalDialog.showModal(
			SwingUtilities.getWindowAncestor(this),
			new SimpleModalBorder(modalContent, "Participant Details", new SimpleModalBorder.Option[] {
				new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
			}, (controller, action) -> {
				if (action == SimpleModalBorder.CLOSE_OPTION) {
					controller.close();
				}
			}),
			"input"
		);
		ModalDialog.getDefaultOption().getLayoutOption().setSize(800, 600);
	}

	private JPanel createDetailItem(String label, String value) {
		JPanel item = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
		item.setBackground(CONTAINER_BG);

		JLabel labelLbl = new JLabel(label);
		labelLbl.setFont(MODAL_LABEL_FONT);
		labelLbl.setForeground(MODAL_CLOSE_COLOR);
		item.add(labelLbl, "growx, wrap");

		JLabel valueLbl = new JLabel(value);
		valueLbl.setFont(MODAL_VALUE_FONT);
		valueLbl.setForeground(TEXT_COLOR);
		item.add(valueLbl, "growx");

		return item;
	}

	private JPanel createActionButtons() {
		JPanel actionButtons = new JPanel(new MigLayout("fill", "[grow][]", "[]"));
		actionButtons.setBackground(CONTAINER_BG);

		JButton createIncidentBtn = createButton("Create Incident Report", BLUE_BUTTON);
		createIncidentBtn.addActionListener(e -> createIncidentReport());

		// Add Edit and End Session buttons if session is active
		if ("Active".equals(sessionData.getSessionStatus())) {
			editSessionBtn = createButton("Edit Session", GREEN_BUTTON);
			editSessionBtn.addActionListener(e -> editSession());

			endSessionBtn = createButton("End Session", RED_BUTTON);
			endSessionBtn.addActionListener(e -> endSession());

			actionButtons.add(editSessionBtn, "center");
			actionButtons.add(endSessionBtn, "center");
		}

		actionButtons.add(createIncidentBtn, "center");

		return actionButtons;
	}

	private void createIncidentReport() {
		try {
			// Get all participants from the table
			DefaultTableModel model = (DefaultTableModel) participantsTable.getModel();
			List<Participants> participants = new ArrayList<>();
			
			for (int i = 0; i < model.getRowCount(); i++) {
				String fullName = (String) model.getValueAt(i, 1);
				String type = (String) model.getValueAt(i, 2);
				
				// Split full name into first and last name
				String[] nameParts = fullName.split(" ", 2);
				String firstName = nameParts[0];
				String lastName = nameParts.length > 1 ? nameParts[1] : "";
				
				Participants participant = new Participants();
				participant.setParticipantFirstName(firstName);
				participant.setParticipantLastName(lastName);
				participant.setParticipantType(type);
				participants.add(participant);
			}

			// Create and show the IncidentFillUpForm using FormManager
			IncidentFillUpForm incidentForm = new IncidentFillUpForm(conn);
			incidentForm.populateFromSession(sessionData, participants);
			FormManager.showForm(incidentForm);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, 
				"Error creating incident report: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void editSession() {
		try {
			SessionsForm sessionsForm = new SessionsForm(conn);
			sessionsForm.setEditingSession(sessionData);
			sessionsForm.setSaveCallback(() -> {
				// Refresh the current view
				try {
					SessionsDAO sessionsDAO = new SessionsDAO(conn);
					Sessions updatedSession = sessionsDAO.getSessionById(sessionData.getSessionId());
					if (updatedSession != null) {
						// Update the view with new data
						updateSessionView(updatedSession);
					}
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "Error refreshing session data", e);
				}
			});
			FormManager.showForm(sessionsForm);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error opening edit form", e);
			JOptionPane.showMessageDialog(this, 
				"Error opening edit form: " + e.getMessage(), 
				"Error", 
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void endSession() {
		int confirm = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to end this session? This action cannot be undone.",
			"Confirm End Session",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			try {
				sessionData.setSessionStatus("Ended");
				SessionsDAO sessionsDAO = new SessionsDAO(conn);
				sessionsDAO.updateSession(sessionData);

				JOptionPane.showMessageDialog(this, 
					"Session has been ended successfully.", 
					"Success", 
					JOptionPane.INFORMATION_MESSAGE);

				// Update the view
				updateSessionView(sessionData);
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "Error ending session", e);
				JOptionPane.showMessageDialog(this, 
					"Error ending session: " + e.getMessage(), 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void updateSessionView(Sessions updatedSession) {
		// Update the session data
		this.sessionData.setSessionStatus(updatedSession.getSessionStatus());
		this.sessionData.setSessionSummary(updatedSession.getSessionSummary() != null ? updatedSession.getSessionSummary() : "");
		this.sessionData.setSessionNotes(updatedSession.getSessionNotes());
		this.sessionData.setAppointmentType(updatedSession.getAppointmentType());
		this.sessionData.setConsultationType(updatedSession.getConsultationType());
		this.sessionData.setSessionDateTime(updatedSession.getSessionDateTime());

		// Refresh the UI components
		removeAll();
		add(createHeaderPanel(), "growx, wrap");
		add(createSessionGrid(sessionData, getCounselor()), "growx, wrap");
		add(createSessionSummary(sessionData), "growx, wrap, gaptop 20");
		add(createSessionNotes(sessionData), "growx, wrap, gaptop 20");
		add(createParticipantsTable(getParticipants()), "growx, wrap, gaptop 20");
		add(createActionButtons(), "growx, gaptop 20");
		revalidate();
		repaint();
	}

	private GuidanceCounselor getCounselor() {
		try {
			SessionsDAO sessionsDAO = new SessionsDAO(conn);
			return sessionsDAO.getCounselorById(sessionData.getGuidanceCounselorId());
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Error getting counselor", e);
			return null;
		}
	}

	private List<Participants> getParticipants() {
		try {
			SessionsDAO sessionsDAO = new SessionsDAO(conn);
			return sessionsDAO.getParticipantsBySessionId(sessionData.getSessionId());
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Error getting participants", e);
			return new ArrayList<>();
		}
	}

	private JButton createButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(BUTTON_FONT);
		button.setForeground(Color.WHITE);
		button.setBackground(bgColor);
		button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return button;
	}

	private void printSessionReport() {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d'th' 'day of' MMMM, yyyy");
			String formattedDate = LocalDateTime.now().format(formatter);
			JOptionPane.showMessageDialog(this,
					"Printing Session Report for Session Date/Time: " + 
					(sessionData.getSessionDateTime() != null ? 
						sessionData.getSessionDateTime().toLocalDateTime().format(formatter) : "N/A") + 
					" on " + formattedDate);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Failed to print session report: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
