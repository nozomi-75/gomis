package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import net.miginfocom.swing.MigLayout; // Import MigLayout

public class ViewViolationDetails extends JDialog {

	private JTextField txtLRN;
	private JTextField txtFIRST_NAME;
	private JTextField txtLAST_NAME;
	private JTextField txtContact;
	private JComboBox<String> cmbParticipantType;
	private JComboBox<String> cmbViolationType;
	private JComboBox<String> cmbStatus;
	private JComboBox<String> cmbReinforcement;
	private JTextArea txtDescription;
	private JButton btnEdit;
	private JButton btnClose;	
	private ViolationRecord violation;
	private boolean isEditing = false;
	private StudentsDataDAO studentsDataDAO; // Add this line
	private ParticipantsDAO participantsDAO; // Add this line

	public ViewViolationDetails(JFrame parent, ViolationRecord violation, StudentsDataDAO studentsDataDAO, ParticipantsDAO participantsDAO) {
		super(parent, "Violation Details", true);
		this.violation = violation;
		this.studentsDataDAO = studentsDataDAO; // Add this line
		this.participantsDAO = participantsDAO; // Add this line
		initComponents();
		loadViolationData();
		setLocationRelativeTo(parent);
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());

		// Main panel using MigLayout
		JPanel mainPanel = new JPanel(new MigLayout("wrap 2, gapy 5, insets 20")); // 2 columns, gapy 5, 20px insets

		// Initialize components
		cmbParticipantType = new JComboBox<>(new String[] { "Student", "Teacher", "Staff" });
		txtLRN = new JTextField(20);
		txtFIRST_NAME = new JTextField(20);
		txtLAST_NAME = new JTextField(20);
		txtContact = new JTextField(20);
		cmbViolationType = new JComboBox<>(new String[] { "Minor", "Major", "Critical" });
		cmbStatus = new JComboBox<>(new String[] { "Pending", "Resolved" });
		cmbReinforcement = new JComboBox<>(new String[] { "Warning", "Suspension", "Expulsion" });
		txtDescription = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(txtDescription); // Scroll pane for description

		// Make all components non-editable initially
		setFieldsEditable(false);

		// Add components to panel using MigLayout constraints

		mainPanel.add(new JLabel("Participant Type:"), "");
		mainPanel.add(cmbParticipantType, "growx");

		mainPanel.add(new JLabel("LRN:"), "");
		mainPanel.add(txtLRN, "growx");

		mainPanel.add(new JLabel("First Name:"), "");
		mainPanel.add(txtFIRST_NAME, "growx");

		mainPanel.add(new JLabel("Last Name:"), "");
		mainPanel.add(txtLAST_NAME, "growx");


		mainPanel.add(new JLabel("Contact:"), "");
		mainPanel.add(txtContact, "growx");

		mainPanel.add(new JLabel("Violation Type:"), "");
		mainPanel.add(cmbViolationType, "growx");

		mainPanel.add(new JLabel("Status:"), "");
		mainPanel.add(cmbStatus, "growx");

		mainPanel.add(new JLabel("Reinforcement:"), "");
		mainPanel.add(cmbReinforcement, "growx");

		mainPanel.add(new JLabel("Description:"), "span 2, growx"); // Span 2 columns
		mainPanel.add(scrollPane, "span 2, growx, growy"); // Span 2, grow both horizontally and vertically

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnEdit = new JButton("Edit");
		btnClose = new JButton("Close");

		btnEdit.addActionListener(e -> toggleEdit());
		btnClose.addActionListener(e -> dispose());

		buttonPanel.add(btnEdit);
		buttonPanel.add(btnClose);

		// Add panels to dialog
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Set dialog properties
		setSize(364, 446);
		setResizable(false);
	}

	// ... (rest of the code remains the same)
	private void setFieldsEditable(boolean editable) {

		txtLRN.setEditable(editable);

		txtFIRST_NAME.setEditable(editable);

		txtLAST_NAME.setEditable(editable);

		txtContact.setEditable(editable);

		cmbParticipantType.setEnabled(editable);

		cmbViolationType.setEnabled(editable);

		cmbStatus.setEnabled(editable);

		cmbReinforcement.setEnabled(editable);

		txtDescription.setEditable(editable);

	}

	private void toggleEdit() {
		if (!isEditing) {
			int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to edit?", "Confirm Edit",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		} else {
			int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to save the changes?",
					"Confirm Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		}

		isEditing = !isEditing;
		setFieldsEditable(isEditing);
		btnEdit.setText(isEditing ? "Save" : "Edit");

		if (!isEditing) {
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
			violation.setParticipantId(getStudentUidFromLrn(txtLRN.getText()));
			violation.setViolationType((String) cmbViolationType.getSelectedItem());
			violation.setStatus((String) cmbStatus.getSelectedItem());
			violation.setReinforcement((String) cmbReinforcement.getSelectedItem());
			violation.setViolationDescription(txtDescription.getText()); // Save description
		}
	}

	// Method to get studentUid from LRN
	private int getStudentUidFromLrn(String lrn) {
		try {
			Student student = studentsDataDAO.getStudentDataByLrn(lrn);
			if (student != null) {
				return student.getStudentUid();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1; // Return -1 if student not found
	}
}