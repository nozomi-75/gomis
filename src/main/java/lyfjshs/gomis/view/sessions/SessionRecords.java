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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.core.animation.timing.KeyFrames.Frame;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;
import lyfjshs.gomis.components.DrawerBuilder;

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
        sessionsDAO = new SessionsDAO(conn);
        initializeComponents();
        setupLayout();
        loadSessionData();
    }

    private void initializeComponents() {
        setupTable(); // Initialize GTable

        // Initialize SlidePane
        slidePane = new SlidePane();
        slidePane.setOpaque(true);

        // Create main panel that will contain the table
        mainPanel = createSessionTablePanel();

        // Initialize back button
        backBtn = new JButton("Back");
        backBtn.setVisible(false);
        backBtn.addActionListener(e -> {
            FlatAnimatedLafChange.showSnapshot();
            slidePane.addSlide(mainPanel, SlidePaneTransition.Type.BACK);
            backBtn.setVisible(false);
            addSessionBtn.setVisible(true); // Show Add Session button when returning
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        });

        // Initialize add session button
        addSessionBtn = new JButton("Add Session");
        addSessionBtn.addActionListener(e -> openAddSessionForm());
        
        // Initialize search session button
        searchSessionBtn = new JButton("Search Session");
        searchSessionBtn.addActionListener(e -> openSearchSessionDialog()); // Add action listener for search button
        
        slidePane.addSlide(mainPanel, SlidePaneTransition.Type.FORWARD);
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

        TableActionManager actionManager = setupTableActions();

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

    private TableActionManager setupTableActions() {
        TableActionManager actionManager = new TableActionManager();
        actionManager.addAction("View", (table, row) -> {
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

        return actionManager;
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
            sessionFullDataPanel = new SessionFullData(session, counselor, participants);
            slidePane.addSlide(sessionFullDataPanel, SlidePaneTransition.Type.FORWARD);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error displaying session details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Create header panel
        JPanel headerPanel = new JPanel(new MigLayout("", "[][grow][][]", "[grow]"));
        JLabel headerLabel = new JLabel("SESSION RECORDS");
        headerLabel.setFont(new Font("Tahoma", Font.BOLD, 16));

        headerPanel.add(headerLabel, "flowx,cell 1 0,alignx center,growy");
        headerPanel.add(addSessionBtn, "cell 2 0");
        headerPanel.add(searchSessionBtn, "cell 3 0");
        headerPanel.add(backBtn, "cell 4 0");

        // Add components to main frame
        add(headerPanel, BorderLayout.NORTH);
        add(slidePane, BorderLayout.CENTER);
    }

    private JPanel createSessionTablePanel() {
        JScrollPane scrollPane = new JScrollPane(sessionTable);
        JPanel panel = new JPanel(new MigLayout("", "[grow]", "[grow]"));
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
}