package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.FlatTest.loading.LoadingGlassPane;
import net.miginfocom.swing.MigLayout;

public class AppointmentsDB_Test {

    private static Connection connection;
    private static AppointmentDAO appointmentDAO;
    private static ParticipantsDAO participantDAO;
    private static JTable table;
    private static DefaultTableModel tableModel;
    private static JFrame frame;
    private static LoadingGlassPane loadingGlassPane;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                setUp();
                createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static void setUp() throws SQLException {
        String url = "jdbc:mariadb://localhost:3306/gomisdb";
        String user = "root";
        String password = "YourRootPassword123!";
        
        connection = DriverManager.getConnection(url, user, password);
        appointmentDAO = new AppointmentDAO(connection);
        participantDAO = new ParticipantsDAO(connection);
        System.out.println("✔ Database connection established.");
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Appointments Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new MigLayout("fill", "[grow]", "[][grow][]"));

        JButton btnAdd = new JButton("Add Appointment");
        JButton btnUpdate = new JButton("Update Appointment");
        JButton btnDelete = new JButton("Delete Appointment");
        JButton btnReload = new JButton("Reload Data");
        
        JPanel panel = new JPanel(new MigLayout("", "[][][][]", ""));
        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnReload);
        
        String[] columnNames = {"ID", "Title", "Category", "DateTime", "Notes", "Status", "Participants"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        frame.add(panel, "wrap");
        frame.add(scrollPane, "grow, wrap");
        
        btnAdd.addActionListener(e -> showAppointmentDialog(null));
        btnUpdate.addActionListener(e -> updateAppointment());
        btnDelete.addActionListener(e -> deleteAppointment());
        btnReload.addActionListener(e -> reloadData());
        
        // Set up the glass pane for loading animation
        loadingGlassPane = new LoadingGlassPane("loading.svg", 100, 10, "circleG.svg");
        frame.setGlassPane(loadingGlassPane);
        
        // Make the frame visible
        frame.setVisible(true);
        
        // Initial data load with animation
        loadingGlassPane.setAlpha(1.0f);
        loadingGlassPane.setVisible(true);
        loadingGlassPane.startAnimation();
        refreshTableData(() -> {
            loadingGlassPane.stopAnimation(); // Switch to logo
            Timer logoTimer = new Timer(1000, e -> startFadeOut());
            logoTimer.setRepeats(false);
            logoTimer.start();
        });
    }

    private static void refreshTableData(Runnable onComplete) {
        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<List<Appointment>, Void>() {
            @Override
            protected List<Appointment> doInBackground() throws Exception {
                return appointmentDAO.getAllAppointments();
            }

            @Override
            protected void done() {
                try {
                    List<Appointment> appointments = get();
                    tableModel.setRowCount(0);
                    for (Appointment a : appointments) {
                        String participantNames = a.getParticipants().stream()
                                .map(p -> p.getParticipantFirstName() + " " + p.getParticipantLastName())
                                .collect(Collectors.joining(", "));
                        tableModel.addRow(new Object[]{
                                a.getAppointmentId(),
                                a.getAppointmentTitle(),
                                a.getConsultationType(),
                                a.getAppointmentDateTime(),
                                a.getAppointmentNotes(),
                                a.getAppointmentStatus(),
                                participantNames
                        });
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private static void reloadData() {
        loadingGlassPane.setAlpha(1.0f);
        loadingGlassPane.setVisible(true);
        loadingGlassPane.startAnimation();
        refreshTableData(() -> {
            loadingGlassPane.stopAnimation(); // Switch to logo
            Timer logoTimer = new Timer(900, e -> startFadeOut());
            logoTimer.setRepeats(false);
            logoTimer.start();
        });
    }

    private static void startFadeOut() {
        final float[] alpha = {1.0f};
        Timer fadeTimer = new Timer(90, e -> {
            alpha[0] -= 0.1f;
            if (alpha[0] <= 0) {
                ((Timer)e.getSource()).stop();
                loadingGlassPane.setAlpha(0.0f);
                loadingGlassPane.setVisible(false);
            } else {
                loadingGlassPane.setAlpha(alpha[0]);
            }
        });
        fadeTimer.start();
    }

    private static void showAppointmentDialog(Appointment appointment) {
        JTextField titleField = new JTextField(20);
        JTextField notesField = new JTextField(20);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"});
        JList<Participants> participantList = new JList<>(new DefaultListModel<>());
        JScrollPane participantScrollPane = new JScrollPane(participantList);
        
        try {
            List<Participants> participants = participantDAO.getAllParticipants();
            DefaultListModel<Participants> listModel = (DefaultListModel<Participants>) participantList.getModel();
            for (Participants p : participants) {
                listModel.addElement(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel panel = new JPanel(new MigLayout("wrap 2"));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Notes:"));
        panel.add(notesField);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        panel.add(new JLabel("Participants:"), "span");
        panel.add(participantScrollPane, "span, grow");

        int result = JOptionPane.showConfirmDialog(null, panel, "Appointment Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            List<Integer> selectedParticipants = participantList.getSelectedValuesList().stream()
                    .map(Participants::getParticipantId)
                    .collect(Collectors.toList());
            Timestamp appointmentDateTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
            int insertResult = 0;
            try {
                insertResult = appointmentDAO.insertAppointment(
                        1, titleField.getText(), "General", appointmentDateTime, notesField.getText(),
                        (String) statusBox.getSelectedItem(), selectedParticipants);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (insertResult > 0) {
                JOptionPane.showMessageDialog(null, "✔ Appointment Added");
                reloadData(); // Reload with animation after adding
            } else {
                JOptionPane.showMessageDialog(null, "❌ Failed to Add Appointment");
            }
        }
    }

    private static void updateAppointment() {
        JOptionPane.showMessageDialog(null, "Update feature not yet implemented");
    }

    private static void deleteAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Select an appointment to delete");
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        boolean deleted = false;
        try {
            deleted = appointmentDAO.deleteAppointment(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (deleted) {
            JOptionPane.showMessageDialog(null, "✔ Appointment Deleted");
            reloadData(); // Reload with animation after deleting
        } else {
            JOptionPane.showMessageDialog(null, "❌ Deletion Failed");
        }
    }
}