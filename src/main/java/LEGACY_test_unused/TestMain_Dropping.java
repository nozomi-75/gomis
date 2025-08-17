/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package LEGACY_test_unused;

import java.awt.EventQueue;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatDarculaLaf;

import docPrinter.templateManager;
import docPrinter.droppingForm.droppingFormGenerator;
import docPrinter.droppingForm.droppingFormModalPanel;
import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Student;
import net.miginfocom.swing.MigLayout;

public class TestMain_Dropping extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private DefaultTableModel tableModel;
	private List<Student> studentList;
	private StudentsDataDAO studentsDataDAO;
	private File templateFile;
	private File outputFolder;
	private JTextField selectedTemplateLoc;
	private JTextField outputFolderLoc;
	private droppingFormGenerator generator = new droppingFormGenerator();

    public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				// Establish database connection first
				DBConnection.setParentComponent(null);
				Connection conn = DBConnection.getConnection();
				FlatDarculaLaf.setup();
				
				TestMain_Dropping frame = new TestMain_Dropping(conn);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to connect to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	public TestMain_Dropping(Connection connection) {
		this.studentsDataDAO = new StudentsDataDAO(connection);
		
		setTitle("Dropping Form - Test Generator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 700);
		
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[784px,grow]", "[36px][525px][]"));

		// Header with controls
		JPanel headerPanel = new JPanel(new MigLayout("", "[grow][grow][][][]", "[][]"));
		JButton loadTemplateBtn = new JButton("Load Template");
		JButton selectOutputBtn = new JButton("Select Output Folder");
		JButton validateTemplateBtn = new JButton("Validate Template");
		JButton resetTemplateBtn = new JButton("Reset to Default");
		JButton printSelectedBtn = new JButton("Generate Forms");
		
		headerPanel.add(loadTemplateBtn, "cell 0 0");
		headerPanel.add(selectOutputBtn, "cell 1 0");
		headerPanel.add(validateTemplateBtn, "cell 2 0");
		headerPanel.add(resetTemplateBtn, "cell 3 0");
		headerPanel.add(printSelectedBtn, "cell 4 0,gapx push"); // Pushes the button to the right
		contentPane.add(headerPanel, "cell 0 0,growx,aligny top");
		selectedTemplateLoc = new JTextField();
		selectedTemplateLoc.setEditable(false);
		headerPanel.add(selectedTemplateLoc, "cell 0 1,growx");
		selectedTemplateLoc.setColumns(10);
		outputFolderLoc = new JTextField();
		outputFolderLoc.setEditable(false);
		headerPanel.add(outputFolderLoc, "cell 1 1,growx");
		outputFolderLoc.setColumns(10);

		// Table setup
		String[] columns = {"Select", "LRN", "Name", "Sex", "Grade & Section", "Track & Strand"};
		tableModel = new DefaultTableModel(null, columns) {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0) return Boolean.class;
				return String.class;
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0; // Only checkbox is editable
			}
		};

		table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		contentPane.add(scrollPane, "cell 0 1,grow");

		// Load data from database
		initializeDefaultPaths();
		loadStudentData();

		// Add action listeners
		loadTemplateBtn.addActionListener(e -> loadTemplate());
		selectOutputBtn.addActionListener(e -> selectOutputFolder());
		validateTemplateBtn.addActionListener(e -> validateTemplate());
		resetTemplateBtn.addActionListener(e -> resetTemplate());
		printSelectedBtn.addActionListener(e -> printSelectedStudents());
	}

	private void loadStudentData() {
		try {
			studentList = studentsDataDAO.getAllStudentsData(); // A method to get all students
			tableModel.setRowCount(0); // Clear existing data
			for (Student s : studentList) {
				String trackAndStrand = s.getSchoolForm() != null ? s.getSchoolForm().getSF_TRACK_AND_STRAND() : "";
				tableModel.addRow(new Object[]{
					false, 
					s.getStudentLrn(), 
					s.getStudentLastname() + ", " + s.getStudentFirstname(),
					s.getStudentSex(),
					s.getSchoolForm().getSF_GRADE_LEVEL() + " - " + s.getSchoolForm().getSF_SECTION(),
					trackAndStrand
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to load student data.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void initializeDefaultPaths() {
		// Use templateManager to get the active template and output folder
		templateFile = templateManager.getActiveTemplate(templateManager.TemplateType.DROPPING_FORM);
		selectedTemplateLoc.setText(templateFile.getAbsolutePath());

		outputFolder = templateManager.getDefaultOutputFolder();
		outputFolderLoc.setText(outputFolder.getAbsolutePath());

		if (!templateFile.exists()) {
			JOptionPane.showMessageDialog(
				this,
				"No active template found for Dropping Form. Please load or import a template using the Template Manager.",
				"Missing Template",
				JOptionPane.WARNING_MESSAGE
			);
		}
	}
	
	private void validateTemplate() {
		boolean isValid = templateManager.validateTemplate(templateManager.TemplateType.DROPPING_FORM);
		String status = templateManager.getTemplateStatus(templateManager.TemplateType.DROPPING_FORM);
		
		if (isValid) {
			JOptionPane.showMessageDialog(this, 
				"Template is valid!\n" + status, 
				"Template Validation", 
				JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, 
				"Template validation failed!\n" + status, 
				"Template Validation", 
				JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void resetTemplate() {
		int result = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to reset the Dropping Form template to default?",
			"Reset Template",
			JOptionPane.YES_NO_OPTION);
			
		if (result == JOptionPane.YES_OPTION) {
			boolean success = templateManager.resetToDefaultTemplate(templateManager.TemplateType.DROPPING_FORM);
			if (success) {
				initializeDefaultPaths(); // Refresh the template path
				JOptionPane.showMessageDialog(this, 
					"Template reset to default successfully.", 
					"Reset Template", 
					JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, 
					"Failed to reset template.", 
					"Reset Template", 
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	private void loadTemplate() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select DOCX Template");
		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".docx");
			}
			@Override
			public String getDescription() {
				return "DOCX files (*.docx)";
			}
		});
		
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			if (templateManager.importCustomTemplate(templateManager.TemplateType.DROPPING_FORM, selected)) {
				templateFile = templateManager.getActiveTemplate(templateManager.TemplateType.DROPPING_FORM);
				selectedTemplateLoc.setText(templateFile.getAbsolutePath());
				JOptionPane.showMessageDialog(this, "Template imported and set as active.", "Success", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "Failed to import template.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void selectOutputFolder() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select Output Folder");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			outputFolder = chooser.getSelectedFile();
			outputFolderLoc.setText(outputFolder.getAbsolutePath());
		}
	}

	private void printSelectedStudents() {
		templateFile = templateManager.getActiveTemplate(templateManager.TemplateType.DROPPING_FORM);
		if (templateFile == null || !templateFile.exists() || outputFolder == null) {
			JOptionPane.showMessageDialog(this, "Please load a template and select an output folder first.", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		List<Student> selectedStudents = new ArrayList<>();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
			if (checked != null && checked) {
				selectedStudents.add(studentList.get(i));
			}
		}

		if (selectedStudents.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No students selected.", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		droppingFormModalPanel modalPanel = new droppingFormModalPanel(generator);
		modalPanel.setSelectedStudents(selectedStudents);
		modalPanel.setTemplateAndOutput(templateFile, outputFolder);
		modalPanel.showModal(studentsDataDAO, this, () -> {
			JOptionPane.showMessageDialog(this, "Dropping form generation complete for " + selectedStudents.size() + " student(s).", "Generation Complete", JOptionPane.INFORMATION_MESSAGE);
		});
    }
}
