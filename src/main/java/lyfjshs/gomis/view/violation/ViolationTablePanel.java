package lyfjshs.gomis.view.violation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;

public class ViolationTablePanel extends JPanel {
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
        String[] columnNames = { "Type", "Description", "Reinforcement", "Status", "Actions" };
        Class<?>[] columnTypes = { String.class, String.class, String.class, String.class, Object.class };
        boolean[] editableColumns = { false, false, false, false, true };
        double[] columnWidths = { 0.15, 0.25, 0.2, 0.15, 0.25 };
        int[] alignments = { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER };

        // Create action manager for view and resolve actions
        TableActionManager actionManager = new DefaultTableActionManager() {
            @Override
            public void setupTableColumn(GTable table, int actionColumnIndex) {
                table.getColumnModel().getColumn(actionColumnIndex)
                    .setCellRenderer((t, value, isSelected, hasFocus, row, column) -> {
                        JPanel panel = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
                        panel.setOpaque(false);
                        
                        JButton viewBtn = new JButton("View");
                        viewBtn.setBackground(new Color(0, 123, 255));
                        viewBtn.setForeground(Color.WHITE);
                        
                        JButton resolveBtn = new JButton("Resolve");
                        resolveBtn.setBackground(new Color(46, 204, 113));
                        resolveBtn.setForeground(Color.WHITE);
                        
                        panel.add(viewBtn);
                        panel.add(resolveBtn);
                        
                        return panel;
                    });
            }

            @Override
            public void onTableAction(GTable table, int row) {
                // Actions are handled by mouse listener
            }
        };

        // Create table
        violationTable = new GTable(new Object[0][5], columnNames, columnTypes, editableColumns, columnWidths,
                alignments, false, actionManager);

        // Add mouse listener for button actions
        violationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = violationTable.rowAtPoint(e.getPoint());
                int col = violationTable.columnAtPoint(e.getPoint());
                
                if (col == 4 && row >= 0) { // Actions column
                    Rectangle cellRect = violationTable.getCellRect(row, col, false);
                    int x = e.getX() - cellRect.x;
                    
                    // Assuming View button is on the left and Resolve on the right
                    if (x < cellRect.width / 2) {
                        viewViolation(row);
                    } else {
                        Violation violation = getViolationAt(row);
                        if (violation != null) {
                            resolveViolation(violation);
                        }
                    }
                }
            }
        });

        // Set minimum width for Actions column
        violationTable.getColumnModel().getColumn(4).setMinWidth(200);
        violationTable.getColumnModel().getColumn(4).setPreferredWidth(200);

        // Add table to scroll pane and panel
        JScrollPane scrollPane = new JScrollPane(violationTable);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            violations = violationDAO.getAllViolations();
            updateTableWithViolations();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading violations: " + e.getMessage());
        }
    }

    private void updateTableWithViolations() {
        DefaultTableModel model = (DefaultTableModel) violationTable.getModel();
        model.setRowCount(0);

        for (int i = 0; i < violations.size(); i++) {
            Violation violation = violations.get(i);
            model.addRow(new Object[] {
                violation.getViolationType(),
                violation.getViolationDescription(),
                violation.getReinforcement(),
                violation.getStatus(),
                null // Actions column
            });
            // Store violation object in table
            violationTable.putClientProperty("violation_" + i, violation);
        }
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
            try {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to resolve this violation?",
                    "Confirm Resolution",
                    JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (violationDAO.updateViolationStatus(violation.getViolationId(), "RESOLVED")) {
                        JOptionPane.showMessageDialog(this,
                            "Violation resolved successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error resolving violation: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}