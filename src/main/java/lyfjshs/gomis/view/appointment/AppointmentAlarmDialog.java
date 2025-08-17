/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.alarm.AlarmManagement;
import net.miginfocom.swing.MigLayout;

public class AppointmentAlarmDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JLabel missedLabel;
	private AppointmentManagement appointmentManagement;
	private static final Logger logger = LogManager.getLogger(AppointmentAlarmDialog.class);

	public AppointmentAlarmDialog(Appointment appointment, Runnable onStartSession, AlarmManagement alarmManagement, AppointmentManagement appointmentManagement) {
		super((Frame) null, "Appointment Reminder", true);
		this.appointmentManagement = appointmentManagement;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][][grow][][][grow]"));

		// Title panel
		JPanel titlePanel = new JPanel(new MigLayout("fill", "[grow]", "[]"));
		JLabel titleLabel = new JLabel("Appointment Reminder");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
		titlePanel.add(titleLabel, "center");
		add(titlePanel, "growx, wrap");

		// Content panel
		JPanel contentPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[][][][][][][][][]"));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Appointment details
		JLabel appointmentTitle = new JLabel(appointment.getAppointmentTitle());
		appointmentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
		contentPanel.add(appointmentTitle, "growx, wrap");

		JLabel appointmentTime = new JLabel(
				appointment.getAppointmentDateTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
		appointmentTime.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		contentPanel.add(appointmentTime, "growx, wrap");

		// Add missed label (initially hidden)
		missedLabel = new JLabel("This appointment is marked as Missed!");
		missedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		missedLabel.setForeground(new Color(220, 53, 69));
		missedLabel.setVisible(false);
		contentPanel.add(missedLabel, "growx, wrap");

		add(contentPanel, "grow, wrap");

		// Button panel
		JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 10", "[grow][grow][grow]", "[]"));

		// Start Session button
		JButton startSessionButton = new JButton("Start Session Now");
		startSessionButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
		startSessionButton.setBackground(new Color(40, 167, 69));
		startSessionButton.setForeground(Color.WHITE);
		startSessionButton.addActionListener(e -> {
			AlarmManagement.getInstance().handleStartSession(appointment);
		});

		// Snooze button
		JButton snoozeButton = new JButton("Snooze (5 min)");
		snoozeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
		snoozeButton.setBackground(new Color(255, 193, 7));
		snoozeButton.setForeground(Color.WHITE);
		snoozeButton.addActionListener(e -> {
			AlarmManagement.getInstance().handleSnooze(5);
		});

		// Cancel button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cancelButton.setBackground(new Color(220, 53, 69)); // Red color
		cancelButton.setForeground(Color.WHITE);
		cancelButton.addActionListener(e -> {
			AlarmManagement.getInstance().handleCancel(appointment);
		});

		buttonPanel.add(startSessionButton, "growx");
		buttonPanel.add(snoozeButton, "growx");
		buttonPanel.add(cancelButton, "growx");

		add(buttonPanel, "growx");

		// Set dialog properties
		pack();
		setLocationRelativeTo(null);
		setResizable(true);
	}

	public void showMissedLabel() {
		if (missedLabel != null) {
			missedLabel.setVisible(true);
		}
	}

}
