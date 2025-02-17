package lyfjshs.gomis.view.violation;

import javax.swing.*;

import lyfjshs.gomis.Database.model.Violation;

import java.awt.*;
import java.awt.event.*;

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

    public ViolationDetails(JFrame parent, Violation violation) {
        super(parent, "Violation Details", true);
        this.violation = violation;
        initComponents();
        loadViolationData();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
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
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // First column
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Participant Type:"), gbc);
        
        gbc.gridy++;
        mainPanel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridy++;
        mainPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridy++;
        mainPanel.add(new JLabel("Violation Type:"), gbc);
        
        gbc.gridy++;
        mainPanel.add(new JLabel("Reinforcement:"), gbc);
        
        // Second column
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        mainPanel.add(cmbParticipantType, gbc);
        
        gbc.gridy++;
        mainPanel.add(txtFIRST_NAME, gbc);
        
        gbc.gridy++;
        mainPanel.add(txtEmail, gbc);
        
        gbc.gridy++;
        mainPanel.add(cmbViolationType, gbc);
        
        gbc.gridy++;
        mainPanel.add(cmbReinforcement, gbc);
        
        // Third column
        gbc.gridx = 2;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("LRN:"), gbc);
        
        gbc.gridy++;
        mainPanel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridy++;
        mainPanel.add(new JLabel("Contact:"), gbc);
        
        gbc.gridy++;
        mainPanel.add(new JLabel("Status:"), gbc);
        
        // Fourth column
        gbc.gridx = 3;
        gbc.gridy = 0;
        mainPanel.add(txtLRN, gbc);
        
        gbc.gridy++;
        mainPanel.add(txtLAST_NAME, gbc);
        
        gbc.gridy++;
        mainPanel.add(txtContact, gbc);
        
        gbc.gridy++;
        mainPanel.add(cmbStatus, gbc);
        
        // Description area (spans all columns)
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 4;
        mainPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridy++;
        JScrollPane scrollPane = new JScrollPane(txtDescription);
        mainPanel.add(scrollPane, gbc);
        
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
        setResizable(false);
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
            txtLRN.setText(violation.getStudentLRN());
            txtFIRST_NAME.setText(violation.getFIRST_NAME());
            txtLAST_NAME.setText(violation.getLAST_NAME());
            txtEmail.setText(violation.getEmail());
            txtContact.setText(violation.getContact());
            cmbParticipantType.setSelectedItem(violation.getParticipantType());
            cmbViolationType.setSelectedItem(violation.getViolationType());
            cmbStatus.setSelectedItem(violation.getStatus());
            cmbReinforcement.setSelectedItem(violation.getReinforcement());
            // txtDescription.setText(violation.getDescription());
        }
    }
    
    private void saveViolationData() {
        if (violation != null) {
            violation.setStudentLRN(txtLRN.getText());
            violation.setFIRST_NAME(txtFIRST_NAME.getText());
            violation.setLAST_NAME(txtLAST_NAME.getText());
            violation.setEmail(txtEmail.getText());
            violation.setContact(txtContact.getText());
            violation.setParticipantType((String) cmbParticipantType.getSelectedItem());
            violation.setViolationType((String) cmbViolationType.getSelectedItem());
            violation.setStatus((String) cmbStatus.getSelectedItem());
            violation.setReinforcement((String) cmbReinforcement.getSelectedItem());
            // violation.setDescription(txtDescription.getText());
        }
    }
} 