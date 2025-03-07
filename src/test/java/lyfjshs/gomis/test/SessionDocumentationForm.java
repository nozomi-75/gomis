package lyfjshs.gomis.test;


 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
 
public class SessionDocumentationForm {
    private static DefaultTableModel tableModel; // Table model for participants table
    private static JTable participantsTable; // The participants table
    private static JPanel mainPanel; // Main panel to hold the form and the table
    
    // Store participant details for retrieval when viewing
    private static Map<Integer, Map<String, String>> participantDetails = new HashMap<>();
 
    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Session Documentation Form");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null); // Center the frame on screen
 
        // Create a main panel with a layout manager to organize components
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
 
        // Create and add header label
        JLabel headerLabel = new JLabel("Session Documentation Form", SwingConstants.CENTER);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(33, 97, 140));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setPreferredSize(new Dimension(900, 40));  // Set header size
        mainPanel.add(headerLabel, BorderLayout.NORTH);
 
        // Create the form panel (content part of the frame)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);  // Use absolute positioning within formPanel
 
        // Participant Section
        JLabel participantLabel = new JLabel("Participant");
        participantLabel.setBounds(20, 50, 100, 25);
        formPanel.add(participantLabel);
 
        String[] participantOptions = {"Student", "Non-Student"};
        JComboBox<String> participantDropdown = new JComboBox<>(participantOptions);
        participantDropdown.setBounds(120, 50, 150, 25);
        formPanel.add(participantDropdown);
 
        JButton searchButton = new JButton("Search Student");
        searchButton.setBounds(280, 50, 140, 25);
        formPanel.add(searchButton);
 
        // Non-Student Section
        JLabel firstNameLabel = new JLabel("First Name");
        firstNameLabel.setBounds(20, 90, 100, 25);
        formPanel.add(firstNameLabel);
 
        JTextField firstNameField = new JTextField();
        firstNameField.setBounds(120, 90, 150, 25);
        formPanel.add(firstNameField);
 
        JLabel lastNameLabel = new JLabel("Last Name");
        lastNameLabel.setBounds(280, 90, 100, 25);
        formPanel.add(lastNameLabel);
 
        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(370, 90, 150, 25);
        formPanel.add(lastNameField);
 
        JLabel contactLabel = new JLabel("Contact Number");
        contactLabel.setBounds(20, 130, 100, 25);
        formPanel.add(contactLabel);
 
        JTextField contactField = new JTextField();
        contactField.setBounds(120, 130, 150, 25);
        formPanel.add(contactField);
 
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(280, 130, 100, 25);
        formPanel.add(emailLabel);
 
        JTextField emailField = new JTextField();
        emailField.setBounds(370, 130, 150, 25);
        formPanel.add(emailField);
 
        JButton saveParticipantButton = new JButton("Save Participant");
        saveParticipantButton.setBounds(200, 170, 150, 25);
        formPanel.add(saveParticipantButton);
 
        // Right Side Inputs
        JLabel violationLabel = new JLabel("Violation");
        violationLabel.setBounds(550, 50, 100, 25);
        formPanel.add(violationLabel);
 
        JTextField violationField = new JTextField();
        violationField.setBounds(650, 50, 150, 25);
        formPanel.add(violationField);
 
        JLabel appointmentLabel = new JLabel("Appointment Type");
        appointmentLabel.setBounds(550, 90, 120, 25);
        formPanel.add(appointmentLabel);
 
        String[] appointmentOptions = {"Walk-in", "Scheduled"};
        JComboBox<String> appointmentDropdown = new JComboBox<>(appointmentOptions);
        appointmentDropdown.setBounds(680, 90, 120, 25);
        formPanel.add(appointmentDropdown);
 
        JLabel startTimeLabel = new JLabel("Start Session Time");
        startTimeLabel.setBounds(550, 130, 120, 25);
        formPanel.add(startTimeLabel);
 
        JTextField startTimeField = new JTextField();
        startTimeField.setBounds(680, 130, 80, 25);
        formPanel.add(startTimeField);
 
        JLabel endTimeLabel = new JLabel("End Session Time");
        endTimeLabel.setBounds(550, 170, 120, 25);
        formPanel.add(endTimeLabel);
 
        JTextField endTimeField = new JTextField();
        endTimeField.setBounds(680, 170, 80, 25);
        formPanel.add(endTimeField);
 
        JLabel consultationLabel = new JLabel("Consultation Type");
        consultationLabel.setBounds(550, 210, 120, 25);
        formPanel.add(consultationLabel);
 
        String[] consultationOptions = {"Academic Consultation", "Personal Counseling"};
        JComboBox<String> consultationDropdown = new JComboBox<>(consultationOptions);
        consultationDropdown.setBounds(680, 210, 160, 25);
        formPanel.add(consultationDropdown);
 
        JLabel dateLabel = new JLabel("Date");
        dateLabel.setBounds(550, 250, 100, 25);
        formPanel.add(dateLabel);
 
        JTextField dateField = new JTextField();
        dateField.setBounds(680, 250, 100, 25);
        formPanel.add(dateField);
 
        JLabel notesLabel = new JLabel("Notes");
        notesLabel.setBounds(550, 290, 100, 25);
        formPanel.add(notesLabel);
 
        JTextArea notesArea = new JTextArea();
        notesArea.setBounds(550, 320, 300, 80);
        formPanel.add(notesArea);
 
        // Create participants table directly in the form
        JLabel participantsTableLabel = new JLabel("PARTICIPANTS:");
        participantsTableLabel.setBounds(20, 210, 100, 25);
        formPanel.add(participantsTableLabel);
 
        // Empty table model with no data
        String[] columnNames = {"#", "Participant Name", "Participant Type", "Actions"};
        Object[][] data = {};
        tableModel = new DefaultTableModel(data, columnNames);
        participantsTable = new JTable(tableModel);
        participantsTable.setRowHeight(30);
        participantsTable.setEnabled(false);  // Disable editing
 
        // Customize table header
        JTableHeader tableHeader = participantsTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 12));
        tableHeader.setBackground(new Color(176, 196, 222));
 
        // Setting column widths
        TableColumn column = participantsTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(30);
        column = participantsTable.getColumnModel().getColumn(1);
        column.setPreferredWidth(200);
        column = participantsTable.getColumnModel().getColumn(2);
        column.setPreferredWidth(150);
        column = participantsTable.getColumnModel().getColumn(3);
        column.setPreferredWidth(100);
 
        // Add table to a scroll pane and add to the form
        JScrollPane tableScrollPane = new JScrollPane(participantsTable);
        tableScrollPane.setBounds(20, 240, 500, 120);
        formPanel.add(tableScrollPane);
 
        // Footer
        JLabel recordedByLabel = new JLabel("Recorded By");
        recordedByLabel.setBounds(20, 490, 100, 25);
        formPanel.add(recordedByLabel);
 
        JTextField recordedByField = new JTextField();
        recordedByField.setBounds(120, 490, 200, 25);
        formPanel.add(recordedByField);
 
        JButton printButton = new JButton("PRINT");
        printButton.setBounds(650, 490, 100, 30);
        formPanel.add(printButton);
 
        JButton saveButton = new JButton("SAVE");
        saveButton.setBounds(760, 490, 100, 30);
        formPanel.add(saveButton);
 
        // Add formPanel to the main panel in the center
        JScrollPane scrollPane = new JScrollPane(formPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
 
        // Add the main panel to the frame
        frame.add(mainPanel);
 
        // Validate and display the frame
        frame.validate();
        frame.setVisible(true);
 
        // Action listener for Save Participant button
        saveParticipantButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String contact = contactField.getText();
                String email = emailField.getText();
                String participantType = (String) participantDropdown.getSelectedItem();
                
                // Validate that at least name fields are filled
                if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, 
                            "Please enter at least first and last name", 
                            "Missing Information", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Add the new participant to the table
                String fullName = firstName + " " + lastName;
                int rowNum = tableModel.getRowCount() + 1;
                tableModel.addRow(new Object[]{rowNum, fullName, participantType, "View | Remove"});
                
                // Store all participant details for later viewing
                Map<String, String> details = new HashMap<>();
                details.put("firstName", firstName);
                details.put("lastName", lastName);
                details.put("fullName", fullName);
                details.put("contact", contact);
                details.put("email", email);
                details.put("type", participantType);
                
                // Store using row index as key
                participantDetails.put(rowNum, details);
                
                // Clear the input fields after saving
                firstNameField.setText("");
                lastNameField.setText("");
                contactField.setText("");
                emailField.setText("");
            }
        });
 
        // Add a listener to handle the "View" and "Remove" actions for each row
        participantsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = participantsTable.rowAtPoint(evt.getPoint());
                int col = participantsTable.columnAtPoint(evt.getPoint());
 
                // Check if the "Actions" column was clicked
                if (col == 3) {
                    String action = (String) participantsTable.getValueAt(row, col);
                    int rowNumber = (int) participantsTable.getValueAt(row, 0);
                    
                    // Parse click position to determine if View or Remove was clicked
                    int clickX = evt.getX();
                    int cellX = participantsTable.getCellRect(row, col, false).x;
                    int relativeX = clickX - cellX;
                    
                    // Approximate width of "View" text - adjust as needed
                    int viewWidth = 40;
                    
                    if (relativeX <= viewWidth) {
                        // "View" part was clicked
                        showParticipantDetails(rowNumber);
                    } else {
                        // "Remove" part was clicked
                        // Show confirmation dialog for removing the participant
                        int option = JOptionPane.showConfirmDialog(frame,
                                "Are you sure you want to remove this participant?", 
                                "Confirm Remove", 
                                JOptionPane.YES_NO_OPTION);
 
                        if (option == JOptionPane.YES_OPTION) {
                            // Remove participant from the table
                            tableModel.removeRow(row);
                            // Also remove from our details map
                            participantDetails.remove(rowNumber);
                        }
                    }
                }
            }
        });
    }
 
    // Display participant details in a separate panel
    private static void showParticipantDetails(int rowNumber) {
        // Get the stored details for this participant
        Map<String, String> details = participantDetails.get(rowNumber);
        
        if (details == null) {
            JOptionPane.showMessageDialog(null, 
                    "Participant details not found", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create a new JFrame for showing the details
        JFrame detailFrame = new JFrame("Participant Details");
        detailFrame.setSize(400, 300);
        detailFrame.setLocationRelativeTo(null); // Center on screen
        
        // Create a panel to show details with a more organized layout
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(0, 2, 10, 10)); // Rows adjust automatically, 2 columns
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        
        // Add all available details
        detailsPanel.add(new JLabel("Full Name:"));
        detailsPanel.add(new JLabel(details.get("fullName")));
        
        detailsPanel.add(new JLabel("Participant Type:"));
        detailsPanel.add(new JLabel(details.get("type")));
        
        // Only add contact if it exists
        if (details.get("contact") != null && !details.get("contact").trim().isEmpty()) {
            detailsPanel.add(new JLabel("Contact Number:"));
            detailsPanel.add(new JLabel(details.get("contact")));
        }
        
        // Only add email if it exists
        if (details.get("email") != null && !details.get("email").trim().isEmpty()) {
            detailsPanel.add(new JLabel("Email:"));
            detailsPanel.add(new JLabel(details.get("email")));
        }
        
        // Add a close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> detailFrame.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        
        // Add panels to the frame
        detailFrame.setLayout(new BorderLayout());
        detailFrame.add(detailsPanel, BorderLayout.CENTER);
        detailFrame.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show the frame
        detailFrame.setVisible(true);
    }
}
