package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;

public class ViolationTablePanel extends JPanel {
    private GTable table; // Use GTable instead of JTable
    private ViolationCRUD violationCRUD;
    private Connection connect;
    private List<ViolationRecord> violations; // Store violations for action handling
    private TableActionManager actionManager;

    public ViolationTablePanel(Connection conn, ViolationCRUD violationCRUD) {
        this.connect = conn;
        this.violationCRUD = violationCRUD;
        this.violations = new ArrayList<>();
        initializePanel();
        initializeTable();
        refreshData();

    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private TableActionManager setupTableActions() {
        TableActionManager actionManager = new TableActionManager();

        actionManager.addAction("View", (table, row) -> {
            ViolationRecord violation = getViolationAt(row);
            FlatAnimatedLafChange.showSnapshot();
            // show violation details
            if (violation != null) {
                ViewViolationDetails viewDialog = new ViewViolationDetails(
                        (JFrame) SwingUtilities.getWindowAncestor(this),
                        violation,
                        new StudentsDataDAO(connect),
                        new ParticipantsDAO(connect));
                viewDialog.setVisible(true);
            }
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.5f));

        actionManager.addAction("Resolve",
                (t, row) -> handleResolveAction(row),
                new Color(46, 204, 113),
                new com.formdev.flatlaf.extras.FlatSVGIcon("icons/resolve.svg", 0.5f));
        return actionManager;
    }

    private void initializeTable() {
        String[] columnNames = { "Student LRN", "Name", "Violation Type", "Reinforcement", "Status", "Actions" };
        Class<?>[] columnTypes = { String.class, String.class, String.class, String.class, String.class, Object.class };
        boolean[] editableColumns = { false, false, false, false, false, true };
        double[] columnWidths = { 0.15, 0.25, 0.20, 0.25, 0.10, 0.04 }; // Adjusted widths for better spacing
        int[] alignments = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT,
                SwingConstants.CENTER, SwingConstants.CENTER };

        actionManager = setupTableActions();

        table = new GTable(
                new Object[0][columnNames.length],
                columnNames,
                columnTypes,
                editableColumns,
                columnWidths,
                alignments,
                false, // No checkbox column
                actionManager);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void handleResolveAction(int row) {
        ViolationRecord violation = getViolationAt(row);
        if (violation != null) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to resolve this violation?",
                "Confirm Resolution",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (violationCRUD.updateViolationStatus(violation.getViolationId(), "RESOLVED")) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Violation has been resolved successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        refreshData(); // Refresh the table to show updated status
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Failed to resolve violation.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    showError("Error resolving violation", e);
                }
            }
        }
    }

    public void refreshData() {
        try {
            loadViolations(violationCRUD.getAllViolations());
        } catch (Exception e) {
            showError("Error loading violation data", e);
        }
    }

    public void searchViolations(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                refreshData();
                return;
            }
            loadViolations(violationCRUD.searchViolations(searchTerm));
        } catch (Exception e) {
            showError("Error searching violations", e);
        }
    }

    private void loadViolations(List<ViolationRecord> violationList) {
        this.violations = violationList;
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.setRowCount(0); // Clear existing rows

        StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connect);
        for (ViolationRecord violation : violations) {
            try {
                Student student = studentsDataDAO.getStudentById(violation.getParticipantId());
                String studentLRN = student != null ? student.getStudentLrn() : "N/A";
                String fullName = student != null
                        ? String.format("%s %s %s", 
                            student.getStudentFirstname(), 
                            student.getStudentMiddlename() != null ? student.getStudentMiddlename() : "",
                            student.getStudentLastname())
                        : "N/A";

                model.addRow(new Object[] {
                        studentLRN,
                        fullName.trim(), // Trim to remove extra spaces if middle name is null
                        violation.getViolationType(),
                        violation.getReinforcement(),
                        violation.getStatus(),
                        null // Placeholder for the Actions column
                });

            } catch (Exception e) {
                showError("Error loading student data for violation", e);
            }
        }
    }

    private ViolationRecord getViolationAt(int row) {
        return (violations != null && row >= 0 && row < violations.size()) ? violations.get(row) : null;
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this,
                message + ": " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

}