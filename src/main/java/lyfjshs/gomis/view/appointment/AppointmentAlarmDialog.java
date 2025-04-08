package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import lyfjshs.gomis.Database.entity.Appointment;
import net.miginfocom.swing.MigLayout;

public class AppointmentAlarmDialog extends JDialog {

	private static final long serialVersionUID = 1L;
    
	public AppointmentAlarmDialog(Appointment appointment, Runnable onStartSession, AppointmentAlarm appointmentAlarm) {

		setTitle("Appointment Reminder");
		setModal(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new MigLayout("fill, wrap 1", "[center]", "[]20[]20[][][]"));

		// Set up dialog components
		JLabel titleLabel = new JLabel(appointment.getAppointmentTitle());
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
		JLabel timeLabel = new JLabel(appointment.getAppointmentDateTime().toLocalDateTime()
			.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
		timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		JLabel typeLabel = new JLabel(appointment.getConsultationType());
		typeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));

		// Add components to dialog
		getContentPane().add(titleLabel, "cell 0 0");
		getContentPane().add(timeLabel, "cell 0 1");
		getContentPane().add(typeLabel, "cell 0 2");

		// Create a spinner for snooze duration
		JSpinner snoozeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
		JLabel label_1 = new JLabel("Snooze for:");
		getContentPane().add(label_1, "flowx,cell 0 3");
		getContentPane().add(snoozeSpinner, "cell 0 3");

		// Buttons panel
		JPanel buttonsPanel = new JPanel(new MigLayout("", "[]20[]20[]", "[]"));
		JButton startSessionButton = new JButton("Start Session");
		startSessionButton.setBackground(new Color(40, 167, 69));
		startSessionButton.setForeground(Color.WHITE);
		startSessionButton.addActionListener(e -> {
			if (onStartSession != null) {
				onStartSession.run();
			}
			dispose();
		});

		JButton snoozeButton = new JButton("Snooze");
		snoozeButton.addActionListener(e -> {
			int snoozeMinutes = (int) snoozeSpinner.getValue();
			LocalDateTime newDateTime = LocalDateTime.now().plusMinutes(snoozeMinutes);
			appointmentAlarm.updateAppointmentInDatabase(newDateTime, "Snoozed for " + snoozeMinutes + " minutes");
			appointmentAlarm.getAlarmManager().snooze(snoozeMinutes);
			dispose();
		});

		JButton dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(e -> {
			appointmentAlarm.getAlarmManager().cancelAlarm();
			dispose();
		});

		buttonsPanel.add(startSessionButton);
		buttonsPanel.add(snoozeButton);
		buttonsPanel.add(dismissButton);
		getContentPane().add(buttonsPanel, "cell 0 4");
		JLabel label = new JLabel("minutes");
		getContentPane().add(label, "cell 0 3");

		setSize(500, 300);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setVisible(true);
	}

}
