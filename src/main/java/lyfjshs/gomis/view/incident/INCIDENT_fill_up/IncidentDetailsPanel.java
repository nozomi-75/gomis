/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.incident.INCIDENT_fill_up;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.ModalDialog;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class IncidentDetailsPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(IncidentDetailsPanel.class);
    private JTextField reportedByField;
    private JTextField gradeSectionField;
    private JFormattedTextField dateField;
    private JFormattedTextField timeField;
    private JComboBox<String> statusCombo;
    private JButton searchReporterBtn;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Connection connection;

    public IncidentDetailsPanel(Connection connection) {
        this.connection = connection;
        setOpaque(false);
        setLayout(new MigLayout("", "[][grow][][grow]", "[][][]"));
        initComponents();
        searchReporterBtn.addActionListener(e -> openStudentReporterSearchUI());
    }

    private void initComponents() {
        JLabel lblReportedBy = new JLabel("Reported By:");
        add(lblReportedBy, "cell 0 0,alignx left");
        reportedByField = new JTextField();
        add(reportedByField, "cell 1 0,growx");
        searchReporterBtn = new JButton("Search Reporter Student");
        add(searchReporterBtn, "cell 2 0 2 1");

        JLabel lblGradeSection = new JLabel("Grade & Section:");
        add(lblGradeSection, "cell 0 1,alignx left");
        gradeSectionField = new JTextField();
        gradeSectionField.setEditable(false);
        add(gradeSectionField, "cell 1 1,growx");

        JLabel lblDate = new JLabel("Date:");
        add(lblDate, "cell 2 1,alignx left");
        dateField = new JFormattedTextField();
        datePicker = new DatePicker();
        datePicker.setSelectedDate(LocalDate.now());
        datePicker.setEditor(dateField);
        add(dateField, "cell 3 1,growx");

        JLabel lblTime = new JLabel("Time:");
        add(lblTime, "cell 0 2,alignx left");
        timeField = new JFormattedTextField();
        timePicker = new TimePicker();
        timePicker.setSelectedTime(LocalTime.now());
        timePicker.setEditor(timeField);
        add(timeField, "cell 1 2,growx");

        JLabel lblStatus = new JLabel("Status:");
        add(lblStatus, "cell 2 2,alignx left");
        statusCombo = new JComboBox<>(new String[]{"Pending", "Ongoing Investigation", "Resolved", "Dismissed"});
        add(statusCombo, "cell 3 2,growx");
    }

    private void openStudentReporterSearchUI() {
        if (ModalDialog.isIdExist("reporterSearch")) {
            return;
        }
        StudentSearchPanel searchPanel = new StudentSearchPanel(connection, "reporterSearch") {
            @Override
            protected void onStudentSelected(Student student) {
                setReportedBy(student.getStudentFirstname() + " " + student.getStudentLastname());
                if (student.getSchoolForm() != null) {
                    setGradeSection(student.getSchoolForm().getSF_SECTION());
                }
                ModalDialog.closeModal("reporterSearch");
            }
        };
        Option option = ModalDialog.createOption();
        option.setAnimationEnabled(true);
        option.getLayoutOption().setMargin(40, 10, 10, 10).setLocation(Location.CENTER, Location.TOP);
        ModalDialog.showModal(this, searchPanel, option, "reporterSearch");
    }

    // Getters and setters for data
    public String getReportedBy() { return reportedByField.getText(); }
    public void setReportedBy(String name) { reportedByField.setText(name); }
    public String getGradeSection() { return gradeSectionField.getText(); }
    public void setGradeSection(String section) { gradeSectionField.setText(section); }
    public LocalDate getDate() { return datePicker.getSelectedDate(); }
    public void setDate(LocalDate date) { datePicker.setSelectedDate(date); }
    public LocalTime getTime() { return timePicker.getSelectedTime(); }
    public void setTime(LocalTime time) { timePicker.setSelectedTime(time); }
    public String getStatus() { return (String) statusCombo.getSelectedItem(); }
    public void setStatus(String status) { statusCombo.setSelectedItem(status); }
    public JButton getSearchReporterBtn() { return searchReporterBtn; }

    public void clearFields() {
        reportedByField.setText("");
        gradeSectionField.setText("");
        datePicker.setSelectedDate(java.time.LocalDate.now());
        timePicker.setSelectedTime(java.time.LocalTime.now());
        statusCombo.setSelectedIndex(0);
    }

    public boolean isValidPanel() {
        return !getReportedBy().trim().isEmpty() && getDate() != null && getTime() != null && getStatus() != null && !getStatus().trim().isEmpty();
    }

    private void onFieldChange(String fieldName, Object value) {
        logger.info("Field '{}' changed to: {}", fieldName, value);
    }
    private boolean validateFields() {
        boolean valid = !getReportedBy().trim().isEmpty() && getDate() != null && getTime() != null && getStatus() != null && !getStatus().trim().isEmpty();
        if (!valid) {
            logger.warn("Validation failed in IncidentDetailsPanel");
        }
        return valid;
    }
} 