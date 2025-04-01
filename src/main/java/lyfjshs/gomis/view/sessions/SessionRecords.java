package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;
import lyfjshs.gomis.components.table.DefaultTableActionManager;

public class SessionRecords extends Form {

    private static final long serialVersionUID = 1L;
    private GTable sessionTable; // Use GTable instead of JTable
    private SessionsDAO sessionsDAO;
    private final Connection connection;
    private SlidePane slidePane;
    private JButton backBtn;
    private JPanel mainPanel;
    private JPanel sessionFullDataPanel; // Panel to show session details
    private JButton addSessionBtn;
    private JButton searchSessionBtn;

    public SessionRecords(Connection conn) {
        this.connection = conn;
        this.sessionsDAO = new SessionsDAO(conn);
        
        // Set layout first
        setLayout(new BorderLayout());
        
        // Initialize components in order
        initializeComponents();
        
        // Create header panel
        JPanel headerPanel = new JPanel(new MigLayout("", "[][grow][][]", "[grow]"));
        JLabel headerLabel = new JLabel("SESSION RECORDS");
        headerLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        
        // Initialize buttons
        addSessionBtn = new JButton("Add Session");
        addSessionBtn.addActionListener(e -> openAddSessionForm());
        
        searchSessionBtn = new JButton("Search Session");
        searchSessionBtn.addActionListener(e -> openSearchSessionDialog());
        
        backBtn = new JButton("Back");
        backBtn.setVisible(false);
        
        // Add components to header
        headerPanel.add(headerLabel, "cell 1 0,alignx center,growy");
        headerPanel.add(addSessionBtn, "cell 2 0");
        headerPanel.add(searchSessionBtn, "cell 3 0");
        headerPanel.add(backBtn, "cell 4 0");
        
        // Create main content panel
        mainPanel = createSessionTablePanel();
        
        // Initialize and setup SlidePane
        slidePane = new SlidePane();
        slidePane.setOpaque(true);
        slidePane.addSlide(mainPanel, SlidePaneTransition.Type.FORWARD);
        
        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(slidePane, BorderLayout.CENTER);
        
        // Setup back button action
        backBtn.addActionListener(e -> {
            FlatAnimatedLafChange.showSnapshot();
            slidePane.addSlide(mainPanel, SlidePaneTransition.Type.BACK);
            backBtn.setVisible(false);
            addSessionBtn.setVisible(true);
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        });
        
        // Load initial data
        loadSessionData();
    }

    private void initializeComponents() {
        // Initialize table first
        setupTable();
        
        // Initialize panels with proper parent relationships
        sessionFullDataPanel = new JPanel(new BorderLayout());
        mainPanel = new JPanel(new MigLayout("", "[grow]", "[grow]"));
        
        // Set proper background colors and opacity
        mainPanel.setOpaque(true);
        mainPanel.setBackground(UIManager.getColor("Panel.background"));
        sessionFullDataPanel.setOpaque(true);
        sessionFullDataPanel.setBackground(UIManager.getColor("Panel.background"));
    }

    private void openAddSessionForm() {
        try {
            DrawerBuilder.switchToSessionsForm();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error opening session form: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupTable() {
        Object[][] initialData = new Object[0][7]; // Adjusted for new column structure
        String[] columnNames = { "#", "Appointment Type", "Consultation Type", "Date", "Status", "Participants", "Actions" };
        Class<?>[] columnTypes = { Integer.class, String.class, String.class, String.class, String.class, Integer.class, Object.class };
        boolean[] editableColumns = { false, false, false, false, false, false, true };
        double[] columnWidths = { 0.05, 0.2, 0.2, 0.2, 0.15, 0.1, 0.1 }; // Sum to 1.0
        int[] alignments = {
            SwingConstants.CENTER,  // #
            SwingConstants.LEFT,    // Appointment Type
            SwingConstants.LEFT,    // Consultation Type
            SwingConstants.CENTER,  // Date
            SwingConstants.CENTER,  // Status
            SwingConstants.CENTER,  // Participants
            SwingConstants.CENTER   // Actions
        };

        TableActionManager actionManager = new DefaultTableActionManager();
        ((DefaultTableActionManager)actionManager).addAction("View", (table, row) -> {
            // Get the actual session ID from the first column
            int sessionId = (int) table.getValueAt(row, 0);
            FlatAnimatedLafChange.showSnapshot();

            try {
                Sessions session = sessionsDAO.getSessionById(sessionId);
                if (session != null) {
                    showSessionFullData(session);
                    backBtn.setVisible(true);
                    addSessionBtn.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Session ID " + sessionId + " not found in database.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error retrieving session data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.5f));

        sessionTable = new GTable(
            initialData,
            columnNames,
            columnTypes,
            editableColumns,
            columnWidths,
            alignments,
            false, // No checkbox column
            actionManager
        );
    }

    private void showSessionFullData(Sessions session) {
        try {
            if (session == null) {
                throw new IllegalArgumentException("Session cannot be null");
            }

            // Get counselor from FormManager instead of database
            GuidanceCounselor counselor = Main.formManager.getCounselorObject();
            if (counselor == null) {
                throw new IllegalStateException("No counselor is currently logged in");
            }

            // Get participants for this session
            List<Participants> participants = new ArrayList<>();
            if (session.getSessionId() > 0) {
                participants = sessionsDAO.getParticipantsBySessionId(session.getSessionId());
            }

            // Create session details panel
            sessionFullDataPanel = new SessionFullData(session, counselor, participants, connection);
            slidePane.addSlide(sessionFullDataPanel, SlidePaneTransition.Type.FORWARD);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error displaying session details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createSessionTablePanel() {
        if (sessionTable == null) {
            setupTable(); // Ensure table is initialized
        }
        
        JScrollPane scrollPane = new JScrollPane(sessionTable);
        JPanel panel = new JPanel(new MigLayout("", "[grow]", "[grow]"));
        panel.setOpaque(true);
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.add(scrollPane, "cell 0 0,grow");
        return panel;
    }

    private void loadSessionData() {
        try {
            List<Sessions> sessions = sessionsDAO.getSessionDataWithParticipantCount();
            DefaultTableModel model = (DefaultTableModel) sessionTable.getModel();
            model.setRowCount(0);

            for (Sessions session : sessions) {
                model.addRow(new Object[] { 
                    session.getSessionId(), // Use actual session ID instead of row number
                    session.getAppointmentType(),
                    session.getConsultationType(),
                    session.getSessionDateTime(),
                    session.getSessionStatus(),
                    session.getParticipantCount(),
                    null // Actions column handled by TableActionManager
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading session data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openSearchSessionDialog() {
        try {
            // Create a new dialog for the session search panel
            JDialog searchDialog = new JDialog();
            searchDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            searchDialog.setSize(400, 300); // Set the size of the dialog
            searchDialog.setLocationRelativeTo(this); // Center the dialog relative to the parent

            // Create an instance of SessionSearchPanel
            SessionSearchPanel searchPanel = new SessionSearchPanel();
            searchDialog.add(searchPanel); // Add the search panel to the dialog

            // Make the dialog visible
            searchDialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace for debugging
            JOptionPane.showMessageDialog(this, "Error opening search dialog: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}