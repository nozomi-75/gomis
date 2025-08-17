/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.appointment;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;

public class AppointmentsListModal {
    private static final Logger logger = LogManager.getLogger(AppointmentsListModal.class);
    private static AppointmentsListModal instance;
    private AppointmentDAO appointmentDAO;

    private AppointmentsListModal() {
        // Private constructor to enforce singleton pattern
    }

    public static AppointmentsListModal getInstance() {
        if (instance == null) {
            instance = new AppointmentsListModal();
        }
        return instance;
    }

    public void showModal(Connection connection, Component parent, LocalDate date, int width, int height, Consumer<Boolean> refreshCallback) {
        try {
            if (this.appointmentDAO == null) {
                this.appointmentDAO = new AppointmentDAO(connection);
            }
            List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(date);
            
            // Configure modal options
            Option listModalOption = new Option();
            listModalOption.setOpacity(0f)
                .setAnimationOnClose(false)
                .getBorderOption()
                .setBorderWidth(0.5f)
                .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

            // Create the appointments list view
            AppointmentsListView listView = new AppointmentsListView(connection);
            listView.setDate(date, appointments);
            
            String title = "Appointments for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            SimpleModalBorder.Option[] options;
            
            if (appointments.isEmpty()) {
                options = new SimpleModalBorder.Option[] {
                    new SimpleModalBorder.Option("No appointments scheduled. Create one?", SimpleModalBorder.YES_OPTION),
                    new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
                };
            } else {
                options = new SimpleModalBorder.Option[] {
                    new SimpleModalBorder.Option("Create New Appointment", SimpleModalBorder.YES_OPTION),
                    new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
                };
            }
            
            // Show modal with proper size
            ModalDialog.showModal(parent, 
                new SimpleModalBorder(listView, title,
                    options,
                    (controller, action) -> {
                        if (action == SimpleModalBorder.YES_OPTION) {
                            // Close the current modal first
                            controller.close();
                            // Then show the AddAppointmentModal
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                AddAppointmentModal.getInstance().showModal(
                                    connection,
                                    parent,
                                    new AddAppointmentPanel(null, appointmentDAO, connection),
                                    appointmentDAO,
                                    800,
                                    600,
                                    () -> {
                                        if (refreshCallback != null) {
                                            refreshCallback.accept(true);
                                        }
                                    }
                                );
                            });
                        } else if (action == SimpleModalBorder.CLOSE_OPTION) {
                            controller.close();
                        }
                    }), 
                listModalOption, // Pass the specific option for this modal
                "appointments_list");

            // Set modal size
            listModalOption.getLayoutOption().setSize(width, height);

        } catch (SQLException e) {
            logger.error("Error loading appointments: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(parent, 
                "Error loading appointments: " + e.getMessage(), 
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error showing appointments list: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(parent, 
                "Error showing appointments list: " + e.getMessage(), 
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 