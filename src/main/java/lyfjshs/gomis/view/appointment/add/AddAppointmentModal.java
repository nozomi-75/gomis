package lyfjshs.gomis.view.appointment.add;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.view.appointment.AppointmentCalendar;
import lyfjshs.gomis.view.appointment.AppointmentDailyOverview;
import lyfjshs.gomis.view.appointment.AppointmentManagement;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.component.SimpleModalBorder.Option;

public class AddAppointmentModal {
    private static AddAppointmentModal instance; // Singleton instance

    private AddAppointmentModal() {
        // Private constructor to prevent instantiation
    }

    public static AddAppointmentModal getInstance() {
        if (instance == null) {
            instance = new AddAppointmentModal();
        }
        return instance;
    }

    public void showModal(Connection conn, JPanel parent, AddAppointmentPanel addAppointPanel, AppointmentDAO appointmentDAO, Number width, Number height, Runnable onSuccess) {
        Option[] modalBorder = new SimpleModalBorder.Option[] {
                new SimpleModalBorder.Option("Add Appointment", SimpleModalBorder.YES_OPTION),
                new SimpleModalBorder.Option("Cancel", SimpleModalBorder.NO_OPTION) };

        // Create the modal dialog
        ModalDialog.showModal(parent, new SimpleModalBorder(addAppointPanel, "Add Appointment", modalBorder,
                (controller, action) -> {
                    if (action == SimpleModalBorder.YES_OPTION) {
                        System.out.println("Add Appointment button clicked.");

                        try {
                            // Validate and save the appointment
                            if (addAppointPanel.saveAppointment()) {
                                // Close the modal first
                                controller.close();
                                
                                // Call success callback
                                if (onSuccess != null) {
                                    onSuccess.run();
                                }
                                
                                // Refresh the views after successful creation
                                if (parent instanceof AppointmentCalendar) {
                                    AppointmentCalendar calendar = (AppointmentCalendar) parent;
                                    calendar.updateCalendar();
                                    if (calendar.getParent() instanceof AppointmentManagement) {
                                        ((AppointmentManagement) calendar.getParent()).refreshViews();
                                    }
                                } else if (parent instanceof AppointmentManagement) {
                                    ((AppointmentManagement) parent).refreshViews();
                                } else if (parent instanceof AppointmentDailyOverview) {
                                    ((AppointmentDailyOverview) parent).updateAppointmentsDisplay();
                                }
                            }
                        } catch (Exception e) {
                            // Show error message
                            JOptionPane.showMessageDialog(parent, "Error creating appointment: " + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    } else if (action == SimpleModalBorder.NO_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                        // Ask for confirmation and cleanup participants if needed
                        List<Integer> participants = addAppointPanel.getParticipantIds();
                        if (participants != null && !participants.isEmpty()) {
                            int confirm = JOptionPane.showConfirmDialog(parent,
                                    "Are you sure you want to cancel? "
                                            + "All added participants will be removed.",
                                    "Cancel Appointment", 
                                    JOptionPane.YES_NO_OPTION, 
                                    JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.YES_OPTION) {
                                // Cleanup participants
                                try {
                                    ParticipantsDAO participantsDAO = new ParticipantsDAO(conn);
                                    for (Integer participantId : participants) {
                                        participantsDAO.deleteParticipant(participantId);
                                    }
                                    controller.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(parent,
                                        "Error cleaning up participants: " + e.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else {
                            controller.close();
                        }
                    }
                }), "add_appointment_modal");
        ModalDialog.getDefaultOption().getLayoutOption().setSize(width, height);
    }
}
