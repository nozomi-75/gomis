package lyfjshs.gomis.test;

import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AppointmentDetailsUI extends JPanel {
    public AppointmentDetailsUI() {
        // Set up FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Set background and border for shadow effect
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // Set layout with MigLayout
        setLayout(new MigLayout("wrap, fillx", "[grow]", "[]20[]20[]20[]"));

        // **Appointment Details Section**
        JPanel appointmentDetailsHeader = createSectionHeader("Appointment Details", "APT-2025-0356");
        add(appointmentDetailsHeader, "cell 0 0, growx");

        JPanel detailsGrid = new JPanel(new MigLayout("wrap 2, gap 15", "[grow][grow]"));
        detailsGrid.setOpaque(false);

        // Add detail items with static cell positions
        addDetailItemToGrid(detailsGrid, "Appointment Title", "Career Guidance Consultation", 0, 0);
        addDetailItemToGrid(detailsGrid, "Consultation Type", "Personal Consultation", 1, 0);
        addDetailItemToGrid(detailsGrid, "Date & Time", "2025-03-21 10:00 AM", 0, 1);
        JLabel statusBadge = new JLabel("Pending");
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(243, 156, 18));
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setHorizontalAlignment(JLabel.CENTER);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        addDetailItemToGrid(detailsGrid, "Status", statusBadge, 1, 1);

        add(detailsGrid, "cell 0 1, growx");

        // **Participants Section**
        JPanel participantsHeader = createSectionHeader("Participants", null);
        add(participantsHeader, "cell 0 2, growx");

        // Participants Table
        String[] columnNames = {"Name", "Participant Type", "Contact Number", "Gender"};
        Object[][] data = {
            {"Juan Dela Cruz", "Student", "+63 912 345 6789", "Male"},
            {"Maria Santos", "Parent", "+63 987 654 3210", "Female"}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        JTable participantsTable = new JTable(model);
        participantsTable.setRowHeight(40);
        participantsTable.setShowGrid(true);
        participantsTable.setGridColor(new Color(221, 221, 221));

        // Custom header renderer
        participantsTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(new Color(241, 243, 245));
                label.setForeground(new Color(44, 62, 80));
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 221, 221)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                return label;
            }
        });

        // Custom cell renderer
        participantsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 221, 221)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                return label;
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(participantsTable);
        add(tableScrollPane, "cell 0 3, growx");

        // **Guidance Counselor Information Section**
        JPanel counselorHeader = createSectionHeader("Guidance Counselor Information", null);
        add(counselorHeader, "cell 0 4, growx");

        JPanel counselorGrid = new JPanel(new MigLayout("wrap 2, gap 15", "[grow][grow]"));
        counselorGrid.setOpaque(false);

        // Add counselor detail items with static cell positions
        addDetailItemToGrid(counselorGrid, "Name", "Alice B. Smith", 0, 0);
        addDetailItemToGrid(counselorGrid, "Specialization", "Career Counseling", 1, 0);
        addDetailItemToGrid(counselorGrid, "Contact Number", "+63 987 654 3210", 0, 1);
        addDetailItemToGrid(counselorGrid, "Email", "alice.smith@school.edu", 1, 1);

        add(counselorGrid, "cell 0 5, growx");

        // **Additional Information Section**
        JPanel additionalHeader = createSectionHeader("Additional Information", null);
        add(additionalHeader, "cell 0 6, growx");

        JPanel notesSectionPanel = new JPanel(new MigLayout("wrap, insets 15", "[grow]", "[]10[]"));
        notesSectionPanel.setBackground(new Color(248, 249, 250));
        notesSectionPanel.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));

        JLabel notesLabel = new JLabel("Appointment Notes");
        notesLabel.setForeground(new Color(44, 62, 80));
        notesLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JTextArea notesArea = new JTextArea(
            "Career path discussion, future academic planning, and potential internship opportunities. " +
            "Student seeks guidance on career trajectory and subject selection for upcoming semester."
        );
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setEditable(false);
        notesArea.setOpaque(false);
        notesArea.setBorder(null);
        notesArea.setFont(new Font("Arial", Font.PLAIN, 12));

        notesSectionPanel.add(notesLabel, "growx");
        notesSectionPanel.add(notesArea, "growx, h 100!");

        add(notesSectionPanel, "cell 0 7, growx");
    }

    private JPanel createSectionHeader(String title, String badge) {
        JPanel headerPanel = new JPanel(new MigLayout("insets 12 15, fillx"));
        headerPanel.setBackground(new Color(52, 152, 219));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        headerPanel.add(titleLabel, "grow, pushx");

        if (badge != null) {
            JLabel badgeLabel = new JLabel(badge);
            badgeLabel.setForeground(Color.WHITE);
            badgeLabel.setBackground(new Color(41, 128, 185));
            badgeLabel.setOpaque(true);
            badgeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            headerPanel.add(badgeLabel);
        }

        return headerPanel;
    }

    private void addDetailItemToGrid(JPanel grid, String label, Object value, int col, int row) {
        JPanel itemPanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
        itemPanel.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(new Color(44, 62, 80));
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        itemPanel.add(labelComponent, "growx");

        if (value instanceof String) {
            JLabel valueComponent = new JLabel((String) value);
            valueComponent.setBackground(new Color(248, 249, 250));
            valueComponent.setOpaque(true);
            valueComponent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 236, 239)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            valueComponent.setFont(new Font("Arial", Font.PLAIN, 12));
            itemPanel.add(valueComponent, "growx");
        } else if (value instanceof JComponent) {
            itemPanel.add((JComponent) value, "growx");
        }

        grid.add(itemPanel, "cell " + col + " " + row + ", grow");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Comprehensive Appointment Details");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            AppointmentDetailsUI panel = new AppointmentDetailsUI();
            frame.add(new JScrollPane(panel));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}