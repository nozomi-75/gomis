package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.model.Violation;
import lyfjshs.gomis.components.table.TableActionManager;

public class ViolationTablePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private ViolationCRUD violationCRUD;
    private Connection connect;
    private ViolationActionListener actionListener;

    public interface ViolationActionListener {
        void onViewViolation(Violation violation);

        void onResolveViolation(Violation violation);

        void onDeleteViolation(Violation violation);
    }

    public ViolationTablePanel(Connection conn, ViolationCRUD violationCRUD) {
        this.connect = conn;
        this.violationCRUD = violationCRUD;
        initializePanel();
        initializeTable();
        setupTableActions();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private void initializeTable() {
        String[] columnNames = {"#", "Violation Type", "Reinforcement", "Status", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        setupTableProperties();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupTableProperties() {
        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setFont(new Font("Tahoma", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        table.setSelectionBackground(new Color(232, 241, 249));
        table.setSelectionForeground(Color.BLACK);
    }

    private void setupTableActions() {
        TableActionManager actionManager = new TableActionManager();

        // Add view action
        actionManager.addAction("View", (table, row) -> {
            Violation violation = getViolationAt(row);
            if (violation != null) {
                ViewViolationDetails viewDialog = new ViewViolationDetails(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    violation,
                    new StudentsDataDAO(connect) // Assuming you have a StudentsDataDAO class that takes a Connection
                );
                viewDialog.setVisible(true);
            }
        }, new Color(0, 150, 200), null);  // Blue color for view button

        // Resolve action
        actionManager.addAction("Resolve",
                (t, row) -> handleResolveAction(row),
                new Color(46, 204, 113),
                new com.formdev.flatlaf.extras.FlatSVGIcon("icons/check.svg", 0.5f));

        actionManager.applyTo(table, 5); // Apply to Actions column
    }

    private Violation getViolationAt(int row) {
        int index = row + 1; // Assuming the table is 1-based
        String violationType = (String) tableModel.getValueAt(row, 1);
        String reinforcement = (String) tableModel.getValueAt(row, 2);
        String status = (String) tableModel.getValueAt(row, 3);
        return new Violation(index, violationType, reinforcement, status);
    }

    private void handleViewAction(int row) {
        Violation violation = getViolationAt(row);
        if (violation != null && actionListener != null) {
            actionListener.onViewViolation(violation);
        }
    }

    private void handleResolveAction(int row) {
        Violation violation = getViolationAt(row);
        if (violation != null && actionListener != null) {
            actionListener.onResolveViolation(violation);
        }
    }

    private void handleDeleteAction(int row) {
        Violation violation = getViolationAt(row);
        if (violation != null && actionListener != null) {
            actionListener.onDeleteViolation(violation);
        }
    }

    public void refreshData() {
        try {
            tableModel.setRowCount(0);
            for (Violation violation : violationCRUD.getAllViolations()) {
                addViolationRecord(violation.getIndex(), violation.getViolationType(), violation.getReinforcement(), violation.getStatus());
            }
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
            tableModel.setRowCount(0);
            for (Violation violation : violationCRUD.searchViolations(connect, searchTerm)) {
                addViolationRecord(violation.getIndex(), violation.getViolationType(), violation.getReinforcement(), violation.getStatus());
            }
        } catch (Exception e) {
            showError("Error searching violations", e);
        }
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this,
                message + ": " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public void setActionListener(ViolationActionListener listener) {
        this.actionListener = listener;
    }

    public void addViolationRecord(int index, String violationType, String reinforcement, String status) {
        Object[] rowData = {index, violationType, reinforcement, status, ""}; // Action column is left blank
        tableModel.addRow(rowData);
    }
}