package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class ViolationFullData extends Form {
    private JTextField violationIdField;
    private JTextField participantIdField;
    private JTextField participantNameField;
    private JTextField violationTypeField;
    private JTextField statusField;
    private JTextArea descriptionArea;
    private JTextField dateField;
    private JTextField anecdotalRecordField;
    private JTextField reinforcementField;
    private Connection connection;

    public ViolationFullData(ViolationRecord violation, Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout(0, 0));
        initComponents();
        populateData(violation);
        
        // Create main panel with scroll
        JPanel mainPanel = new JPanel(new MigLayout("", "[grow]", "[grow][]"));
        JScrollPane scroll = new JScrollPane(mainPanel);
        
        mainPanel.add(createViolationDetailsPanel(), "cell 0 0, grow");
        mainPanel.add(createDescriptionPanel(), "cell 0 1, grow");
        
        add(scroll);
    }

    private void initComponents() {
        violationIdField = new JTextField();
        participantIdField = new JTextField();
        participantNameField = new JTextField();
        violationTypeField = new JTextField();
        statusField = new JTextField();
        descriptionArea = new JTextArea(5, 20);
        dateField = new JTextField();
        anecdotalRecordField = new JTextField();
        reinforcementField = new JTextField();
        
        // Make fields read-only
        violationIdField.setEditable(false);
        participantIdField.setEditable(false);
        participantNameField.setEditable(false);
        violationTypeField.setEditable(false);
        statusField.setEditable(false);
        descriptionArea.setEditable(false);
        dateField.setEditable(false);
        anecdotalRecordField.setEditable(false);
        reinforcementField.setEditable(false);
    }

    private void populateData(ViolationRecord violation) {
        violationIdField.setText(String.valueOf(violation.getViolationId()));
        participantIdField.setText(String.valueOf(violation.getParticipantId()));
        
        try {
            ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
            Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
            if (participant != null) {
                participantNameField.setText(String.format("%s %s", participant.getParticipantFirstName(), participant.getParticipantLastName()));
            } else {
                participantNameField.setText("N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            participantNameField.setText("Error fetching participant data");
        }

        violationTypeField.setText(violation.getViolationType());
        statusField.setText(violation.getStatus());
        descriptionArea.setText(violation.getViolationDescription());
        dateField.setText(violation.getUpdatedAt() != null ? 
            violation.getUpdatedAt().toString() : "");
        anecdotalRecordField.setText(violation.getAnecdotalRecord());
        reinforcementField.setText(violation.getReinforcement());
    }

    private JPanel createViolationDetailsPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][][][][][][][]"));
        panel.setBorder(new TitledBorder(null, "Violation Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        panel.add(new JLabel("Violation ID:"));
        panel.add(violationIdField, "growx");

        panel.add(new JLabel("Participant ID:"));
        panel.add(participantIdField, "growx");

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