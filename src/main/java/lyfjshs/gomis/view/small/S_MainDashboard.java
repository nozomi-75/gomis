/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.small;

import java.awt.Component;
import java.io.File;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType;

import docPrinter.templateManager;
import docPrinter.incidentReport.incidentReportGenerator;
import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.view.ChartPanel;
import lyfjshs.gomis.view.HORIZONTAL_AppointmentsOverview;
import lyfjshs.gomis.view.appointment.AppointmentManagement;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import lyfjshs.gomis.view.incident.INCIDENT_fill_up.IncidentFillUpFormPanel;
import lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel;
import lyfjshs.gomis.view.students.StudentFullData;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import lyfjshs.gomis.view.students.StudentsListMain;
import lyfjshs.gomis.view.violation.ViolationTablePanel;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;

public class S_MainDashboard extends Form {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(S_MainDashboard.class);
	private JPanel mainPanel;
	private JPanel appointmntPanel;
	private JPanel centralTablePanel;	
	private Connection connection;
	private JPanel actionPanel;
	private ChartPanel chartPanel;
	private JLabel appointmentsHeaderLabel;
	private JLabel violationsHeaderLabel;
	private JLabel managementHeaderLabel;
	FlatSVGIcon viewIcon = new FlatSVGIcon("icons/view.svg", 0.4f);
	FlatSVGIcon resolveIcon = new FlatSVGIcon("icons/resolve.svg", 0.4f);
	/**
	 * S means SMALL, for smaller screens
	 */
	public S_MainDashboard(Connection conn) {
		this.connection = conn;
		
		setComponents();
		
		refreshTable();
	}

	private void setComponents() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));
		mainPanel = new JPanel(new MigLayout("", "[760,grow]", "[]20[]20[]20[][]"));
		mainPanel.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "cell 0 0,alignx center,growy");
		
		AppointmentDAO appointmentDAO = new AppointmentDAO(connection);

		// Appointments Section
		appointmntPanel = new JPanel(new MigLayout("", "[grow]", "[][]"));
		appointmntPanel.setOpaque(false);
		appointmentsHeaderLabel = new JLabel("Active Appointments");
		appointmentsHeaderLabel.putClientProperty("FlatLaf.styleClass", "h1");
		appointmntPanel.add(appointmentsHeaderLabel, "cell 0 0, gapbottom 10");
		HORIZONTAL_AppointmentsOverview appointmentOverview = new HORIZONTAL_AppointmentsOverview(appointmentDAO, connection);
		appointmntPanel.add(appointmentOverview, "cell 0 1,grow");
		mainPanel.add(appointmntPanel, "growx, wrap");

		// Violations Section
		centralTablePanel = new JPanel(new MigLayout("fill", "[grow]", "[][grow]"));
		centralTablePanel.setOpaque(false);
		violationsHeaderLabel = new JLabel("Active Violations");
		violationsHeaderLabel.putClientProperty("FlatLaf.styleClass", "h1");
		centralTablePanel.add(violationsHeaderLabel, "cell 0 0,growx, gapbottom 10");
		mainPanel.add(centralTablePanel, "grow,wrap");

		// Chart Section
		chartPanel = new ChartPanel(connection);
		mainPanel.add(chartPanel, "grow,wrap");

		// Management/Actions Section
		actionPanel = new JPanel(new MigLayout("fill, insets 10 0 0 0", "[grow]", "[]"));
		actionPanel.setOpaque(false);
		JPanel managementPanel = new JPanel(new MigLayout("", "[grow][][grow][][grow][][grow][][grow]", "[][][][][][]"));
		managementPanel.setOpaque(false);

		managementHeaderLabel = new JLabel("Management");
		managementHeaderLabel.putClientProperty("FlatLaf.styleClass", "h1");
		managementPanel.add(managementHeaderLabel, "cell 0 0 9 1,growx,aligny center, gapbottom 10");

		JLabel incidentFormsLabel = new JLabel("Incident Forms");
		incidentFormsLabel.putClientProperty("FlatLaf.styleClass", "large");
		managementPanel.add(incidentFormsLabel, "cell 1 2,alignx center");

		JLabel sessionFormLabel = new JLabel("Session Form");
		sessionFormLabel.putClientProperty("FlatLaf.styleClass", "large");
		managementPanel.add(sessionFormLabel, "cell 3 2,alignx center");

		JLabel appointmentLabel = new JLabel("Appointment");
		appointmentLabel.putClientProperty("FlatLaf.styleClass", "large");
		managementPanel.add(appointmentLabel, "cell 5 2,alignx center");

		JLabel studentLabel = new JLabel("Student");
		studentLabel.putClientProperty("FlatLaf.styleClass", "large");
		managementPanel.add(studentLabel, "cell 7 2,alignx center");

		// Action Buttons
		FlatButton printINITIAL = new FlatButton();
		printINITIAL.setText("Print INITIAL Incident");
		printINITIAL.setButtonType(ButtonType.none);
		printINITIAL.addActionListener(e -> printInitialForm());
		managementPanel.add(printINITIAL, "flowx,cell 1 3,growx");

		FlatButton createSessionBtn = new FlatButton();
		createSessionBtn.setText("Create a Session");
		createSessionBtn.setButtonType(ButtonType.none);
		createSessionBtn.addActionListener(e -> SessionForm());
		managementPanel.add(createSessionBtn, "cell 3 3,growx");

		FlatButton setAppointment = new FlatButton();
		setAppointment.setText("Set Appointment");
		setAppointment.setButtonType(ButtonType.none);
		setAppointment.addActionListener(e -> createAppointment());
		managementPanel.add(setAppointment, "flowx,cell 5 3,growx");

		FlatButton createIncident = new FlatButton();
		createIncident.setText("Create a Incident Record");
		createIncident.setButtonType(ButtonType.none);
		createIncident.addActionListener(e -> createIncidentRecord());
		managementPanel.add(createIncident, "cell 1 4");

		FlatButton viewAppointments = new FlatButton();
		viewAppointments.setText("View Appointments");
		viewAppointments.setButtonType(ButtonType.none);
		viewAppointments.addActionListener(e -> viewAppointments());
		managementPanel.add(viewAppointments, "cell 5 4,growx");

		FlatButton viewStudents = new FlatButton();
		viewStudents.setText("View Students");
		viewStudents.setButtonType(ButtonType.none);
		viewStudents.addActionListener(e -> {
			StudentsListMain studManagement = new StudentsListMain(connection);
			FormManager.showForm(studManagement);
		});
		managementPanel.add(viewStudents, "flowx,cell 7 3,growx");

		FlatButton searchStudent = new FlatButton();
		searchStudent.setText("Search Student");
		searchStudent.setButtonType(ButtonType.none);
		searchStudent.addActionListener(e -> {
			if (ModalDialog.isIdExist("search")) {
				return;
			}
			StudentSearchPanel searchPanel = new StudentSearchPanel(connection, student -> {
				StudentFullData studentFullData = new StudentFullData(connection, student);
				FormManager.showForm(studentFullData);
				ModalDialog.closeModal("search");
			}, "search") {
				@Override
				protected void onStudentSelected(Student student) {
					// Optional: Implement any specific behavior here
				}
			};
			ModalDialog.getDefaultOption().setOpacity(0.5f).setAnimationOnClose(true).getBorderOption()
				.setBorderWidth(0.5f).setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);
			ModalDialog.showModal(this, searchPanel, "search");
			ModalDialog.getDefaultOption().getLayoutOption().setSize(700, 500);
		});
		managementPanel.add(searchStudent, "cell 7 4");
		actionPanel.add(managementPanel, "grow");
		mainPanel.add(actionPanel, "growx,aligny bottom,wrap");
	}

	private void refreshTable() {
		// Remove the existing table from the centralTablePanel
		for (Component component : centralTablePanel.getComponents()) {
			if (component instanceof JScrollPane || component instanceof ViolationTablePanel) {
				centralTablePanel.remove(component);
				break; // Exit after removing the first table component
			}
		}

		// Create and add the new ViolationTablePanel
		ViolationDAO violationDAO = new ViolationDAO(connection);
		ViolationTablePanel violationTablePanel = new ViolationTablePanel(connection, violationDAO);
		centralTablePanel.add(violationTablePanel, "cell 0 1,grow");

		// Revalidate and repaint the panel
		centralTablePanel.revalidate();
		centralTablePanel.repaint();
	}

	private void SessionForm() {
		try {
			SessionsFillUpFormPanel SessionsFillUpFormPanel = new SessionsFillUpFormPanel(connection);
			FormManager.showForm(SessionsFillUpFormPanel);
		} catch (Exception e) {
			logger.error("Error opening Sessions form", e);
			JOptionPane.showMessageDialog(this, "Error opening Sessions form: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void viewAppointments() {
		try {
			AppointmentManagement appointments = new AppointmentManagement(connection);
			FormManager.showForm(appointments);
		} catch (Exception e) {
			logger.error("Error opening Appointments view", e);
			JOptionPane.showMessageDialog(this, "Error opening Appointments view: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void createAppointment() {
		Appointment newAppointment = new Appointment();
		newAppointment.setAppointmentDateTime(Timestamp.valueOf(LocalDateTime.now())); // Set default date and time
		AppointmentDAO appointmentDAO = Main.appointmentCalendar.appointmentDAO;

		// Set the guidanceCounselorId from FormManager
		newAppointment.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());

		AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(newAppointment, appointmentDAO, connection);

		// Use AddAppointmentModal to show the dialog with correct size
		AddAppointmentModal.getInstance().showModal(connection, this, addAppointmentPanel, appointmentDAO, 800, // Increased
																								// width
				600, // Increased height
				() -> Main.appointmentCalendar.refreshViews());
	}

	private void printInitialForm() {
		try {
			// Use docPrinter incident report system instead of Jasper
			incidentReportGenerator generator = new incidentReportGenerator();
			File outputFolder = templateManager.getDefaultOutputFolder();
			
			boolean success = generator.generateIncidentReport(outputFolder, null, "print");
			
			if (success) {
				logger.info("Incident report template generated successfully");
			} else {
				logger.error("Failed to generate incident report template");
				JOptionPane.showMessageDialog(this, "Failed to generate incident report template", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			logger.error("Error generating incident report template", e);
			JOptionPane.showMessageDialog(this, "Error generating incident report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void createIncidentRecord() {
		IncidentFillUpFormPanel incidentFillUp = new IncidentFillUpFormPanel(connection);
		FormManager.showForm(incidentFillUp);
	}

	// Add method to refresh table when dashboard is shown
	@Override
	public void formRefresh() {
		logger.info("[SMALL_MainDashboard] formRefresh called.");
		refreshTable();
		if (chartPanel != null) {
			chartPanel.refreshData();
		}
	}

	private void refreshStateOnResize() {
        // 3. Revalidate and repaint to update layout
        this.removeAll();
        //ADD AGAIN ALL COMPONENTS
        setComponents();

        this.revalidate();
        this.repaint();

    }

    @Override
    public void onParentFrameResized(int width, int height) {
        logger.debug(" resized to: {}x{}", width, height);
        super.onParentFrameResized(width, height);
		refreshStateOnResize();
    }

}
