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
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.Database.entity.Participants;

public class ViolationTablePanel extends JPanel {
    private GTable table; // Use GTable instead of JTable
    private ViolationDAO ViolationDAO;
    private Connection connect;
    private List<Violation> violations; // Store violations for action handling
    private TableActionManager actionManager;

    public ViolationTablePanel(Connection conn, ViolationDAO ViolationDAO) {
        this.connect = conn;
        this.ViolationDAO = ViolationDAO;
        this.violations = new ArrayList<>();
        initializePanel();
        initializeTable();
        refreshData();

    }

    private void initializePanel() {
        setLayout(new BorderLayout());
    }

    private TableActionManager setupTableActions() {
        TableActionManager actionManager = new DefaultTableActionManager();

        ((DefaultTableActionManager)actionManager)
            .addAction("View", (table, row) -> {
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
            }, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.4f))
            .addAction("Resolve",
                (t, row) -> handleResolveAction(row),
                new Color(46, 204, 113),
                new FlatSVGIcon("icons/resolve.svg", 0.4f));

        return actionManager;
    }

    private void initializeTable() {
        String[] columnNames = { "Student LRN", "Name", "Violation Type", "Status", "Date Recorded", "Actions" };
        Class<?>[] columnTypes = { String.class, String.class, String.class, String.class, String.class, Object.class };
        boolean[] editableColumns = { false, false, false, false, false, true };
        double[] columnWidths = { 0.15, 0.25, 0.15, 0.10, 0.15, 0.20 }; // Adjusted widths for new column
        int[] alignments = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.LEFT,
                SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER };

        actionManager = setupTableActions();

        table = new GTable(
                new Object[0][columnNames.length],
                columnNames,
                columnTypes,
                editableColumns,
                columnWidths,
                alignments,
                false,
                actionManager);

        // Set minimum width for Actions column
        table.getColumnModel().getColumn(5).setMinWidth(200);
        table.getColumnModel().getColumn(5).setMaxWidth(250);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void handleResolveAction(int row) {
        Violation violation = getViolationAt(row);
        if (violation != null) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to resolve this violation?",
                    "Confirm Resolution",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (ViolationDAO.updateViolationStatus(violation.getViolationId(), "RESOLVED")) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Violation has been resolved successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        refreshData(); // Refresh the table to show updated status
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Failed to resolve violation.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    showError("Error resolving violation", e);
                }
            }
        }
    }

    public void refreshData() {
        try {
            loadViolations(ViolationDAO.getAllViolations());
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
            loadViolations(ViolationDAO.searchViolations(searchTerm));
        } catch (Exception e) {
            showError("Error searching violations", e);
        }
    }

    private void loadViolations(List<Violation> violationList) {
        this.violations = violationList;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear existing rows

        StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connect);
        ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);

        for (Violation violation : violations) {
            try {
                // Get participant first
                Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
                String lrn = "N/A";
                String fullName;

                if (participant != null) {
                    // If participant is a student, get student details
                    if (participant.getStudentUid() != null && participant.getStudentUid() > 0) {
                        Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
                        if (student != null) {
                            lrn = student.getStudentLrn();
                            fullName = String.format("%s %s %s",
                                    student.getStudentFirstname(),
                                    student.getStudentMiddlename() != null ? student.getStudentMiddlename() : "",
                                    student.getStudentLastname());
                        } else {
                            fullName = String.format("%s %s",
                                    participant.getParticipantFirstName(),
                                    participant.getParticipantLastName());
                        }
                    } else {
                        // For non-student participants
                        fullName = String.format("%s %s",
                                participant.getParticipantFirstName(),
                                participant.getParticipantLastName());
                    }
                } else {
                    fullName = "Unknown Participant";
                }

                model.addRow(new Object[] {
                        lrn,
                        fullName.trim(), // Trim to remove extra spaces
                        violation.getViolationType(),
                        violation.getStatus(),
                        violation.getUpdatedAt(),
                        null // Placeholder for the Actions column
                });

            } catch (Exception e) {
                showError("Error loading participant data for violation", e);
            }
        }
    }

    private Violation getViolationAt(int row) {
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