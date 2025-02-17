package lyfjshs.gomis.view.violation;

import java.awt.Color;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.model.Violation;
import lyfjshs.gomis.components.table.TableActionManager;

public class ViolationTableModel extends DefaultTableModel {
    private static final String[] COLUMN_NAMES = {
        "Violation ID", "Student LRN", "Name", "Violation Type", "Status", "Actions"
    };
    
    private List<Violation> violations; // Store violations for action handling
    private TableActionManager actionManager;

    public ViolationTableModel() {
        super(COLUMN_NAMES, 0);
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

    public void loadViolations(List<Violation> violations) {
        this.violations = violations; // Store violations
        setRowCount(0);
        for (Violation violation : violations) {
            addViolationRow(violation);
        }
    }

    private void addViolationRow(Violation violation) {
        String studentLRN = violation.getStudentLRN() != null ? 
            violation.getStudentLRN() : "N/A";
        
        Object[] rowData = {
            violation.getViolationId(),
            studentLRN,
            String.format("%s %s", violation.getFIRST_NAME(), violation.getLAST_NAME()),
            violation.getViolationType(),
            violation.getStatus(),
            "" // Actions column - will be handled by TableActionManager
        };
        addRow(rowData);
    }

    public void applyActionsToTable(JTable table) {
        actionManager.applyTo(table, 5); // Apply to Actions column (index 5)
    }

    // Action handlers
    private void handleViewAction(int row) {
        if (row >= 0 && row < violations.size()) {
            Violation violation = violations.get(row);
            // Implement view logic or notify listeners
            if (actionListener != null) {
                actionListener.onViewViolation(violation);
            }
        }
    }

    private void handleResolveAction(int row) {
        if (row >= 0 && row < violations.size()) {
            Violation violation = violations.get(row);
            // Implement resolve logic or notify listeners
            if (actionListener != null) {
                actionListener.onResolveViolation(violation);
            }
        }
    }

    // Action listener interface
    public interface ViolationActionListener {
        void onViewViolation(Violation violation);
        void onResolveViolation(Violation violation);
    }

    private ViolationActionListener actionListener;

    public void setActionListener(ViolationActionListener listener) {
        this.actionListener = listener;
    }

    // Helper method to get violation at specific row
    public Violation getViolationAt(int row) {
        return violations != null && row >= 0 && row < violations.size() ? 
            violations.get(row) : null;
    }
} 