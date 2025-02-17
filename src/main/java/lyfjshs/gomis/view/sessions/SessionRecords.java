package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.model.Session;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;

public class SessionRecords extends Form {

    private static final long serialVersionUID = 1L;
    private JTable sessionTable;
    private SessionsDAO sessionsDAO;
    private final Connection connection;
    private final String[] columnNames = { "#", "Session ID", "Session Type", "Participants", "Date & Time", "Status", "Actions" };
    private SlidePane slidePane;
    private JButton backBtn;
    private JPanel mainPanel;
    private JPanel sessionFullDataPanel; // Panel to show session details

    public SessionRecords(Connection conn) {
        this.connection = conn;
        sessionsDAO = new SessionsDAO(conn);
        initializeComponents();
        setupLayout();
        loadSessionData();
    }

    private void initializeComponents() {
        // Initialize table
        DefaultTableModel model = new DefaultTableModel(null, columnNames);
        sessionTable = new JTable(model);
        setupTable();

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
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        });

        // Add the main panel as the first slide
        slidePane.addSlide(mainPanel, SlidePaneTransition.Type.FORWARD);
    }

    private void setupTable() {
        sessionTable.setShowVerticalLines(false);
        sessionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sessionTable.setRowHeight(30);
        sessionTable.setFont(new Font("Tahoma", Font.PLAIN, 14));
        sessionTable.setShowGrid(false);

        // Set column widths
        sessionTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // #
        sessionTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Session ID
        sessionTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Session Type
        sessionTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Participants
        sessionTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Date & Time
        sessionTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Status
        sessionTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Actions
        sessionTable.getColumnModel().getColumn(6).setResizable(false);

        // Setup table actions
        setupTableActions();
    }

    private void setupTableActions() {
        TableActionManager actionManager = new TableActionManager();
        actionManager.addAction("View", (table, row) -> {
            int sessionId = (int) table.getValueAt(row, 1);
            FlatAnimatedLafChange.showSnapshot();

            try {
                Session session = sessionsDAO.getSessionById(sessionId);
                showSessionFullData(session);
                backBtn.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error retrieving session data: " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.5f));

        actionManager.applyTo(sessionTable, 6);
    }

    private void showSessionFullData(Session session) {
        // Create the SessionFullData panel
        sessionFullDataPanel = new SessionFullData(session);
        
        // Add the SessionFullData panel to the SlidePane
        slidePane.addSlide(sessionFullDataPanel, SlidePaneTransition.Type.FORWARD);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Create header panel
        JPanel headerPanel = new JPanel(new MigLayout("", "[][grow][][]", "[grow]"));
        JLabel headerLabel = new JLabel("SESSION RECORDS");
        headerLabel.setFont(new Font("Tahoma", Font.BOLD, 16));

        headerPanel.add(headerLabel, "flowx,cell 1 0,alignx center,growy");
        headerPanel.add(backBtn, "cell 3 0");

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
            List<Session> sessions = sessionsDAO.getSessionDataWithParticipantCount();
            DefaultTableModel model = (DefaultTableModel) sessionTable.getModel();
            model.setRowCount(0);

            int rowNum = 1;
            for (Session session : sessions) {
                model.addRow(new Object[] { 
                    rowNum++, 
                    session.getSessionId(),
                    session.getSessionType(),
                    session.getParticipantCount(),
                    session.getAppointmentDateTime(),
                    session.getSessionStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading session data: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> {
    //         try {
    //             JFrame frame = new JFrame("Session Records");
    //             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //             frame.getContentPane().add(new SessionRecords(DBConnection.getConnection()));
    //             frame.setSize(800, 600);
    //             frame.setLocationRelativeTo(null);
    //             frame.setVisible(true);
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //             JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), 
    //                 "Database Error", JOptionPane.ERROR_MESSAGE);
    //         }
    //     });
    // }
}