/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import net.miginfocom.swing.MigLayout;

public class AppointmentsListPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(AppointmentsListPanel.class);
    private final AppointmentDAO appointmentDAO;
    private final LocalDate selectedDate;
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;
    private final Consumer<Appointment> onAppointmentSelect;

    public AppointmentsListPanel(Connection connection, LocalDate date, Consumer<Appointment> onAppointmentSelect) {
        this.appointmentDAO = new AppointmentDAO(connection);
        this.selectedDate = date;
        this.onAppointmentSelect = onAppointmentSelect;

        setLayout(new BorderLayout());
        initializeUI();
        loadAppointments();
    }

    private void initializeUI() {
        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("fillx"));
        JLabel dateLabel = new JLabel(selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(dateLabel, "left");
        add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        String[] columnNames = {"Time", "Title", "Type", "Status", "Actions", "Appointment"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Actions column is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) { // Hidden Appointment object column
                    return Appointment.class;
                } else if (columnIndex == 4) { // Actions column
                    return JButton.class;
                }
                return String.class;
            }
        };

        appointmentsTable = new JTable(tableModel);
        appointmentsTable.setRowHeight(30);
        appointmentsTable.setShowGrid(false);
        appointmentsTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        appointmentsTable.getTableHeader().setReorderingAllowed(false);
        
        // Hide the appointment column
        appointmentsTable.getColumnModel().getColumn(5).setMinWidth(0);
        appointmentsTable.getColumnModel().getColumn(5).setMaxWidth(0);
        appointmentsTable.getColumnModel().getColumn(5).setWidth(0);
        appointmentsTable.getColumnModel().getColumn(5).setPreferredWidth(0);
        
        // Style the table
        styleTable(appointmentsTable);

        // Add click listener for selecting appointment or deleting
        appointmentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = appointmentsTable.rowAtPoint(evt.getPoint());
                int col = appointmentsTable.columnAtPoint(evt.getPoint());
                
                if (row >= 0) {
                    if (col == 4) { // Actions column (Delete button)
                        Appointment appointmentToDelete = (Appointment) tableModel.getValueAt(row, 5);
                        int confirm = JOptionPane.showConfirmDialog(
                            AppointmentsListPanel.this,
                            "Are you sure you want to delete this appointment?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                appointmentDAO.deleteAppointment(appointmentToDelete.getAppointmentId());
                                JOptionPane.showMessageDialog(AppointmentsListPanel.this, "Appointment deleted successfully.");
                                loadAppointments(); // Refresh the list
                                Main.appointmentCalendar.refreshViews();
                            } catch (SQLException e) {
                                logger.error("Error deleting appointment: " + e.getMessage(), e);
                                JOptionPane.showMessageDialog(AppointmentsListPanel.this, 
                                    "Error deleting appointment: " + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else { // Other columns (select appointment)
                        Appointment appointment = (Appointment) tableModel.getValueAt(row, 5); // Hidden column
                        if (onAppointmentSelect != null) {
                            onAppointmentSelect.accept(appointment);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadAppointments() {
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(selectedDate);
            tableModel.setRowCount(0);

            if (appointments != null) {
                for (Appointment appointment : appointments) {
                    tableModel.addRow(new Object[]{
                        appointment.getAppointmentDateTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                        appointment.getAppointmentTitle(),
                        appointment.getConsultationType(),
                        appointment.getAppointmentStatus(),
                        "Delete", // Value for the Actions column
                        appointment // Store the appointment object in a hidden column
                    });
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading appointments: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Error loading appointments: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTable(JTable table) {
        // Header styling
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(UIManager.getColor("Panel.background"));
        table.getTableHeader().setForeground(UIManager.getColor("Label.foreground"));

        // Cell styling
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 4) { // Actions column
                    JButton deleteButton = new JButton("Delete");
                    deleteButton.setBackground(new Color(220, 53, 69)); // Bootstrap danger color
                    deleteButton.setForeground(Color.WHITE);
                    deleteButton.setFocusPainted(false);
                    return deleteButton;
                }
                
                if (isSelected) {
                    c.setBackground(UIManager.getColor("Component.accentColor"));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? 
                        UIManager.getColor("Panel.background") : 
                        UIManager.getColor("Table.alternateRowColor"));
                    c.setForeground(UIManager.getColor("Label.foreground"));
                }
                
                return c;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }
} 