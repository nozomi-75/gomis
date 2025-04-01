package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import net.miginfocom.swing.MigLayout;

public class ViolationDetails extends JDialog {
    private JTextField txtLRN;
    private JTextField txtFIRST_NAME;
    private JTextField txtLAST_NAME;
    private JTextField txtEmail;
    private JTextField txtContact;
    private JComboBox<String> cmbParticipantType;
    private JComboBox<String> cmbViolationType;
    private JComboBox<String> cmbStatus;
    private JComboBox<String> cmbReinforcement;
    private JTextArea txtDescription;
    private JButton btnEdit;
    private JButton btnClose;
    
    private Violation violation;
    private boolean isEditing = false;
    private StudentsDataDAO studentsDataDAO;
    private ParticipantsDAO participantsDAO;

    public ViolationDetails(JFrame parent, Violation violation, StudentsDataDAO studentsDataDAO, ParticipantsDAO participantsDAO) {
        super(parent, "Violation Details", true);
        this.violation = violation;
        this.studentsDataDAO = studentsDataDAO;
        this.participantsDAO = participantsDAO;
        initComponents();
        loadViolationData();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new MigLayout("wrap 4", "[grow][grow][grow][grow]", "[]10[]10[]10[]10[]10[]10[]"));
        
        // Initialize components
        cmbParticipantType = new JComboBox<>(new String[]{"Student", "Teacher", "Staff"});
        txtLRN = new JTextField(20);
        txtFIRST_NAME = new JTextField(20);
        txtLAST_NAME = new JTextField(20);
        txtEmail = new JTextField(20);
        txtContact = new JTextField(20);
        cmbViolationType = new JComboBox<>(new String[]{"Minor", "Major", "Critical"});
        cmbStatus = new JComboBox<>(new String[]{"Pending", "Resolved"});
        cmbReinforcement = new JComboBox<>(new String[]{"Warning", "Suspension", "Expulsion"});
        txtDescription = new JTextArea(5, 20);
        
        // Make all components non-editable initially
        setFieldsEditable(false);
        
        // Add components to panel
        mainPanel.add(new JLabel("Participant Type:"), "cell 0 0");
        mainPanel.add(cmbParticipantType, "cell 1 0");
        mainPanel.add(new JLabel("LRN:"), "cell 2 0");
        mainPanel.add(txtLRN, "cell 3 0");
        
        mainPanel.add(new JLabel("First Name:"), "cell 0 1");
        mainPanel.add(txtFIRST_NAME, "cell 1 1");
        mainPanel.add(new JLabel("Last Name:"), "cell 2 1");
        mainPanel.add(txtLAST_NAME, "cell 3 1");
        
        mainPanel.add(new JLabel("Email:"), "cell 0 2");
        mainPanel.add(txtEmail, "cell 1 2");
        mainPanel.add(new JLabel("Contact:"), "cell 2 2");
        mainPanel.add(txtContact, "cell 3 2");
        
        mainPanel.add(new JLabel("Violation Type:"), "cell 0 3");
        mainPanel.add(cmbViolationType, "cell 1 3");
        mainPanel.add(new JLabel("Status:"), "cell 2 3");
        mainPanel.add(cmbStatus, "cell 3 3");
        
        mainPanel.add(new JLabel("Reinforcement:"), "cell 0 4");
        mainPanel.add(cmbReinforcement, "cell 1 4");
        
        mainPanel.add(new JLabel("Description:"), "cell 0 5");
        JScrollPane scrollPane = new JScrollPane(txtDescription);
        mainPanel.add(scrollPane, "cell 0 6 4 1, growx");
    
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEdit = new JButton("Edit");
        btnClose = new JButton("Close");
        
        btnEdit.addActionListener(e -> toggleEdit());
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnClose);
        
        // Add panels to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set dialog properties
        setSize(800, 600);
    }
    
    private void setFieldsEditable(boolean editable) {
        txtLRN.setEditable(editable);
        txtFIRST_NAME.setEditable(editable);
        txtLAST_NAME.setEditable(editable);
        txtEmail.setEditable(editable);
        txtContact.setEditable(editable);
        cmbParticipantType.setEnabled(editable);
        cmbViolationType.setEnabled(editable);
        cmbStatus.setEnabled(editable);
        cmbReinforcement.setEnabled(editable);
        txtDescription.setEditable(editable);
    }
    
    private void toggleEdit() {
        isEditing = !isEditing;
        setFieldsEditable(isEditing);
        btnEdit.setText(isEditing ? "Save" : "Edit");
        
        if (!isEditing) {
            // Save changes
            saveViolationData();
        }
    }
    
    private void loadViolationData() {
        if (violation != null) {
            try {
                Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
                if (participant != null) {
                    txtFIRST_NAME.setText(participant.getParticipantFirstName());
                    txtLAST_NAME.setText(participant.getParticipantLastName());
                    txtContact.setText(participant.getContactNumber());
                    cmbParticipantType.setSelectedItem(participant.getParticipantType());

                    if ("Student".equals(participant.getParticipantType())) {
                        Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
                        if (student != null) {
                            txtLRN.setText(student.getStudentLrn());
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            cmbViolationType.setSelectedItem(violation.getViolationType());
            cmbStatus.setSelectedItem(violation.getStatus());
            cmbReinforcement.setSelectedItem(violation.getReinforcement());
            txtDescription.setText(violation.getViolationDescription());
        }
    }
    
    private void saveViolationData() {
        if (violation != null) {
            violation.setParticipantId(getParticipantIdFromLrn(txtLRN.getText()));
            violation.setViolationType((String) cmbViolationType.getSelectedItem());
            violation.setStatus((String) cmbStatus.getSelectedItem());
            violation.setReinforcement((String) cmbReinforcement.getSelectedItem());
            violation.setViolationDescription(txtDescription.getText());
        }
    }

    // Method to get participantId from LRN
    private int getParticipantIdFromLrn(String lrn) {
        try {
            Student student = studentsDataDAO.getStudentDataByLrn(lrn);
            if (student != null) {
                Participants participant = participantsDAO.getParticipantById(student.getStudentUid());
                if (participant != null) {
                    return participant.getParticipantId();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if participant not found
    }
}