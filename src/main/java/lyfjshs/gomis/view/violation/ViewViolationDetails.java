package lyfjshs.gomis.view.violation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;

public class ViewViolationDetails extends JPanel {
	private JTextField txtViolationID;
	private JTextField txtViolationType;
	private JTextField txtStatus;
	private JTextField txtUpdatedAt;
	private JTextArea txtDescription;
	private JTextArea txtAnecdotal;
	private JTextField txtFullName;
	private JTextField txtStudentLRN;
	private JTextField txtAge;
	private JTextField txtSex;
	private JTextField txtGradeSection;
	private JTextField txtContactNumber;
	private JTextField txtReinforcementType;
	private JTextField txtActionRecommended;
	private JTextArea txtActions;
	private ViolationRecord violation;
	private StudentsDataDAO studentsDataDAO;
	private ParticipantsDAO participantsDAO;

	public ViewViolationDetails(ViolationRecord violation, StudentsDataDAO studentsDataDAO,
			ParticipantsDAO participantsDAO) {
		this.violation = violation;
		this.studentsDataDAO = studentsDataDAO;
		this.participantsDAO = participantsDAO;
		initComponents();
		loadViolationData();
	}

	private void initComponents() {
		setLayout(new MigLayout("wrap, insets 30", "[grow]", "[]"));
		putClientProperty("FlatLaf.style", "arc: 8");

		JPanel mainPanel = new JPanel(new MigLayout("wrap, fillx, insets 20", "[grow]", "[]"));
		mainPanel.putClientProperty("FlatLaf.style", "arc: 8");
		mainPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1),
				BorderFactory.createEmptyBorder(20, 20, 20, 20)));

		JLabel sectionTitle1 = new JLabel("Violation Record Details");
		sectionTitle1.setFont(new Font("Arial", Font.BOLD, 16));
		sectionTitle1.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
		mainPanel.add(sectionTitle1, "spanx,growx,wrap 15");

		JPanel violationGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
		violationGrid.setBackground(Color.WHITE);

		txtViolationID = new JTextField(20);
		txtViolationType = new JTextField(20);
		txtStatus = new JTextField(20);
		txtUpdatedAt = new JTextField(20);

		addLabelValuePair(violationGrid, "Violation ID:", txtViolationID);
		addLabelValuePair(violationGrid, "Violation Type:", txtViolationType);
		addLabelValuePair(violationGrid, "Status:", txtStatus);
		addLabelValuePair(violationGrid, "Updated At:", txtUpdatedAt);

		mainPanel.add(violationGrid, "span, wrap 15");

		JLabel sectionTitle2 = new JLabel("Violation Description");
		sectionTitle2.setFont(new Font("Arial", Font.BOLD, 14));
		sectionTitle2.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
		mainPanel.add(sectionTitle2, "spanx,growx,wrap 10");

		txtDescription = new JTextArea(5, 20);
		txtDescription.setLineWrap(true);
		txtDescription.setWrapStyleWord(true);
		txtDescription.setEditable(false);
		txtDescription.setFont(new Font("Arial", Font.PLAIN, 12));
		txtDescription.setBackground(Color.WHITE);
		mainPanel.add(new JScrollPane(txtDescription), "span, growx, wrap 15");

		JLabel sectionTitle3 = new JLabel("Anecdotal Record");
		sectionTitle3.setFont(new Font("Arial", Font.BOLD, 14));
		sectionTitle3.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
		mainPanel.add(sectionTitle3, "spanx,growx,wrap 10");

		txtAnecdotal = new JTextArea(5, 20);
		txtAnecdotal.setLineWrap(true);
		txtAnecdotal.setWrapStyleWord(true);
		txtAnecdotal.setEditable(false);
		txtAnecdotal.setFont(new Font("Arial", Font.PLAIN, 12));
		txtAnecdotal.setBackground(Color.WHITE);
		mainPanel.add(new JScrollPane(txtAnecdotal), "span, growx, wrap 15");

		JLabel sectionTitle4 = new JLabel("Student Information");
		sectionTitle4.setFont(new Font("Arial", Font.BOLD, 14));
		sectionTitle4.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
		mainPanel.add(sectionTitle4, "spanx,growx,wrap 10");

		JPanel studentGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
		studentGrid.setBackground(Color.WHITE);

		txtFullName = new JTextField(20);
		txtStudentLRN = new JTextField(20);
		txtAge = new JTextField(20);
		txtSex = new JTextField(20);
		txtGradeSection = new JTextField(20);
		txtContactNumber = new JTextField(20);

		addLabelValuePair(studentGrid, "Full Name:", txtFullName);
		addLabelValuePair(studentGrid, "Student LRN:", txtStudentLRN);
		addLabelValuePair(studentGrid, "Age:", txtAge);
		addLabelValuePair(studentGrid, "Sex:", txtSex);
		addLabelValuePair(studentGrid, "Grade/Section:", txtGradeSection);
		addLabelValuePair(studentGrid, "Contact Number:", txtContactNumber);

		mainPanel.add(studentGrid, "span, wrap 15");

		JLabel sectionTitle5 = new JLabel("Reinforcement & Recommendations");
		sectionTitle5.setFont(new Font("Arial", Font.BOLD, 14));
		sectionTitle5.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
		mainPanel.add(sectionTitle5, "spanx,growx,wrap 10");

		JPanel reinforcementGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
		reinforcementGrid.setBackground(Color.WHITE);

		txtReinforcementType = new JTextField(20);
		txtActionRecommended = new JTextField(20);

		addLabelValuePair(reinforcementGrid, "Reinforcement Type:", txtReinforcementType);
		addLabelValuePair(reinforcementGrid, "Action Recommended:", txtActionRecommended);

		mainPanel.add(reinforcementGrid, "span, wrap 10");

		JLabel recommendedActionsLabel = new JLabel("Recommended actions include:");
		recommendedActionsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		mainPanel.add(recommendedActionsLabel, "span, wrap 5");

		txtActions = new JTextArea(
				"• Conduct a one-on-one counseling session\n" +
						"• Develop a behavior improvement plan\n" +
						"• Involve parents/guardians in the intervention\n" +
						"• Monitor progress and provide ongoing support");
		txtActions.setEditable(false);
		txtActions.setFont(new Font("Arial", Font.PLAIN, 12));
		txtActions.setBackground(Color.WHITE);
		mainPanel.add(new JScrollPane(txtActions), "span, growx");
	}

	private void loadViolationData() {
		if (violation != null) {
			try {
				Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
				if (participant != null) {
					txtFullName.setText(
							participant.getParticipantFirstName() + " " + participant.getParticipantLastName());
					txtContactNumber.setText(participant.getContactNumber());
					txtReinforcementType.setText(violation.getReinforcement());
					txtActionRecommended.setText(violation.getViolationDescription());

					if ("Student".equals(participant.getParticipantType())) {
						Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
						if (student != null) {
							txtStudentLRN.setText(student.getStudentLrn());
							txtAge.setText(student.getStudentBirthdate() != null
									? String.valueOf(calculateAge(student.getStudentBirthdate()))
									: "");
							txtSex.setText(student.getStudentSex() != null ? student.getStudentSex() : "");
							txtGradeSection
									.setText(student.getSchoolSection() != null ? student.getSchoolSection() : "");
						}
					}
				}
				txtViolationID.setText(String.valueOf(violation.getViolationId()));
				txtViolationType.setText(violation.getViolationType());
				txtStatus.setText(violation.getStatus());
				txtUpdatedAt.setText(violation.getUpdatedAt().toString());
				txtDescription.setText(violation.getViolationDescription());
				txtAnecdotal.setText(violation.getAnecdotalRecord());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private int calculateAge(Date birthdate) {
		LocalDate birthDate = birthdate.toLocalDate();
		return Period.between(birthDate, LocalDate.now()).getYears();
	}

	private void addLabelValuePair(JPanel panel, String labelText, JTextField valueField) {
		JLabel label = new JLabel(labelText);
		label.setFont(new Font("Arial", Font.BOLD, 12));
		label.setForeground(new Color(85, 85, 85));
		valueField.setEditable(false);
		valueField.setFont(new Font("Arial", Font.PLAIN, 12));
		valueField.setBackground(Color.WHITE);
		panel.add(label);
		panel.add(valueField, "growx");
	}

	public static void showDialog(Component parent, ViolationRecord violation, StudentsDataDAO studentsDataDAO,
			ParticipantsDAO participantsDAO) {
		ViewViolationDetails detailsPanel = new ViewViolationDetails(violation, studentsDataDAO, participantsDAO);

		// Configure modal options
		Option option = ModalDialog.createOption();
		option.setOpacity(0.3f)
			.setAnimationOnClose(false)
			.getBorderOption()
			.setBorderWidth(0.5f)
			.setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

		// Show modal with the correct ID
		String modalId = "violation_details_" + violation.getViolationId();
		if (ModalDialog.isIdExist(modalId)) {
			ModalDialog.closeModal(modalId);
		}

		ModalDialog.showModal(parent,
				new SimpleModalBorder(detailsPanel, "Violation Details",
						new SimpleModalBorder.Option[] {
								new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
						},
						(controller, action) -> {
							if (action == SimpleModalBorder.CLOSE_OPTION) {
								controller.close();
							}
						}),
				modalId);

		// Set size
		option.getLayoutOption().setSize(800, 600);
	}
}