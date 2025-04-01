package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatLightLaf;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.Modal;

public class ViolationFullData extends Modal {
    private JTextField participantNameField;
    private JTextField violationTypeField;
    private JTextField statusField;
    private JTextArea descriptionArea;
    private JTextField dateField;
    private JTextField sessionSummaryField;
    private JTable participantsTable;
    private DefaultTableModel participantsTableModel;
    private JTextArea sessionDetailsArea; // New field for session details
    private Connection connection;

    public static void showViolationDetails(JFrame parent, Violation violation, Connection connection) {
        ViolationFullData violationFullData = new ViolationFullData(violation, connection);
        
        ModalDialog.getDefaultOption()
            .setOpacity(0f)
            .setAnimationOnClose(true)
            .getBorderOption()
            .setBorderWidth(0.5f)
            .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

        ModalDialog.showModal(parent, violationFullData, "violationDetails");
        ModalDialog.getDefaultOption().getLayoutOption().setSize(800, 600);
    }

    public ViolationFullData(Violation violation, Connection connection) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.connection = connection;
        setLayout(new BorderLayout());
        initComponents();
        populateData(violation);
        
        JPanel mainPanel = new JPanel(new MigLayout("wrap, insets 30", "[grow]", "[]"));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Violation Information Section
        addSectionHeader(mainPanel, "Violation Information");
        JPanel violationGrid = createGridPanel();
        addLabelValuePair(violationGrid, "Name:", participantNameField);
        addLabelValuePair(violationGrid, "Violation Type:", violationTypeField);
        addLabelValuePair(violationGrid, "Status:", statusField);
        addLabelValuePair(violationGrid, "Date:", dateField);
        mainPanel.add(violationGrid, "span, wrap 15");

        // Description Section
        addSectionHeader(mainPanel, "Violation Description");
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(new JScrollPane(descriptionArea), "span, growx, h 100!, wrap 15");

        // Session Summary Section
        addSectionHeader(mainPanel, "Session Summary");
        sessionSummaryField.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(sessionSummaryField, "span, growx, wrap 15");

        // Participants Section
        addSectionHeader(mainPanel, "Participants");
        participantsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        participantsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(new JScrollPane(participantsTable), "span, growx, h 100!, wrap 15");

        // Session Details Section
        addSectionHeader(mainPanel, "Session Details");
        sessionDetailsArea.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(new JScrollPane(sessionDetailsArea), "span, growx, h 100!");

        add(new JScrollPane(mainPanel));
    }

    private void addSectionHeader(JPanel panel, String title) {
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 16));
        sectionTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        panel.add(sectionTitle, "spanx, growx, wrap 15");
    }

    private JPanel createGridPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[right]10[left,grow]", "[]5[]"));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private void addLabelValuePair(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(85, 85, 85));
        field.setEditable(false);
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBackground(Color.WHITE);
        panel.add(label);
        panel.add(field, "growx");
    }

    private void initComponents() {
        participantNameField = new JTextField();
        violationTypeField = new JTextField();
        statusField = new JTextField();
        descriptionArea = new JTextArea(5, 20);
        dateField = new JTextField();
        sessionSummaryField = new JTextField();
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
        sessionSummaryField.setEditable(false);
    }

    private void populateData(Violation violation) {
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
        sessionSummaryField.setText(violation.getSessionSummary());

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
}