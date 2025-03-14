package lyfjshs.gomis.view.incident;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;
import lyfjshs.gomis.view.appointment.StudentSearchUI;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JOptionPane;

public class IncidentFillUpForm extends Form {

    private static final long serialVersionUID = 1L;
    private JTextField nameField;
    private JTextField addressField;
    private JTextField dateTimeField;
    private JTextField DateField;
    private JTextArea narrativeReportField;
    private JTextArea actionsTakenField;
    private JTextArea recommendationsField;
    private JTextField reportedByField;
    private JTextField TimeField;
    private JTextField guardianNumberField;
    private JTextField GradeSectionField;
    private JTextField ageField;
    private JTextField sexField;
    private Connection conn;
    private JTable table;

    public IncidentFillUpForm(Connection connectDB) {
        this.conn = connectDB;
        setLayout(new MigLayout("", "[grow][grow]", "[38px][][200px][200px][200px][pref]"));

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("", "[grow]", "[]"));
        JLabel lblTitle = new JLabel("INCIDENT Fill-Up Form");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(lblTitle, "grow");
        add(headerPanel, "cell 0 0 2 1,grow");

        // Incident Details Panel
        JPanel detailsPanel = new JPanel(new MigLayout("", "[][grow][][][][grow][]", "[][]"));
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 177)),
                "INCIDENT DETAILS",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12),
                new Color(100, 149, 177)));
        
                JLabel label_1 = new JLabel("Reported By:");
                detailsPanel.add(label_1, "cell 0 0,alignx left");
        detailsPanel.add(reportedByField = new JTextField(), "cell 1 0,growx");
        
                JLabel label_2 = new JLabel("Date: ");
                detailsPanel.add(label_2, "cell 4 0");
        detailsPanel.add(DateField = new JTextField(), "grow");

        detailsPanel.add(new JLabel("Grade & Section: "), "cell 0 1,alignx left");
        detailsPanel.add(GradeSectionField = new JTextField(), "cell 1 1,growx");

        detailsPanel.add(new JLabel("Time: "), "cell 4 1,alignx left");
        detailsPanel.add(TimeField = new JTextField(), "cell 5 1,growx");

        add(detailsPanel, "cell 0 1 2 1,grow");

        // Narrative Report Panel
        JPanel narrativePanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
        narrativePanel.add(new JLabel("Narrative Report"), "cell 0 0");
        narrativeReportField = new JTextArea();
        narrativeReportField.setLineWrap(true);
        narrativeReportField.setWrapStyleWord(true);
        JScrollPane narrativeScrollPane = new JScrollPane(narrativeReportField);
        narrativeScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 177)),
                "Narrative Report:",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 17),
                new Color(0, 0, 0)));
        add(narrativeScrollPane, "cell 0 2 2 1,grow");

        // Actions Taken Panel
        JPanel actionsPanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
        actionsPanel.add(new JLabel("Action Taken"), "cell 0 0");
        actionsTakenField = new JTextArea();
        actionsTakenField.setLineWrap(true);
        actionsTakenField.setWrapStyleWord(true);
        JScrollPane actionsScrollPane = new JScrollPane(actionsTakenField);
        actionsScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 177)),
                "Action Taken:",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 17),
                new Color(0, 0, 0)));
        add(actionsScrollPane, "cell 0 3,grow");

        // Recommendations Panel
        JPanel recommendationsPanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
        recommendationsPanel.add(new JLabel("Recommendation"), "cell 0 0");
        recommendationsField = new JTextArea();
        recommendationsField.setLineWrap(true);
        recommendationsField.setWrapStyleWord(true);
        JScrollPane recommendationsScrollPane = new JScrollPane(recommendationsField);
        recommendationsScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 177)),
                "Recommendation:",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 17),
                new Color(0, 0, 0)));
        add(recommendationsScrollPane, "cell 1 3,grow");

        // Add Participant Panel
        JPanel addParticipantPanel = new JPanel();
        addParticipantPanel.setBorder(new TitledBorder(null, " Add Participant: ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(addParticipantPanel, "cell 0 4,grow");
        addParticipantPanel.setLayout(new MigLayout("", "[][grow]", "[][][][][]"));
        
                JComboBox<String> violationComboBox = new JComboBox<>();
                violationComboBox.addItem("Minor");
                violationComboBox.addItem("Major");
                violationComboBox.addItem("Academic Dishonesty");
                violationComboBox.addItem("Bullying");
                violationComboBox.addItem("Dress Code");
                violationComboBox.addItem("Tardiness");
                violationComboBox.addItem("Cutting Classes");
                violationComboBox.addItem("Vandalism");
                
                JLabel ViolationLabel = new JLabel("Violation: ");
                addParticipantPanel.add(ViolationLabel, "cell 0 0,alignx left");
                addParticipantPanel.add(violationComboBox, "cell 1 0,growx");
                String violation = (String) violationComboBox.getSelectedItem();
                
                JComboBox<String> ParticipantcomboBox = new JComboBox<>();
                ParticipantcomboBox.addItem("Student");
                ParticipantcomboBox.addItem("non-Student");
               
                JLabel ParticipantLabel = new JLabel("Participant:");
                addParticipantPanel.add(ParticipantLabel, "cell 0 1,alignx left");
                addParticipantPanel.add(ParticipantcomboBox, "cell 1 1,growx");
                String Participant = (String) ParticipantcomboBox.getSelectedItem();
        
                JLabel label = new JLabel("Name: ");
                addParticipantPanel.add(label, "flowx,cell 0 2");
        JTextField participantNameField = new JTextField();
        addParticipantPanel.add(participantNameField, "cell 1 2,growx");
        participantNameField.setColumns(10);
        String name = participantNameField.getText().trim();
        
                    // Clear the name field after adding
                    participantNameField.setText("");
        
                JButton searchButton = new JButton("Search by LRN");
                addParticipantPanel.add(searchButton, "cell 0 3");
                
                        // Add action listeners
                        searchButton.addActionListener(event -> {
                            JTextField lrnField = new JTextField();
                            Object[] message = {
                                "Enter Student LRN:", lrnField
                            };
                
                            int option = JOptionPane.showConfirmDialog(this, message, "Search Student", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                String lrn = lrnField.getText().trim();
                                if (lrn.isEmpty()) {
                                    JOptionPane.showMessageDialog(this, "Please enter an LRN", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                
                                // TODO: Implement LRN search functionality
                                // This should open a dialog to search by LRN and populate the participantNameField
                            }
                        });

        JButton addButton = new JButton("Add");
        addParticipantPanel.add(addButton, "cell 0 4");

        addButton.addActionListener(e -> {

            if (name.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Please enter a name",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int rowCount = model.getRowCount() + 1;
            model.addRow(new Object[]{rowCount, name, violation});
        });

        // List of Participants Panel
        JPanel listOfParticipantsPanel = new JPanel();
        listOfParticipantsPanel.setBorder(new TitledBorder(null, " List of Participant: ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(listOfParticipantsPanel, "cell 1 4,grow");
        listOfParticipantsPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));

        // Create table model with columns
        String[] columnNames = {"#", "Name", "Violation", "Action"};
        Object[][] data = {};
        table = new JTable(new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only allow editing of the Action column
            }
        });

        // Add custom renderer for the Action column
        table.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
                JButton viewButton = new JButton("View");
                JButton removeButton = new JButton("Remove");
                panel.add(viewButton);
                panel.add(removeButton);
                return panel;
            }
        });

        listOfParticipantsPanel.add(new JScrollPane(table), "cell 0 0,grow");

        // Add table button action listeners
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int column = table.getColumnModel().getColumnIndexAtX(evt.getX());
                int row = evt.getY() / table.getRowHeight();

                if (row < table.getRowCount() && row >= 0) {
                    if (column == 3) { // Action column
                        int modelColumn = table.convertColumnIndexToModel(column);
                        int modelRow = table.convertRowIndexToModel(row);

                        // Calculate if click was on View or Remove button
                        int buttonWidth = 60; // Approximate width of each button
                        int x = evt.getX();

                        if (x < buttonWidth) {
                            // View button clicked
                            String name = (String) table.getValueAt(row, 1);
                            String violation = (String) table.getValueAt(row, 2);
                            javax.swing.JOptionPane.showMessageDialog(IncidentFillUpForm.this,
                                "Name: " + name + "\nViolation: " + violation,
                                "View Details",
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        } else if (x < buttonWidth * 2) {
                            // Remove button clicked
                            int confirm = javax.swing.JOptionPane.showConfirmDialog(IncidentFillUpForm.this,
                                "Are you sure you want to remove this entry?",
                                "Confirm Removal",
                                javax.swing.JOptionPane.YES_NO_OPTION);

                            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                                ((DefaultTableModel) table.getModel()).removeRow(row);
                            }
                        }
                    }
                }
            }
        });

        // Footer Panel
        JPanel footPanel = new JPanel(new MigLayout("", "[grow,fill][center]", "[]"));
        add(footPanel, "cell 0 5 2 1,grow");

        JButton submitButton = new JButton("Submit");
        footPanel.add(submitButton, "cell 1 0");

        // Add Student Search Button
        JButton studentSearchButton = new JButton("Student Search");
        detailsPanel.add(studentSearchButton, "cell 6 0");

        // Add action listener to the Student Search button
        studentSearchButton.addActionListener(e -> openStudentSearchUI());
    }

    private void openStudentSearchUI() {
        StudentSearchUI studentSearchUI = new StudentSearchUI();
        studentSearchUI.createAndShowGUI();
    }
}
