package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;

public class ViolationTablePanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(ViolationTablePanel.class);
    private GTable violationTable;
    private ViolationDAO violationDAO;
    private Connection connect;
    private List<Violation> violations;

    public ViolationTablePanel(Connection conn, ViolationDAO violationDAO) {
        this.connect = conn;
        this.violationDAO = violationDAO;
        this.violations = new ArrayList<>();
        setLayout(new BorderLayout());
        initializeTable();
        refreshData();
    }

    private void initializeTable() {
        // Define table structure
        String[] columnNames = { "LRN", "Violation Type", "Reinforcement", "Status", "Actions" };
        Class<?>[] columnTypes = { String.class, String.class, String.class, String.class, Object.class };
        boolean[] editableColumns = { false, false, false, false, true };
        double[] columnWidths = { 0.15, 0.25, 0.2, 0.15, 0.25 };
        int[] alignments = { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER };

        // Create action manager for view and resolve actions
        TableActionManager actionManager = new DefaultTableActionManager() {
            @Override
            public void setupTableColumn(GTable table, int actionColumnIndex) {
                // Delegate to the default implementation of DefaultTableActionManager
                super.setupTableColumn(table, actionColumnIndex);
            }
        };

        ((DefaultTableActionManager)actionManager)
            .addAction("View", (table, row) -> {
                viewViolation(row);
            }, new Color(0, 123, 255), null) // Set icon to null or a valid icon if available
            .addAction("Resolve", (table, row) -> {
                Violation violation = getViolationAt(row);
                if (violation != null) {
                    resolveViolation(violation);
                }
            }, new Color(46, 204, 113), null); // Set icon to null or a valid icon if available

        // Create table
        violationTable = new GTable(new Object[0][5], columnNames, columnTypes, editableColumns, columnWidths,
                alignments, false, actionManager);

        // Explicitly set up the Actions column using the actionManager
        actionManager.setupTableColumn(violationTable, 4);



        // Add table to scroll pane and panel
        JScrollPane scrollPane = new JScrollPane(violationTable);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            violations = violationDAO.getAllViolations();
            logger.info("Fetched {} violations from DAO", violations != null ? violations.size() : 0);
            updateTableWithViolations();
        } catch (SQLException e) {
            logger.error("Error loading violations: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading violations: " + e.getMessage());
        }
    }

    private void updateTableWithViolations() {
        DefaultTableModel model = (DefaultTableModel) violationTable.getModel();
        model.setRowCount(0);

        for (int i = 0; i < violations.size(); i++) {
            Violation violation = violations.get(i);
            
            // Add null checks to prevent NullPointerException
            if (violation.getParticipant() == null || violation.getParticipant().getStudent() == null) {
                // Skip violations with missing participant or student data
                logger.warn("Skipping violation {} - missing participant or student data (participant: {}, student: {})", 
                    violation.getViolationId(), violation.getParticipant(), 
                    violation.getParticipant() != null ? violation.getParticipant().getStudent() : null);
                continue;
            }
            
            model.addRow(new Object[] {
                violation.getParticipant().getStudent().getStudentLrn(),
                violation.getViolationType(),
                violation.getReinforcement(),
                violation.getStatus(),
                null // Actions column
            });
            // Store violation object in table
            violationTable.putClientProperty("violation_" + i, violation);
        }
        logger.info("Displayed {} violations in table", model.getRowCount());
    }

    private Violation getViolationAt(int row) {
        if (row >= 0 && row < violations.size()) {
            return violations.get(row);
        }
        return null;
    }

    private void viewViolation(int row) {
        Violation violation = getViolationAt(row);
        if (violation != null) {
            ViewViolationDetails.showDialog(
                SwingUtilities.getWindowAncestor(this),
                connect,
                violation,
                new StudentsDataDAO(connect),
                new ParticipantsDAO(connect)
            );
        }
    }

    private void resolveViolation(Violation violation) {
        if (violation != null) {
            // Use the static showModal method from ViolationResolutionDialog
            ViolationResolutionDialog.showModal(
                SwingUtilities.getWindowAncestor(this),
                connect,
                violation,
                () -> {
                    // Callback to refresh data after resolution
                    refreshData();
                }
            );
        }
    }

    public GTable getTable() {
        return violationTable;
    }
}