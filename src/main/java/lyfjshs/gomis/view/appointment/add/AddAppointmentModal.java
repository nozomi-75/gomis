/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.appointment.add;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class AddAppointmentModal {
    private static AddAppointmentModal instance;

    private AddAppointmentModal() {
        // Private constructor to enforce singleton pattern
    }

    public static AddAppointmentModal getInstance() {
        if (instance == null) {
            instance = new AddAppointmentModal();
        }
        return instance;
    }

    public void showModal(Connection connection, Component parent, AddAppointmentPanel panel,
            AppointmentDAO appointmentDAO, int width, int height, Runnable onSuccess) {
        try {
            // Configure modal options
            ModalDialog.getDefaultOption().setOpacity(0f).setAnimationOnClose(false).getBorderOption()
                    .setBorderWidth(0.5f).setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

            // Show modal with proper size and validation
            ModalDialog.showModal(parent, new SimpleModalBorder(panel, "Add Appointment",
                    new SimpleModalBorder.Option[] { new SimpleModalBorder.Option("Save", SimpleModalBorder.YES_OPTION),
                            new SimpleModalBorder.Option("Cancel", SimpleModalBorder.CANCEL_OPTION) },
                    (controller, action) -> {
                        if (action == SimpleModalBorder.YES_OPTION) {
                            try {
                                if (panel.saveAppointment()) {
                                    controller.close();
                                    if (onSuccess != null) {
                                        onSuccess.run();
                                    }
                                } else {
                                    controller.consume(); // dont close the modal if the save is not successful
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                JOptionPane.showMessageDialog(parent, "Error saving appointment: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else if (action == SimpleModalBorder.CANCEL_OPTION
                                || action == SimpleModalBorder.CLOSE_OPTION) {
                            controller.close();
                        }
                    }), "add_appointment");

            // Set modal size
            ModalDialog.getDefaultOption().getLayoutOption().setSize(width, height);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error showing appointment dialog: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
