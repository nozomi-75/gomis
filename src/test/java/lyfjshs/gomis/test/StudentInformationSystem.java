package lyfjshs.gomis.test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class StudentInformationSystem extends JPanel {

	// Student Information Fields
	private JTextField lrnField, lastNameField, firstNameField, middleNameField, ageField, motherTongueField, ipField,
			religionField, sexField; 
	private JFormattedTextField birthDateEditor;

	// Residential Address Fields
	private JTextField houseNoField, streetField, barangayField, zipCodeField, regionField, provinceField,
			municipalityField; 

	// Parent's Information Fields
	private JTextField fatherLastNameField, fatherFirstNameField, fatherMiddleNameField, fatherContactNumberField;
	private JTextField motherLastNameField, motherFirstNameField, motherMiddleNameField, motherContactNumberField;

	// Guardian's Information Fields
	private JTextField guardianFullNameField, relationToStudentField, guardianContactNumberField;

	// School Form Information Fields
	private JTextField schoolNameField, schoolIdField, schoolYearField, sectionField, courseField, schoolRegionField,
			divisionField, districtField, semesterField, gradeLevelField, trackStrandField; 

	public StudentInformationSystem() {
		setLayout(new MigLayout("fill, insets 10", "[center]", "[]"));

		JLabel pageTitle = new JLabel("Student Information System", SwingConstants.CENTER);
		pageTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");

		JPanel formContainer = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][][][][][][]"));
		formContainer.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:shade($Panel.background,5%)");
		formContainer.add(pageTitle, "growx, gapbottom 20");

		JScrollPane scrollPane = new JScrollPane(formContainer);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getVerticalScrollBar().setBlockIncrement(50);
		scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		add(scrollPane, "grow");

		formContainer.add(createStudentInformationSection(), "span, growx");
		formContainer.add(createResidentialAddressSection(), "span, growx");
		formContainer.add(createParentsInformationSection(), "span, growx");
		formContainer.add(createGuardiansInformationSection(), "span, growx");
		formContainer.add(createSchoolFormInformationSection(), "span, growx");
		formContainer.add(createButtonPanel(), "span, growx");
	}

	private JPanel createStudentInformationSection() {
		JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
		sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

		JLabel sectionTitle = new JLabel("Student Information");
		sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
		sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

		JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][][][]"));
		contentPanel.setOpaque(false);

		// Row 0: Labels
		JLabel lrnLabel = new JLabel("LRN*");
		lrnLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FF5555");
		contentPanel.add(lrnLabel, "cell 0 0");

		JLabel sexLabel = new JLabel("Sex*");
		sexLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FF5555");
		contentPanel.add(sexLabel, "cell 1 0");

		JLabel lastNameLabel = new JLabel("Last Name*");
		lastNameLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FF5555");
		contentPanel.add(lastNameLabel, "cell 2 0");

		JLabel firstNameLabel = new JLabel("First Name*");
		firstNameLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FF5555");
		contentPanel.add(firstNameLabel, "cell 3 0");

		// Row 1: Fields
		 lrnField = new JTextField();
		lrnField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter LRN");
		contentPanel.add(lrnField, "cell 0 1, growx");

		 sexField = new JTextField();
		sexField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter sex (e.g., Male, Female)");
		contentPanel.add(sexField, "cell 1 1, growx");

		 lastNameField = new JTextField();
		lastNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter last name");
		contentPanel.add(lastNameField, "cell 2 1, growx");

		 firstNameField = new JTextField();
		firstNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter first name");
		contentPanel.add(firstNameField, "cell 3 1, growx");

		// Row 2: Labels
		contentPanel.add(new JLabel("Middle Name"), "cell 0 2");
		JLabel birthDateLabel = new JLabel("Birth Date*");
		birthDateLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FF5555");
		contentPanel.add(birthDateLabel, "cell 1 2");
		contentPanel.add(new JLabel("Age"), "cell 2 2");
		contentPanel.add(new JLabel("Mother Tongue"), "cell 3 2");

		// Row 3: Fields
		 middleNameField = new JTextField();
		middleNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter middle name");
		contentPanel.add(middleNameField, "cell 0 3, growx");

		// Apply DatePicker with JFormattedTextField as editor
		DatePicker birthDatePicker = new DatePicker();
		 birthDateEditor = new JFormattedTextField();
		birthDatePicker.setEditor(birthDateEditor);
		contentPanel.add(birthDateEditor, "cell 1 3, growx");

		 ageField = new JTextField();
		ageField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Auto-calculated age");
		ageField.setEditable(false); // Age field should not be manually editable
		contentPanel.add(ageField, "cell 2 3, growx");

		 motherTongueField = new JTextField();
		motherTongueField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother tongue");
		contentPanel.add(motherTongueField, "cell 3 3, growx");

		// Row 4: Labels
		contentPanel.add(new JLabel("IP (Ethnic Group)"), "cell 0 4");
		contentPanel.add(new JLabel("Religion"), "cell 1 4");

		// Row 5: Fields
		 ipField = new JTextField();
		ipField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter ethnic group if applicable");
		contentPanel.add(ipField, "cell 0 5, growx");

		 religionField = new JTextField();
		religionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter religion");
		contentPanel.add(religionField, "cell 1 5, growx");

		// Add a listener to update the ageField when a date is selected
		birthDatePicker.addDateSelectionListener(dateEvent -> {
			LocalDate birthDate = birthDatePicker.getSelectedDate();
			if (birthDate != null) {
				int age = calculateAge(birthDate, LocalDate.now());
				ageField.setText(String.valueOf(age));
			} else {
				ageField.setText(""); // Clear if no date is selected
			}
		});

		sectionPanel.add(contentPanel, "span, growx");
		return sectionPanel;
	}

	// Method to calculate age based on birthdate
	private int calculateAge(LocalDate birthDate, LocalDate currentDate) {
		if (birthDate != null && currentDate != null) {
			return Period.between(birthDate, currentDate).getYears();
		}
		return 0;
	}

	private JPanel createResidentialAddressSection() {
		JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
		sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

		JLabel sectionTitle = new JLabel("Residential Address");
		sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
		sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

		JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][]"));
		contentPanel.setOpaque(false);

		// Row 0: Labels
		contentPanel.add(new JLabel("House No."), "cell 0 0");
		contentPanel.add(new JLabel("Street"), "cell 1 0");
		JLabel regionLabel = new JLabel("Region*");
		regionLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FF5555");
		contentPanel.add(regionLabel, "cell 2 0");
		JLabel provinceLabel = new JLabel("Province*");
		provinceLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FF5555");
		contentPanel.add(provinceLabel, "cell 3 0");

		// Row 1: Fields
		houseNoField = new JTextField();
		houseNoField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter house number");
		contentPanel.add(houseNoField, "cell 0 1, growx");

		streetField = new JTextField();
		streetField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter street");
		contentPanel.add(streetField, "cell 1 1, growx");

		regionField = new JTextField();
		regionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter region (e.g., Region IV-A)");
		contentPanel.add(regionField, "cell 2 1, growx");

		provinceField = new JTextField();
		provinceField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter province (e.g., Cavite)");
		contentPanel.add(provinceField, "cell 3 1, growx");

		// Row 2: Labels
		JLabel municipalityLabel = new JLabel("Municipality/City*");
		municipalityLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#FF5555");
		contentPanel.add(municipalityLabel, "cell 0 2");
		contentPanel.add(new JLabel("Barangay"), "cell 1 2");
		contentPanel.add(new JLabel("Zip Code"), "cell 2 2");

		// Row 3: Fields
		municipalityField = new JTextField();
		municipalityField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter municipality/city");
		contentPanel.add(municipalityField, "cell 0 3, growx");

		barangayField = new JTextField();
		barangayField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter barangay");
		contentPanel.add(barangayField, "cell 1 3, growx");

		zipCodeField = new JTextField();
		zipCodeField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter zip code");
		contentPanel.add(zipCodeField, "cell 2 3, growx");

		sectionPanel.add(contentPanel, "span, growx");
		return sectionPanel;
	}

	private JPanel createParentsInformationSection() {
		JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
		sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

		JLabel sectionTitle = new JLabel("Parent's Information");
		sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
		sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

		JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][]"));
		contentPanel.setOpaque(false);

		// Row 0: Labels
		contentPanel.add(new JLabel("Father's Last Name"), "cell 0 0");
		contentPanel.add(new JLabel("Mother's Last Name"), "cell 1 0");
		contentPanel.add(new JLabel("Father's First Name"), "cell 2 0");
		contentPanel.add(new JLabel("Mother's First Name"), "cell 3 0");

		// Row 1: Fields
		fatherLastNameField = new JTextField();
		fatherLastNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter father's last name");
		contentPanel.add(fatherLastNameField, "cell 0 1, growx");

		motherLastNameField = new JTextField();
		motherLastNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother's last name");
		contentPanel.add(motherLastNameField, "cell 1 1, growx");

		fatherFirstNameField = new JTextField();
		fatherFirstNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter father's first name");
		contentPanel.add(fatherFirstNameField, "cell 2 1, growx");

		motherFirstNameField = new JTextField();
		motherFirstNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother's first name");
		contentPanel.add(motherFirstNameField, "cell 3 1, growx");

		// Row 2: Labels
		contentPanel.add(new JLabel("Father's Middle Name"), "cell 0 2");
		contentPanel.add(new JLabel("Mother's Middle Name"), "cell 1 2");
		contentPanel.add(new JLabel("Father's Contact"), "cell 2 2");
		contentPanel.add(new JLabel("Mother's Contact"), "cell 3 2");

		// Row 3: Fields
		fatherMiddleNameField = new JTextField();
		fatherMiddleNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter father's middle name");
		contentPanel.add(fatherMiddleNameField, "cell 0 3, growx");

		motherMiddleNameField = new JTextField();
		motherMiddleNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother's middle name");
		contentPanel.add(motherMiddleNameField, "cell 1 3, growx");

		fatherContactNumberField = new JTextField();
		fatherContactNumberField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
				"Enter father's contact number");
		contentPanel.add(fatherContactNumberField, "cell 2 3, growx");

		motherContactNumberField = new JTextField();
		motherContactNumberField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
				"Enter mother's contact number");
		contentPanel.add(motherContactNumberField, "cell 3 3, growx");

		sectionPanel.add(contentPanel, "span, growx");
		return sectionPanel;
	}

	private JPanel createGuardiansInformationSection() {
		JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
		sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

		JLabel sectionTitle = new JLabel("Guardian's Information");
		sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
		sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

		JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow]", "[][]"));
		contentPanel.setOpaque(false);

		// Row 0: Labels
		contentPanel.add(new JLabel("Guardian's Full Name"), "cell 0 0");
		contentPanel.add(new JLabel("Relation to Student"), "cell 1 0");
		contentPanel.add(new JLabel("Contact Number"), "cell 2 0");

		// Row 1: Fields
		guardianFullNameField = new JTextField();
		guardianFullNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter guardian's full name");
		contentPanel.add(guardianFullNameField, "cell 0 1, growx");

		relationToStudentField = new JTextField();
		relationToStudentField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter relation to student");
		contentPanel.add(relationToStudentField, "cell 1 1, growx");

		guardianContactNumberField = new JTextField();
		guardianContactNumberField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
				"Enter guardian's contact number");
		contentPanel.add(guardianContactNumberField, "cell 2 1, growx");

		sectionPanel.add(contentPanel, "span, growx");
		return sectionPanel;
	}

	private JPanel createSchoolFormInformationSection() {
		JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
		sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

		JLabel sectionTitle = new JLabel("School Form Information");
		sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
		sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

		JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][][][]"));
		contentPanel.setOpaque(false);

		// Row 0: Labels
		contentPanel.add(new JLabel("School Name"), "cell 0 0");
		contentPanel.add(new JLabel("School ID"), "cell 1 0");
		contentPanel.add(new JLabel("Region"), "cell 2 0");
		contentPanel.add(new JLabel("Division"), "cell 3 0");

		// Row 1: Fields
		schoolNameField = new JTextField();
		schoolNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter school name");
		contentPanel.add(schoolNameField, "cell 0 1, growx");

		schoolIdField = new JTextField();
		schoolIdField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter school ID");
		contentPanel.add(schoolIdField, "cell 1 1, growx");

		schoolRegionField = new JTextField();
		schoolRegionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter region (e.g., Region IV-A)");
		contentPanel.add(schoolRegionField, "cell 2 1, growx");

		divisionField = new JTextField();
		divisionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter division");
		contentPanel.add(divisionField, "cell 3 1, growx");

		// Row 2: Labels
		contentPanel.add(new JLabel("District"), "cell 0 2");
		contentPanel.add(new JLabel("Semester"), "cell 1 2");
		contentPanel.add(new JLabel("School Year"), "cell 2 2");
		contentPanel.add(new JLabel("Grade Level"), "cell 3 2");

		// Row 3: Fields
		districtField = new JTextField();
		districtField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter district");
		contentPanel.add(districtField, "cell 0 3, growx");

		semesterField = new JTextField();
		semesterField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter semester (e.g., 1st Semester)");
		contentPanel.add(semesterField, "cell 1 3, growx");

		schoolYearField = new JTextField();
		schoolYearField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. 2024-2025");
		contentPanel.add(schoolYearField, "cell 2 3, growx");

		gradeLevelField = new JTextField();
		gradeLevelField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter grade level (e.g., Grade 11)");
		contentPanel.add(gradeLevelField, "cell 3 3, growx");

		// Row 4: Labels
		contentPanel.add(new JLabel("Section"), "cell 0 4");
		contentPanel.add(new JLabel("Track & Strand"), "cell 1 4");
		contentPanel.add(new JLabel("Course"), "cell 2 4");

		// Row 5: Fields
		sectionField = new JTextField();
		sectionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter section");
		contentPanel.add(sectionField, "cell 0 5, growx");

		trackStrandField = new JTextField();
		trackStrandField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
				"Enter track and strand (e.g., STEM)");
		contentPanel.add(trackStrandField, "cell 1 5, growx");

		courseField = new JTextField();
		courseField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter course");
		contentPanel.add(courseField, "cell 2 5, growx");

		sectionPanel.add(contentPanel, "span, growx");
		return sectionPanel;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout("insets 10", "[grow][][]", "[]"));
		buttonPanel.putClientProperty(FlatClientProperties.STYLE, "background:$Panel.background");

		JButton clearButton = new JButton("Clear Form");
		clearButton.putClientProperty(FlatClientProperties.STYLE,
				"background:shade($Panel.background,10%); borderWidth:0; focusWidth:0; innerFocusWidth:0");
		clearButton.addActionListener(e -> clearForm());

		JButton submitButton = new JButton("Submit");
		submitButton.putClientProperty(FlatClientProperties.STYLE,
				"background:tint($Panel.background,10%); borderWidth:0; focusWidth:0; innerFocusWidth:0");
		submitButton.addActionListener(e -> submitForm());

		buttonPanel.add(new JLabel(), "grow");
		buttonPanel.add(clearButton, "gapright 10");
		buttonPanel.add(submitButton);

		return buttonPanel;
	}

	private void clearForm() {
		Component[] components = getComponents(); 
		for (Component comp : components) {
			if (comp instanceof JScrollPane scrollPane) {
				JPanel formContainer = (JPanel) scrollPane.getViewport().getView();
				for (Component section : formContainer.getComponents()) {
					if (section instanceof JPanel sectionPanel && !sectionPanel.getComponent(0).equals(new JButton())) {
						JPanel contentPanel = (JPanel) sectionPanel.getComponent(1);
						for (Component field : contentPanel.getComponents()) {
							if (field instanceof JTextField textField) {
								textField.setText("");
							} else if (field instanceof JSpinner spinner) {
								spinner.setValue(new Date());
							}
						}
					}
				}
			}
		}
	}

	private void submitForm() {
		JOptionPane.showMessageDialog(this, "Form submitted (placeholder action)", "Success",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				FlatLaf.setup(new FlatMacLightLaf());
			} catch (Exception e) {
				e.printStackTrace();
			}

			JFrame frame = new JFrame("Student Information System");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(400, 300);
			JToggleButton toggleButton = new JToggleButton("Switch to Dark Mode");
			toggleButton.addActionListener(e -> {
				if (toggleButton.isSelected()) {
					FlatLaf.setup(new FlatMacDarkLaf());
					toggleButton.setText("Switch to Light Mode");
				} else {
					FlatLaf.setup(new FlatMacLightLaf());
					toggleButton.setText("Switch to Dark Mode");
				}
				SwingUtilities.updateComponentTreeUI(frame);
			});
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(toggleButton, BorderLayout.NORTH);

			frame.getContentPane().add(new StudentInformationSystem(), BorderLayout.CENTER);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}