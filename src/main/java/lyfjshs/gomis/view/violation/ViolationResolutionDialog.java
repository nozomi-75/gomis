/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Violation;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class ViolationResolutionDialog extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(ViolationResolutionDialog.class);
    private final Violation violation;
    private final ViolationDAO violationDAO;
    private JTextArea resolutionNotesArea;
    private boolean resolutionConfirmed = false;
    private JComboBox<String> statusCombo;
    private JTextField dateTimeField;

    public ViolationResolutionDialog(Connection conn, Violation violation) {
        this.violation = violation;
        this.violationDAO = new ViolationDAO(conn);
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Main Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new MigLayout("", "[grow,fill][grow,fill]", "[][][][][][]"));

        // Violation Details Section
        JLabel label = new JLabel("Violation Type:");
        contentPanel.add(label, "cell 0 0,alignx left,aligny top");
        JLabel label_1 = new JLabel(violation.getViolationType());
        contentPanel.add(label_1, "cell 1 0,growx,aligny top");
        JLabel label_2 = new JLabel("Description:");
        contentPanel.add(label_2, "cell 0 1,alignx left,aligny top");
        JLabel label_3 = new JLabel(violation.getViolationDescription());
        contentPanel.add(label_3, "cell 1 1,growx,aligny top");
        JLabel label_4 = new JLabel("Current Status:");
        contentPanel.add(label_4, "cell 0 2,alignx left,aligny top");
        JLabel label_5 = new JLabel(violation.getStatus());
        contentPanel.add(label_5, "cell 1 2,growx,aligny top");

        // Resolution Status
        JLabel label_6 = new JLabel("Resolution Status:");
        contentPanel.add(label_6, "cell 0 3,alignx left,aligny center");
        statusCombo = new JComboBox<>(new String[] {"Resolved", "Dismissed", "Escalated"});
        statusCombo.setToolTipText("Select the resolution status for this violation");
        contentPanel.add(statusCombo, "cell 1 3,growx,aligny top");

        // Date/Time
        JLabel label_7 = new JLabel("Resolution Date/Time:");
        contentPanel.add(label_7, "cell 0 4,alignx left,aligny center");
        dateTimeField = new JTextField(20);
        dateTimeField.setEditable(false);
        dateTimeField.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        contentPanel.add(dateTimeField, "cell 1 4,growx,aligny top");

        // Resolution Notes
        JLabel label_8 = new JLabel("Resolution Notes:");
        contentPanel.add(label_8, "cell 0 5,alignx left,aligny top");
        resolutionNotesArea = new JTextArea(6, 30);
        resolutionNotesArea.setLineWrap(true);
        resolutionNotesArea.setWrapStyleWord(true);
        resolutionNotesArea.setBorder(BorderFactory.createLineBorder(new java.awt.Color(180,180,180)));
        resolutionNotesArea.setToolTipText("Describe how this violation was resolved, or any important notes.");
        resolutionNotesArea.setText(""); // Placeholder (Swing doesn't support real placeholder, but can set initial text)
        contentPanel.add(resolutionNotesArea, "cell 1 5,alignx left,aligny top");

        add(contentPanel, BorderLayout.CENTER);
    }

    private void handleResolution() {
        try {
            String resolutionNotes = resolutionNotesArea.getText().trim();
            String status = (String) statusCombo.getSelectedItem();
            if (resolutionNotes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter resolution notes.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Optionally, update the violation status and notes in the database
            if (violationDAO.updateViolationStatus(violation.getViolationId(), status.toUpperCase(), resolutionNotes)) {
                resolutionConfirmed = true;
                JOptionPane.showMessageDialog(this, "Violation resolved successfully", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to resolve violation.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            logger.error("Error resolving violation", e);
            JOptionPane.showMessageDialog(this, "Error resolving violation: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isResolutionConfirmed() {
        return resolutionConfirmed;
    }

    public static void showModal(Component parent, Connection conn, Violation violation, Runnable onResolution) {
        ViolationResolutionDialog panel = new ViolationResolutionDialog(conn, violation);

        String modalId = "resolve_violation_modal";
        if (ModalDialog.isIdExist(modalId)) {
            ModalDialog.closeModal(modalId);
        }

        // Set modal options similar to ViewViolationDetails and AddAppointmentModal
        ModalDialog.getDefaultOption()
            .setOpacity(0.3f)
            .setAnimationOnClose(false)
            .getBorderOption()
            .setBorderWidth(0f)
            .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

        SimpleModalBorder modalBorder = new SimpleModalBorder(
            panel,
            "Resolve Violation",
            new SimpleModalBorder.Option[] {
                new SimpleModalBorder.Option("Resolve", SimpleModalBorder.YES_OPTION),
                new SimpleModalBorder.Option("Cancel", SimpleModalBorder.CANCEL_OPTION)
            },
            (controller, action) -> {
                if (action == SimpleModalBorder.YES_OPTION) {
                    panel.handleResolution();
                    if (panel.isResolutionConfirmed()) {
                        controller.close();
                        if (onResolution != null) {
                            onResolution.run();
                        }
                    } else {
                        controller.consume(); // dont close the modal if the save is not successful
                    }
                } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );

        ModalDialog.showModal(parent, modalBorder, modalId);
        // Set modal size dynamically based on screen size (like ViewViolationDetails)
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(540, screenSize.width - 100);
        int height = Math.min(400, screenSize.height - 100);
        ModalDialog.getDefaultOption().getLayoutOption().setSize(width, height);
    }
}