package lyfjshs.gomis.view.appointment.reschedule;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class AppointmentReschedModal {
	private static final Logger logger = LogManager.getLogger(AppointmentReschedModal.class);
	private static AppointmentReschedModal instance;

	private AppointmentReschedModal() {
	// Private constructor to enforce singleton pattern
	
	}

	public static AppointmentReschedModal getInstance() {
		if (instance == null) {
			instance = new AppointmentReschedModal();
		}
		return instance;
	}

	public void showReschedModal(Connection connection, Component parent, Appointment missedAppointment,
			AppointmentDAO appointmentDAO, int width, int height, Runnable onSuccess) {
		try {
			// Create the resched panel
			AppointmentReschedPanel panel = new AppointmentReschedPanel(missedAppointment, appointmentDAO, connection);

			// Configure modal options
			ModalDialog.getDefaultOption().setOpacity(0f).setAnimationOnClose(false).getBorderOption()
					.setBorderWidth(0.5f).setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

			// Show modal with proper size and validation
			ModalDialog.showModal(parent, new SimpleModalBorder(panel, "Reschedule Missed Appointment",
					new SimpleModalBorder.Option[] { 
							new SimpleModalBorder.Option("Reschedule", SimpleModalBorder.YES_OPTION),
							new SimpleModalBorder.Option("Cancel", SimpleModalBorder.CANCEL_OPTION) 
					},
					(controller, action) -> {
						if (action == SimpleModalBorder.YES_OPTION) {
							try {
								if (panel.saveRescheduledAppointment()) {
									controller.close();
									if (onSuccess != null) {
										onSuccess.run();
									}
								} else {
									controller.consume(); // don't close the modal if the save is not successful
								}
							} catch (SQLException e) {
								logger.error("Error rescheduling appointment: " + e.getMessage(), e);
								JOptionPane.showMessageDialog(parent, "Error rescheduling appointment: " + e.getMessage(),
										"Database Error", JOptionPane.ERROR_MESSAGE);
							}
						} else if (action == SimpleModalBorder.CANCEL_OPTION
								|| action == SimpleModalBorder.CLOSE_OPTION) {
							controller.close();
						}
					}), "resched_appointment");

			// Set modal size
			ModalDialog.getDefaultOption().getLayoutOption().setSize(width, height);

		} catch (Exception e) {
			logger.error("Error showing reschedule dialog: " + e.getMessage(), e);
			JOptionPane.showMessageDialog(parent, "Error showing reschedule dialog: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
}
