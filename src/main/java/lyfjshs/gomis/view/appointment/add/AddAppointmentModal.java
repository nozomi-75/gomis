package lyfjshs.gomis.view.appointment.add;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
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

    public void showModal(JPanel parent, AddAppointmentPanel addAppointPanel, AppointmentDAO appointmentDAO) {
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
                                JOptionPane.showMessageDialog(parent, "Appointment created successfully!",
                                        "Success", JOptionPane.INFORMATION_MESSAGE);
                                controller.close();
                            } else {
                                JOptionPane.showMessageDialog(parent, "Failed to create appointment. Please try again.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IllegalArgumentException e) {
                            // Validation errors are already shown in validateInputs()
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    } else if (action == SimpleModalBorder.NO_OPTION || action == SimpleModalBorder.CLOSE_OPTION
                            || action == SimpleModalBorder.CANCEL_OPTION) {
                        // Ask for confirmation if participants are already added
                        if (addAppointPanel.getParticipantIds() != null
                                && !addAppointPanel.getParticipantIds().isEmpty()) {
                            int confirm = JOptionPane.showConfirmDialog(parent,
                                    "Are you sure you want to cancel? "
                                            + "The participants you have added will be lost.",
                                    "Cancel Appointment", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.YES_OPTION) {
                                controller.close();
                            }
                        } else {
                            controller.close();
                        }
                    }
                }), "input");
        ModalDialog.getDefaultOption().getLayoutOption().setSize(700, 700);
    }
}
