package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.Modal;

public class ViolationFullData extends Modal {
    private JTextField participantNameField;
    private JTextField violationTypeField;
    private JTextField statusField;
    private JTextArea descriptionArea;
    private JTextField dateField;
    private JTextField anecdotalRecordField;
    private JTextField reinforcementField;
    private JTable participantsTable;
    private DefaultTableModel participantsTableModel;
    private JTextArea sessionDetailsArea; // New field for session details
    private Connection connection;

    public ViolationFullData(ViolationRecord violation, Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout(0, 0));
        initComponents();
        populateData(violation);
        
        
        JPanel mainPanel = new JPanel(new MigLayout("", "[grow]", "[grow][]"));
        JScrollPane scroll = new JScrollPane(mainPanel);

        mainPanel.add(createViolationDetailsPanel(), "cell 0 0, grow");
        mainPanel.add(createDescriptionPanel(), "cell 0 1, grow");

        add(scroll);
    }

    private void initComponents() {
        participantNameField = new JTextField();
        violationTypeField = new JTextField();
        statusField = new JTextField();
        descriptionArea = new JTextArea(5, 20);
        dateField = new JTextField();
        anecdotalRecordField = new JTextField();
        reinforcementField = new JTextField();
        sessionDetailsArea = new JTextArea(5, 20); // Initialize session details area
        sessionDetailsArea.setEditable(false);
        sessionDetailsArea.setLineWrap(true);
        sessionDetailsArea.setWrapStyleWord(true);

        participantsTableModel = new DefaultTableModel(new Object[]{"Full Name", "Sex", "Contact Number"}, 0);
        participantsTable = new JTable(participantsTableModel);
        participantsTable.setEnabled(false); // Make the table read-only

        // Make fields read-only
        participantNameField.setEditable(false);
        violationTypeField.setEditable(false);
        statusField.setEditable(false);
        descriptionArea.setEditable(false);
        dateField.setEditable(false);
        anecdotalRecordField.setEditable(false);
        reinforcementField.setEditable(false);
    }

    private void populateData(ViolationRecord violation) {
        ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
        Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
        if (participant != null) {
            participantNameField.setText(String.format("%s %s", participant.getParticipantFirstName(), participant.getParticipantLastName()));

            // If the participant is a student, fetch additional details
            if (participant.getStudentUid() != null) {
                StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
                try {
                    Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
                    if (student != null) {
                        participantsTableModel.addRow(new Object[]{
                            String.format("%s %s", student.getStudentFirstname(), student.getStudentLastname()),
                            student.getStudentSex(),
                            student.getContact() != null ? student.getContact().getContactNumber() : "N/A"
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching student details: " + e.getMessage());
                }
            } else {
                participantsTableModel.addRow(new Object[]{
                    String.format("%s %s", participant.getParticipantFirstName(), participant.getParticipantLastName()),
                    participant.getParticipantType(),
                    participant.getContactNumber()
                });
            }
        } else {
            participantNameField.setText("N/A");
        }

        violationTypeField.setText(violation.getViolationType());
        statusField.setText(violation.getStatus());
        descriptionArea.setText(violation.getViolationDescription());
        dateField.setText(violation.getUpdatedAt() != null ? violation.getUpdatedAt().toString() : "");
        anecdotalRecordField.setText(violation.getAnecdotalRecord());
        reinforcementField.setText(violation.getReinforcement());

        // Fetch and display session details related to the violation
        fetchSessionDetails(violation.getViolationId());
    }

    private void fetchSessionDetails(int violationId) {
        String sql = "SELECT * FROM SESSIONS WHERE VIOLATION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, violationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String sessionDetails = String.format(
                        "Session ID: %d\nAppointment Type: %s\nConsultation Type: %s\nSession Date: %s\nNotes: %s\nStatus: %s\n",
                        rs.getInt("SESSION_ID"),
                        rs.getString("APPOINTMENT_TYPE"),
                        rs.getString("CONSULTATION_TYPE"),
                        rs.getTimestamp("SESSION_DATE_TIME"),
                        rs.getString("SESSION_NOTES"),
                        rs.getString("SESSION_STATUS")
                    );
                    sessionDetailsArea.setText(sessionDetails);
                } else {
                    sessionDetailsArea.setText("No related session found for this violation.");
                }
            }
        } catch (Exception e) {
            sessionDetailsArea.setText("Error fetching session details: " + e.getMessage());
        }
    }

    private JPanel createViolationDetailsPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][][][][][][200px][]"));
        panel.setBorder(new TitledBorder(null, "Violation Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        panel.add(new JLabel("Name:"));
        panel.add(participantNameField, "growx");

        panel.add(new JLabel("Violation Type:"));
        panel.add(violationTypeField, "growx");

        panel.add(new JLabel("Status:"));
        panel.add(statusField, "growx");

        panel.add(new JLabel("Date:"));
        panel.add(dateField, "growx");

        panel.add(new JLabel("Anecdotal Record:"));
        panel.add(anecdotalRecordField, "growx");

        panel.add(new JLabel("Reinforcement:"));
        panel.add(reinforcementField, "growx");

        panel.add(new JLabel("Participants:"));
        JScrollPane participantsScrollPane = new JScrollPane(participantsTable);
        panel.add(participantsScrollPane, "span, growx");

        panel.add(new JLabel("Session Details:"));
        JScrollPane sessionScrollPane = new JScrollPane(sessionDetailsArea);
        panel.add(sessionScrollPane, "span, growx");

        return panel;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow,fill]", "[][]"));
        panel.setBorder(new TitledBorder(null, "Description", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);

        panel.add(scrollPane, "grow");

        return panel;
    }
}