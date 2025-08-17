/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.sessions.fill_up;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class SessionDetailsPanel extends JPanel {
	private final JFormattedTextField sessionDateField = new JFormattedTextField();
	private final JFormattedTextField sessionTimeField = new JFormattedTextField();
	private final DatePicker datePicker = new DatePicker();
	private final TimePicker timePicker = new TimePicker();
	private final JComboBox<String> appointmentTypeCombo = new JComboBox<>(new String[] { "Select Appoint Type", "Walk-in", "Scheduled", "Emergency" });
	private final JComboBox<String> consultationTypeCombo = new JComboBox<>(new String[] { "Select Consultation Type", "Academic Consultation", "Career Guidance", "Personal Consultation", "Behavioral Consultation", "Group Consultation" });
	private final JComboBox<String> sessionStatusCombo = new JComboBox<>(new String[] { "Select Status", "Active", "Ended" });
	private final JTextArea notesArea = new JTextArea(3, 0);
	private final JFormattedTextField rescheduleDateField = new JFormattedTextField();
	private final JFormattedTextField rescheduleTimeField = new JFormattedTextField();
	private final DatePicker rescheduleDatePicker = new DatePicker();
	private final TimePicker rescheduleTimePicker = new TimePicker();
	private final JPanel formBg;
	private final JPanel reschedulePanel = new JPanel(new MigLayout("", "[grow][grow]", "[][]"));
	private JButton searchAppointmentBtn;
	private Integer selectedAppointmentId = null;

	public SessionDetailsPanel() {
		setOpaque(false);
		setLayout(new BorderLayout());
		datePicker.setSelectedDate(LocalDate.now());
		timePicker.setSelectedTime(LocalTime.now());
		rescheduleDatePicker.setSelectedDate(LocalDate.now().plusDays(7));
		rescheduleTimePicker.setSelectedTime(LocalTime.now());

		// Main white rounded panel
		formBg = new JPanel(
				new MigLayout("", "[::300px,grow,fill][grow][::300px,grow,fill][grow][::400px,grow]", "[][][][][][]"));
		formBg.setOpaque(false);
		formBg.putClientProperty(FlatClientProperties.STYLE, "arc:20");

		Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
		Color requiredColor = new Color(220, 53, 69);

		// Session Date
		JLabel dateLabel = new JLabel("Session Date ");
		dateLabel.setFont(labelFont);
		JLabel dateAsterisk = new JLabel("*");
		dateAsterisk.setForeground(requiredColor);
		dateAsterisk.setFont(labelFont);
		JPanel dateLabelPanel = new JPanel(new BorderLayout());
		dateLabelPanel.setOpaque(false);
		dateLabelPanel.add(dateLabel, BorderLayout.WEST);
		dateLabelPanel.add(dateAsterisk, BorderLayout.EAST);
		formBg.add(dateLabelPanel, "cell 0 0,growx");

		// Session Time
		JLabel timeLabel = new JLabel("Session Time ");
		timeLabel.setFont(labelFont);
		JLabel timeAsterisk = new JLabel("*");
		timeAsterisk.setForeground(requiredColor);
		timeAsterisk.setFont(labelFont);
		JPanel timeLabelPanel = new JPanel(new BorderLayout());
		timeLabelPanel.setOpaque(false);
		timeLabelPanel.add(timeLabel, BorderLayout.WEST);
		timeLabelPanel.add(timeAsterisk, BorderLayout.EAST);
		formBg.add(timeLabelPanel, "cell 2 0,growx");

		// Appointment Type
		JLabel apptTypeLabel = new JLabel("Appointment Type ");
		apptTypeLabel.setFont(labelFont);
		JLabel apptAsterisk = new JLabel("*");
		apptAsterisk.setForeground(requiredColor);
		apptAsterisk.setFont(labelFont);
		JPanel apptLabelPanel = new JPanel(new BorderLayout());
		apptLabelPanel.setOpaque(false);
		apptLabelPanel.add(apptTypeLabel, BorderLayout.WEST);
		apptLabelPanel.add(apptAsterisk, BorderLayout.EAST);
		formBg.add(apptLabelPanel, "cell 4 0,growx");

		// Consultation Type
		JLabel consultTypeLabel = new JLabel("Consultation Type ");
		consultTypeLabel.setFont(labelFont);
		JLabel consultAsterisk = new JLabel("*");
		consultAsterisk.setForeground(requiredColor);
		consultAsterisk.setFont(labelFont);
		JPanel consultLabelPanel = new JPanel(new BorderLayout());
		consultLabelPanel.setOpaque(false);
		consultLabelPanel.add(consultTypeLabel, BorderLayout.WEST);
		consultLabelPanel.add(consultAsterisk, BorderLayout.EAST);

		// Link pickers to fields
		datePicker.setEditor(sessionDateField);
		sessionDateField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "mm/dd/yyyy");
		formBg.add(sessionDateField, "cell 0 1,growx");
		timePicker.setEditor(sessionTimeField);
		sessionTimeField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "--:-- --");
		formBg.add(sessionTimeField, "cell 2 1,growx");
		appointmentTypeCombo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "-- Select Appointment Type --");
		formBg.add(appointmentTypeCombo, "flowx,cell 4 1,growx");
		formBg.add(consultLabelPanel, "cell 0 2,growx");

		// Session Status
		JLabel statusLabel = new JLabel("Session Status ");
		statusLabel.setFont(labelFont);
		JLabel statusAsterisk = new JLabel("*");
		statusAsterisk.setForeground(requiredColor);
		statusAsterisk.setFont(labelFont);
		JPanel statusLabelPanel = new JPanel(new BorderLayout());
		statusLabelPanel.setOpaque(false);
		statusLabelPanel.add(statusLabel, BorderLayout.WEST);
		statusLabelPanel.add(statusAsterisk, BorderLayout.EAST);
		formBg.add(statusLabelPanel, "cell 2 2,growx");

		// Session Notes
		JLabel notesLabel = new JLabel("Session Notes");
		notesLabel.setFont(labelFont);
		formBg.add(notesLabel, "cell 4 2,growx");

		consultationTypeCombo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "-- Select Consultation Type --");
		formBg.add(consultationTypeCombo, "cell 0 3,growx");
		sessionStatusCombo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "-- Select Status --");
		formBg.add(sessionStatusCombo, "cell 2 3,growx");
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
				"Any additional notes or context for this session...");
		JScrollPane notesScroll = new JScrollPane(notesArea);
		notesScroll.setPreferredSize(new Dimension(0, 60));
		notesScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
		formBg.add(notesScroll, "cell 4 3 1 2,grow");
		rescheduleTimePicker.setEditor(rescheduleTimeField);

		// Reschedule fields (hidden by default, only for Active sessions)
		reschedulePanel.setOpaque(false);
		JLabel reschedDateLabel = new JLabel("Reschedule Date");
		rescheduleTimeField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "--:-- --");
		reschedulePanel.add(reschedDateLabel, "cell 0 0,growx");
		JLabel reschedTimeLabel = new JLabel("Reschedule Time");
		reschedulePanel.add(reschedTimeLabel, "cell 1 0,growx");
		rescheduleDatePicker.setEditor(rescheduleDateField);
		rescheduleDateField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "mm/dd/yyyy");
		reschedulePanel.add(rescheduleDateField, "cell 0 1,growx");
		reschedulePanel.add(rescheduleTimeField, "cell 1 1,growx");
		reschedulePanel.setVisible(false);
		formBg.add(reschedulePanel, "cell 0 5 5 1,growx");
		searchAppointmentBtn = new JButton("Select Appointment");
		formBg.add(searchAppointmentBtn, "cell 4 1");
		searchAppointmentBtn.setEnabled(false);
		// Enable/disable searchAppointmentBtn based on appointment type
		appointmentTypeCombo.addActionListener(e -> {
			String selected = (String) appointmentTypeCombo.getSelectedItem();
			boolean enable = "Scheduled".equals(selected);
			searchAppointmentBtn.setEnabled(enable);
		});
		searchAppointmentBtn.addActionListener(e -> {
			if (searchAppointmentBtn.isEnabled()) {
				showAppointmentSelectionDialog();
			}
		});

		// Show/hide reschedule panel based on status
		sessionStatusCombo.addActionListener(e -> {
			String selectedStatus = (String) sessionStatusCombo.getSelectedItem();
			reschedulePanel.setVisible("Active".equals(selectedStatus));

		});
	}

	// Stub for appointment selection dialog
	private void showAppointmentSelectionDialog() {
		try {
			// Create a simple dialog to select appointments
			JDialog dialog = new JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), "Select Appointment", true);
			dialog.setLayout(new java.awt.BorderLayout());
			dialog.setSize(600, 400);
			dialog.setLocationRelativeTo(this);
			
			// Create table model for appointments
			String[] columns = {"ID", "Title", "Date", "Time", "Type", "Status"};
			javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			
			JTable appointmentTable = new JTable(model);
			JScrollPane scrollPane = new JScrollPane(appointmentTable);
			
			// Add sample data (in real implementation, load from database)
			model.addRow(new Object[]{"1", "Academic Consultation", "2024-01-15", "09:00", "Scheduled", "Active"});
			model.addRow(new Object[]{"2", "Career Guidance", "2024-01-16", "14:30", "Scheduled", "Active"});
			model.addRow(new Object[]{"3", "Personal Consultation", "2024-01-17", "10:00", "Walk-in", "Active"});
			
			// Add buttons
			JPanel buttonPanel = new JPanel();
			JButton selectButton = new JButton("Select");
			JButton cancelButton = new JButton("Cancel");
			
			selectButton.addActionListener(e -> {
				int selectedRow = appointmentTable.getSelectedRow();
				if (selectedRow >= 0) {
					// Get appointment data and fill fields
					String appointmentId = model.getValueAt(selectedRow, 0).toString();
					String title = model.getValueAt(selectedRow, 1).toString();
					String date = model.getValueAt(selectedRow, 2).toString();
					String time = model.getValueAt(selectedRow, 3).toString();
					String type = model.getValueAt(selectedRow, 4).toString();
					
					// Fill the fields
					setSessionDate(date);
					setSessionTime(time);
					setAppointmentType(type);
					setConsultationType(title);
					setSessionStatus("Active");
					
					selectedAppointmentId = Integer.parseInt(appointmentId);
					dialog.dispose();
				} else {
					JOptionPane.showMessageDialog(dialog, "Please select an appointment first.", "No Selection", JOptionPane.WARNING_MESSAGE);
				}
			});
			
			cancelButton.addActionListener(e -> dialog.dispose());
			
			buttonPanel.add(selectButton);
			buttonPanel.add(cancelButton);
			
			dialog.add(scrollPane, java.awt.BorderLayout.CENTER);
			dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);
			
			dialog.setVisible(true);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error showing appointment dialog: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Stub for filling fields from selected appointment
	public void setAppointmentFieldsFromSelection(lyfjshs.gomis.Database.entity.Appointment appointment) {
		if (appointment == null) return;
		
		try {
			// Fill in fields from selected appointment
			if (appointment.getAppointmentDateTime() != null) {
				java.time.LocalDateTime dateTime = appointment.getAppointmentDateTime().toLocalDateTime();
				setSessionDate(dateTime.toLocalDate().toString());
				setSessionTime(dateTime.toLocalTime().toString());
			}
			
			setAppointmentType("Scheduled");
			setConsultationType(appointment.getConsultationType());
			setSessionStatus("Active");
			setNotes(appointment.getAppointmentNotes() != null ? appointment.getAppointmentNotes() : "");
			
			selectedAppointmentId = appointment.getAppointmentId();
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error setting appointment fields: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Getters for field values (for use in main form)
	public String getSessionDate() {
		return sessionDateField.getText();
	}

	public String getSessionTime() {
		return sessionTimeField.getText();
	}

	public String getAppointmentType() {
		return (String) appointmentTypeCombo.getSelectedItem();
	}

	public String getConsultationType() {
		return (String) consultationTypeCombo.getSelectedItem();
	}

	public String getSessionStatus() {
		return (String) sessionStatusCombo.getSelectedItem();
	}

	public String getNotes() {
		return notesArea.getText();
	}

	public String getRescheduleDate() {
		return rescheduleDateField.getText();
	}

	public String getRescheduleTime() {
		return rescheduleTimeField.getText();
	}

	// Setters for field values (for use in main form)
	public void setSessionDate(String date) {
		sessionDateField.setText(date);
	}

	public void setSessionTime(String time) {
		sessionTimeField.setText(time);
	}

	public void setAppointmentType(String type) {
		appointmentTypeCombo.setSelectedItem(type);
	}

	public void setConsultationType(String type) {
		consultationTypeCombo.setSelectedItem(type);
	}

	public void setSessionStatus(String status) {
		sessionStatusCombo.setSelectedItem(status);
	}

	public void setNotes(String notes) {
		notesArea.setText(notes);
	}

	public void setRescheduleDate(String date) {
		rescheduleDateField.setText(date);
	}

	public void setRescheduleTime(String time) {
		rescheduleTimeField.setText(time);
	}

	public DatePicker getDatePicker() {
		return datePicker;
	}

	public TimePicker getTimePicker() {
		return timePicker;
	}

	public DatePicker getRescheduleDatePicker() {
		return rescheduleDatePicker;
	}

	public TimePicker getRescheduleTimePicker() {
		return rescheduleTimePicker;
	}

	public JPanel getReschedulePanel() {
		return reschedulePanel;
	}

	public void clearFields() {
		setSessionDate("");
		setSessionTime("");
		setAppointmentType("");
		setConsultationType("");
		setSessionStatus("");
		setNotes("");
		setRescheduleDate("");
		setRescheduleTime("");
		datePicker.setSelectedDate(java.time.LocalDate.now());
		timePicker.setSelectedTime(java.time.LocalTime.now());
		rescheduleDatePicker.setSelectedDate(java.time.LocalDate.now().plusDays(7));
		rescheduleTimePicker.setSelectedTime(java.time.LocalTime.now());
		reschedulePanel.setVisible(false);
	}

	public JPanel getContentPanel() {
		return formBg;
	}

	public void getData(SessionFormData data) {
		data.sessionDate = getSessionDate();
		data.sessionTime = getSessionTime();
		data.appointmentType = getAppointmentType();
		data.consultationType = getConsultationType();
		data.sessionStatus = getSessionStatus();
		data.notes = getNotes();
		data.rescheduleDate = getRescheduleDate();
		data.rescheduleTime = getRescheduleTime();
	}

	public void setData(SessionFormData data) {
		setSessionDate(data.sessionDate);
		setSessionTime(data.sessionTime);
		setAppointmentType(data.appointmentType);
		setConsultationType(data.consultationType);
		setSessionStatus(data.sessionStatus);
		setNotes(data.notes);
		setRescheduleDate(data.rescheduleDate);
		setRescheduleTime(data.rescheduleTime);
	}
}