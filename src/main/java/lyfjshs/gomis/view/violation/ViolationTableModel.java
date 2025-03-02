package lyfjshs.gomis.view.violation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import lyfjshs.gomis.components.table.TableActionManager;

public class ViolationTableModel extends DefaultTableModel {
    private static final String[] COLUMN_NAMES = {
        "Student LRN", "Name", "Violation Type", "Reinforcement", "Status", "Actions"
    };
    
    private List<ViolationRecord> violations; // Store violations for action handling
    private TableActionManager actionManager;
    private Connection connection;

    public ViolationTableModel(Connection connection) {
        super(COLUMN_NAMES, 0);
        this.connection = connection;
        initializeActionManager();
    }

    private void initializeActionManager() {
        actionManager = new TableActionManager();
        
        // View action
        actionManager.addAction("View", 
            (table, row) -> handleViewAction(row),
            new Color(41, 128, 185), // Blue
            new FlatSVGIcon("icons/view.svg")
        );
        
        // Resolve action
        actionManager.addAction("Resolve", 
            (table, row) -> handleResolveAction(row),
            new Color(46, 204, 113), // Green
            new FlatSVGIcon("icons/check.svg")
        );
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 5; // Only "Actions" column is editable
    }

    public void loadViolations(List<ViolationRecord> violations) {
        this.violations = violations; // Store violations
        setRowCount(0);
        for (ViolationRecord violation : violations) {
            addViolationRow(violation);
        }
    }

    private void addViolationRow(ViolationRecord violation) {
        try {
            StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
            Student student = studentsDataDAO.getStudentById(violation.getParticipantId());
            String studentLRN = student != null ? student.getStudentLrn() : "N/A";
            String fullName = student != null ? String.format("%s %s", student.getStudentFirstname(), student.getStudentLastname()) : "N/A";
            
            Object[] rowData = {
                studentLRN,
                fullName,
                violation.getViolationType(),
                violation.getReinforcement(),
                violation.getStatus(),
                "" // Actions column - will be handled by TableActionManager
            };
            addRow(rowData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void applyActionsToTable(JTable table) {
        actionManager.applyTo(table, 5); // Apply to Actions column (index 5)
    }

    // Action handlers
    private void handleViewAction(int row) {
        if (row >= 0 && row < violations.size()) {
            ViolationRecord violation = violations.get(row);
            // Implement view logic or notify listeners
            if (actionListener != null) {
                actionListener.onViewViolation(violation);
            }
        }
    }

    private void handleResolveAction(int row) {
        if (row >= 0 && row < violations.size()) {
            ViolationRecord violation = violations.get(row);
            // Implement resolve logic or notify listeners
            if (actionListener != null) {
                actionListener.onResolveViolation(violation);
            }
        }
    }

    // Action listener interface
    public interface ViolationActionListener {
        void onViewViolation(ViolationRecord violation);
        void onResolveViolation(ViolationRecord violation);
    }

    private ViolationActionListener actionListener;

    public void setActionListener(ViolationActionListener listener) {
        this.actionListener = listener;
    }

    // Helper method to get violation at specific row
    public ViolationRecord getViolationAt(int row) {
        return violations != null && row >= 0 && row < violations.size() ? 
            violations.get(row) : null;
    }
}