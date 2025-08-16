package lyfjshs.gomis.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType;

import docPrinter.templateManager;
import docPrinter.incidentReport.incidentReportGenerator;
import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.view.appointment.AppointmentManagement;
import lyfjshs.gomis.view.appointment.AppointmentOverview;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import lyfjshs.gomis.view.incident.INCIDENT_fill_up.IncidentFillUpFormPanel;
import lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel;
import lyfjshs.gomis.view.small.S_MainDashboard;
import lyfjshs.gomis.view.students.StudentFullData;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import lyfjshs.gomis.view.students.StudentsListMain;
import lyfjshs.gomis.view.violation.ViewViolationDetails;
import lyfjshs.gomis.view.violation.ViolationResolutionDialog;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;

public class MainDashboard extends Form {

	private static final Logger logger = LogManager.getLogger(MainDashboard.class);
	private Connection connection;
	private JPanel centralTablePanel;
	FlatSVGIcon viewIcon = new FlatSVGIcon("icons/view.svg", 0.4f);
	FlatSVGIcon resolveIcon = new FlatSVGIcon("icons/resolve.svg", 0.4f);
	private ChartPanel chartPanel;
	
	// Existing components for the full layout
	private JScrollPane mainScroll;
	private JPanel holderPane;
	private JPanel actionPanel;
	private JPanel sideRPanel;
	private AppointmentOverview appointmentOverview;
	private S_MainDashboard smallDashboardInstance;

	public MainDashboard(Connection conn) {
		this.connection = conn;
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));

		// Add ComponentListener for continuous resizing updates
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateLayout(Main.gFrame.getWidth());
			}
		});

		// Add HierarchyListener for initial displayability and valid width
		addHierarchyListener(e -> {
			if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && isDisplayable()) {
				// When the component becomes displayable, its size should be stable
				updateLayout(Main.gFrame.getWidth());
			}
		});
		
		// Initial updateLayout() call to ensure proper layout on first display
		// Call with SwingUtilities.invokeLater to ensure component has valid size on initial render
		SwingUtilities.invokeLater(() -> updateLayout(Main.gFrame.getWidth()));
	}


	public void updateLayout(int frameWidth) {
		int currentPanelWidth = frameWidth; // Use the passed frameWidth
		// Only proceed if the width is valid
		if (currentPanelWidth <= 0) {
			return; 
		}


		// Determine appropriate dashboard based on current width
		// Directly compare with FULL_HD breakpoint width (1920 - 270 = 1650)
		boolean isFullLayout = (currentPanelWidth >= 1650); 

		Component currentMainContent = null;
		if (getComponentCount() > 0) {
			currentMainContent = getComponent(0);
		}

		// Determine the *intended* layout type
		String intendedLayoutType = isFullLayout ? "Full Layout" : "Compact Layout";

		// Always remove all components before switching layouts
		removeAll();

		if (isFullLayout) {
			createFullLayout();
			if (chartPanel != null) chartPanel.refreshData();
			refreshTable();
			if (appointmentOverview != null) {
				appointmentOverview.refreshAppointments();
			}
		} else {
			createCompactLayout(); // This method already adds and refreshes the small dashboard
		}

		revalidate();
		repaint();

		// Diagnostic: Log current components in MainDashboard after layout update
		if (getComponentCount() == 0) {
			logger.info("No components in MainDashboard.");
		} else {
			for (int i = 0; i < getComponentCount(); i++) {
				Component c = getComponent(i);
			}
		}
		logger.info("--------------------------------------------");
	}

	private void createFullLayout() {
		// Clear existing components first if any
		removeAll();

setLayout(new MigLayout("fill", "[grow][::300px]", "[grow]"));
		
		mainScroll = new JScrollPane();
		mainScroll.getVerticalScrollBar().setUnitIncrement(16);

		add(mainScroll, "cell 0 0,grow");
		holderPane = new JPanel();
		mainScroll.setViewportView(holderPane);
		holderPane.setLayout(new MigLayout("", "[grow]", "[300px,grow][450px,grow][100px]"));

		// Left side with violations table
		centralTablePanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
		holderPane.add(centralTablePanel, "cell 0 0,grow");

		JLabel lblNewLabel = new JLabel("List of Violations:");
		centralTablePanel.add(lblNewLabel, "cell 0 0");
		lblNewLabel.putClientProperty("FlatLaf.styleClass", "large");
		chartPanel = new ChartPanel(connection);
		holderPane.add(chartPanel, "cell 0 1,grow, pushy");

		actionPanel = new JPanel(new MigLayout("fill", "[grow]", "[]"));
		holderPane.add(actionPanel, "cell 0 2,grow");
		JPanel panel = new JPanel(new MigLayout("", "[grow][][grow][][grow][][grow]", "[][][][][][]"));

		JLabel violationLabel = new JLabel("Session Form");
		violationLabel.putClientProperty("FlatLaf.styleClass", "large");

		JLabel titleLabel = new JLabel("Management");
		titleLabel.putClientProperty("FlatLaf.styleClass", "h1");
		panel.add(titleLabel, "cell 0 0 9 1,growx,aligny center");

		JLabel lblNewLabel_1 = new JLabel("Incident Forms");
		panel.add(lblNewLabel_1, "cell 1 2,alignx center");
		panel.add(violationLabel, "cell 3 2,alignx center");

		JLabel appointmentLabel = new JLabel("Appointment");
		appointmentLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(appointmentLabel, "cell 5 2,alignx center");

		JLabel studentLabel = new JLabel("Student");
		studentLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(studentLabel, "cell 7 2,alignx center");

		FlatButton printINITIAL = new FlatButton();
		printINITIAL.setText("Print INITIAL Incident");
		printINITIAL.setButtonType(ButtonType.none);
		printINITIAL.addActionListener(e -> printInitialForm());
		panel.add(printINITIAL, "flowx,cell 1 3,growx");

		FlatButton createSessionBtn = new FlatButton();
		createSessionBtn.setText("Create a Session");
		createSessionBtn.setButtonType(ButtonType.none);
		createSessionBtn.addActionListener(e -> SessionForm());
		panel.add(createSessionBtn, "cell 3 3,growx");

		FlatButton setAppointment = new FlatButton();
		setAppointment.setText("Set Appointment");
		setAppointment.setButtonType(ButtonType.none);
		setAppointment.addActionListener(e -> createAppointment());
		panel.add(setAppointment, "flowx,cell 5 3,growx");

		FlatButton createIncident = new FlatButton();
		createIncident.setText("Create a Incident Record");
		createIncident.setButtonType(ButtonType.none);
		createIncident.addActionListener(e -> createIncidentRecord());
		panel.add(createIncident, "cell 1 4");

		FlatButton viewAppointments = new FlatButton();
		viewAppointments.setText("View Appointments");
		viewAppointments.setButtonType(ButtonType.none);
		viewAppointments.addActionListener(e -> viewAppointments());
		panel.add(viewAppointments, "cell 5 4,growx");

		FlatButton viewStudents = new FlatButton();
		viewStudents.setText("View Students");
		viewStudents.setButtonType(ButtonType.none);
		viewStudents.addActionListener(e -> {
			StudentsListMain studManagement = new StudentsListMain(connection);
			FormManager.showForm(studManagement);
		});
		panel.add(viewStudents, "flowx,cell 7 3,growx");

		FlatButton searchStudent = new FlatButton();
		searchStudent.setText("Search Student");
		searchStudent.setButtonType(ButtonType.none);
		searchStudent.addActionListener(e -> {
			if (ModalDialog.isIdExist("search")) {
				return;
			}
			StudentSearchPanel searchPanel = new StudentSearchPanel(connection, student -> {
				// Callback when student is selected show
				// its full details using StudentFullData.java
				StudentFullData studentFullData = new StudentFullData(connection, student);
				FormManager.showForm(studentFullData);

				ModalDialog.closeModal("search");
			}, "search") {
				@Override
				protected void onStudentSelected(Student student) {
					// Optional: Implement any specific behavior here
					// This method is called when a student is selected
				}
			};

			ModalDialog.getDefaultOption().setOpacity(0.5f).setAnimationOnClose(true).getBorderOption()
					.setBorderWidth(0.5f).setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

			ModalDialog.showModal(this, searchPanel, "search");
			ModalDialog.getDefaultOption().getLayoutOption().setSize(700, 500);
		});

		panel.add(searchStudent, "cell 7 4");
		actionPanel.add(panel, "grow");

		// Right side with appointments overview
		sideRPanel = new JPanel(new MigLayout("insets 0, gap 0", "[grow]", "[][grow]"));
		sideRPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground")));
		add(sideRPanel, "cell 1 0,grow");

		// Appointments header
		JPanel headerPanel = new JPanel(new MigLayout("insets 10", "[grow]", "[]"));
		JLabel appointmentsLabel = new JLabel("Appointments");
		appointmentsLabel.putClientProperty("FlatLaf.styleClass", "h2");
		headerPanel.add(appointmentsLabel, "grow");
		sideRPanel.add(headerPanel, "growx, wrap");

		// Appointments content
		AppointmentDAO appointmentDAO = new AppointmentDAO(connection);
		appointmentOverview = new AppointmentOverview(appointmentDAO, connection);
		JScrollPane scrollPane = new JScrollPane(appointmentOverview);
		scrollPane.setBorder(null);
		sideRPanel.add(scrollPane, "grow");

		refreshTable();
	}

	private void createCompactLayout() {
		// Clear existing components first if any
		removeAll();
		
		// Reuse or create SMALL_MainDashboard instance
		if (smallDashboardInstance == null) {
			smallDashboardInstance = new S_MainDashboard(connection);
		}
		add(smallDashboardInstance.getMainPanel(), "grow");
		smallDashboardInstance.formRefresh(); // Ensure data is refreshed for small dashboard
	}

	private JScrollPane createTablePanel() {
		String[] columnNames = { "LRN", "Full Name", "Violation Type", "Status", "Actions" };
		Class<?>[] columnTypes = { String.class, String.class, String.class, String.class, Object.class };
		boolean[] editableColumns = { false, false, false, false, true };
		double[] columnWidths = { 0.15, 0.20, 0.17, 0.10, 0.48 };
		int[] alignments = { SwingConstants.CENTER, 
				SwingConstants.LEFT, 
				SwingConstants.LEFT, 
				SwingConstants.CENTER, 
				SwingConstants.CENTER 
		};

		TableActionManager actionsColumn = new DefaultTableActionManager();
		((DefaultTableActionManager)actionsColumn)
			.addAction("View", (t, row) -> {
				String lrn = (String) t.getValueAt(row, 0);
				showViolationDetails(lrn);
			}, new Color(0, 150, 136), viewIcon)
			.addAction("Resolve", (t, row) -> {
				String lrn = (String) t.getValueAt(row, 0);
				resolveViolation(lrn);
			}, new Color(0, 150, 136), resolveIcon);

		Object[][] initialData = new Object[0][columnNames.length];
		GTable table = new GTable(initialData, columnNames, columnTypes, editableColumns, columnWidths, alignments,
				false, 
				actionsColumn);

		// Explicitly set up the Actions column
		((DefaultTableActionManager)actionsColumn).setupTableColumn(table, 4);

		try {
			ViolationDAO ViolationDAO = new ViolationDAO(connection);
			ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
			StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);

			List<Violation> violations = ViolationDAO.getAllViolations();
			logger.info("Found " + violations.size() + " violations"); 

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.setRowCount(0); 

			for (Violation violation : violations) {
				if ("Active".equals(violation.getStatus())) {
					Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
					if (participant != null) {
						

						if (participant.getStudentUid() != null && participant.getStudentUid() > 0) {
							Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
							if (student != null) {
								String fullName = String.format("%s %s",
										student.getStudentFirstname() != null ? student.getStudentFirstname() : "",
										student.getStudentLastname() != null ? student.getStudentLastname() : "")
										.trim();

								model.addRow(new Object[] {
										student.getStudentLrn(),
										fullName,
										violation.getViolationType(),
										violation.getStatus(),
										"actions"
								});
							} else {
								logger.error("Student not found in database for StudentUID: " + participant.getStudentUid());
							}
						} else {
							logger.error("Invalid StudentUID (null or 0) for participant");
						}
					} else {
						logger.error("No participant found for ID: " + violation.getParticipantId());
					}
				} else {
					logger.info("Skipping non-active violation");
				}
			}
		} catch (SQLException e) {
			logger.error("Error loading violations", e);
			JOptionPane.showMessageDialog(this, "Error loading violations: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		return new JScrollPane(table);
	}

	private void showViolationDetails(String lrn) {
		try {
			ViolationDAO violationDAO = new ViolationDAO(connection);
			Violation violation = violationDAO.getViolationByLRN(lrn);

			if (violation != null) {
				String modalId = "violation_details_" + violation.getViolationId();

				if (ModalDialog.isIdExist(modalId)) {
					return;
				}

				ViewViolationDetails detailsPanel = new ViewViolationDetails(connection, violation,
						new StudentsDataDAO(connection), new ParticipantsDAO(connection));

				Option violationDetailsOption = new Option();
				violationDetailsOption.setOpacity(0.3f).setAnimationOnClose(false).getBorderOption()
						.setBorderWidth(0f).setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

				// Get the window ancestor as parent
				Component parent = SwingUtilities.getWindowAncestor(this);
				if (parent == null) {
					parent = Main.gFrame; // Fallback to main frame if window ancestor is null
				}

				ModalDialog.showModal(parent,
						new SimpleModalBorder(detailsPanel, "Violation Details",
								new SimpleModalBorder.Option[] {
										new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION) },
								(controller, action) -> {
									if (action == SimpleModalBorder.CLOSE_OPTION) {
										controller.close();
									}
									refreshTable();
								}),
						violationDetailsOption, 
						modalId);

				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int width = Math.min(800, (int) (screenSize.width * 0.8));
				int height = Math.min(600, (int) (screenSize.height * 0.8));
				violationDetailsOption.getLayoutOption().setSize(width, height);
			}
		} catch (SQLException e) {
			logger.error("Error retrieving violation details", e);
			JOptionPane.showMessageDialog(this, "Error retrieving violation details: " + e.getMessage());
		}
	}

	private void resolveViolation(String lrn) {
		try {
			ViolationDAO violationDAO = new ViolationDAO(connection);
			Violation violation = violationDAO.getViolationByLRN(lrn);

			if (violation == null) {
				JOptionPane.showMessageDialog(this, "Violation not found.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if ("Resolved".equals(violation.getStatus())) {
				JOptionPane.showMessageDialog(this, "This violation has already been resolved.", "Already Resolved",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			// Use the new ModalDialog for resolution
			ViolationResolutionDialog.showModal(
				SwingUtilities.getWindowAncestor(this),
				connection,
				violation,
				() -> {
					// Callback to refresh table after resolution
					refreshTable();
					// Also refresh the global Violation_Record panel if it exists
					if (lyfjshs.gomis.Main.vrList != null) {
						lyfjshs.gomis.Main.vrList.refreshViolations();
					}
				}
			);
		} catch (Exception e) {
			logger.error("Failed to resolve violation", e);
			JOptionPane.showMessageDialog(this, "Failed to resolve violation: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void refreshTable() {
		for (Component component : centralTablePanel.getComponents()) {
			if (component instanceof JScrollPane) {
				centralTablePanel.remove(component);
				break; 
			}
		}

		JScrollPane newTableScrollPane = createTablePanel();
		centralTablePanel.add(newTableScrollPane, "cell 0 1,grow");

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
		newAppointment.setAppointmentDateTime(Timestamp.valueOf(LocalDateTime.now()));
		newAppointment.setGuidanceCounselorId(((GuidanceCounselor) Main.formManager.getCounselorObject()).getGuidanceCounselorId());

		AppointmentDAO appointmentDAO = Main.appointmentCalendar.appointmentDAO;

		AddAppointmentModal.getInstance().showModal(
			connection,
			this,
			new AddAppointmentPanel(newAppointment, appointmentDAO, connection),
			appointmentDAO,
			800,
			600,
			() -> {
				if (appointmentOverview != null) {
					appointmentOverview.refreshAppointments();
				}
				if (Main.appointmentCalendar != null) {
					Main.appointmentCalendar.refreshViews();
				}
			}
		);
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

	@Override
	public void formRefresh() {
		super.formRefresh();
		// Only call updateLayout if the component is displayable and has a valid width
		if (isDisplayable() && Main.gFrame.getWidth() > 0) {
			updateLayout(Main.gFrame.getWidth()); 
		} else {
			// If not displayable yet, ensure layout is updated once it becomes displayable
			addHierarchyListener(new HierarchyListener() {
				@Override
				public void hierarchyChanged(HierarchyEvent e) {
					if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && isDisplayable()) {
						updateLayout(Main.gFrame.getWidth());
						removeHierarchyListener(this); // Remove listener after first update
					}
				}
			});
		}
		if (chartPanel != null) {
			chartPanel.refreshData();
		}
		if (appointmentOverview != null) {
			appointmentOverview.refreshAppointments();
		}
		if (smallDashboardInstance != null) {
			smallDashboardInstance.formRefresh();
		}
	}
}