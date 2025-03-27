package lyfjshs.gomis.view.sessions;

import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionsRecordData extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(SessionsRecordData.class.getName());

    // Colors from the CSS
    private static final Color CONTAINER_BG = Color.WHITE;
    private static final Color CARD_BG = new Color(0xF8F9FA);
    private static final Color BORDER_COLOR = new Color(0xE9ECEF);
    private static final Color HEADER_COLOR = new Color(0x2C3E50);
    private static final Color TEXT_COLOR = new Color(0x212529);
    private static final Color SUBTEXT_COLOR = new Color(0x495057);
    private static final Color TABLE_HEADER_BG = new Color(0xF1F3F5);
    private static final Color BLUE_BUTTON = new Color(0x3498DB);
    private static final Color GREEN_BUTTON = new Color(0x2ECC71);
    private static final Color RED_BUTTON = new Color(0xE74C3C);
    private static final Color MODAL_CLOSE_COLOR = new Color(0x6C757D);

    // Fonts
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font CARD_VALUE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font MODAL_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font MODAL_LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font MODAL_VALUE_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public SessionsRecordData() {
        // Set FlatLaf look and feel (should be set in the parent application, but included here for completeness)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            LOGGER.log(Level.SEVERE, "Failed to set FlatLaf look and feel", e);
        }

        // Panel setup
        setLayout(new MigLayout("fill, insets 30", "[grow]", "[][][][][]"));
        setBackground(CONTAINER_BG);
        setBorder(BorderFactory.createLineBorder(new Color(0xE7E9EC), 2));

        // Build UI components
        add(createHeaderPanel(), "growx, wrap");
        add(createSessionGrid(), "growx, wrap");
        add(createSessionSummary(), "growx, wrap, gaptop 20");
        add(createParticipantsTable(), "growx, wrap, gaptop 20");
        add(createActionButtons(), "growx, gaptop 20");
    }

    // Create the header panel with title and buttons
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new MigLayout("fill", "[grow][]", "[]"));
        headerPanel.setBackground(CONTAINER_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel headerLabel = new JLabel("Session Record");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(HEADER_COLOR);
        headerPanel.add(headerLabel, "growx");

        JPanel headerButtons = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        headerButtons.setBackground(CONTAINER_BG);
        JButton editSessionBtn = createButton("Edit Session", BLUE_BUTTON);
        JButton printReportBtn = createButton("Print Report", GREEN_BUTTON);
        headerButtons.add(editSessionBtn, "gapright 10");
        headerButtons.add(printReportBtn);
        headerPanel.add(headerButtons, "right");

        return headerPanel;
    }

    // Create the session grid with session details
    private JPanel createSessionGrid() {
        JPanel sessionGrid = new JPanel(new MigLayout("wrap 3, fill", "[grow][grow][grow]", "[][]"));
        sessionGrid.setBackground(CONTAINER_BG);

        sessionGrid.add(createSessionCard("SESSION DATE/TIME", "2025-03-26 18:26:04.0"), "grow");
        sessionGrid.add(createSessionCard("SESSION STATUS", "Active"), "grow");
        sessionGrid.add(createSessionCard("APPOINTMENT TYPE", "Scheduled"), "grow");
        sessionGrid.add(createSessionCard("COUNSELOR NAME", "Alice Smith"), "grow");
        sessionGrid.add(createSessionCard("CONSULTATION TYPE", "Individual Counseling"), "grow");
        sessionGrid.add(createSessionCard("UPDATED AT", "2025-03-26 18:26:04.0"), "grow");

        return sessionGrid;
    }

    // Create a session card
    private JPanel createSessionCard(String title, String value) {
        JPanel card = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(BORDER_COLOR, 1));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CARD_TITLE_FONT);
        titleLabel.setForeground(SUBTEXT_COLOR);
        card.add(titleLabel, "growx, wrap");

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(CARD_VALUE_FONT);
        valueLabel.setForeground(TEXT_COLOR);
        card.add(valueLabel, "growx");

        return card;
    }

    // Create the session summary panel
    private JPanel createSessionSummary() {
        JPanel sessionSummary = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
        sessionSummary.setBackground(CARD_BG);
        sessionSummary.setBorder(new LineBorder(BORDER_COLOR, 1));
        sessionSummary.setPreferredSize(new Dimension(0, 80));

        JLabel summaryLabel = new JLabel("SESSION SUMMARY");
        summaryLabel.setFont(CARD_TITLE_FONT);
        summaryLabel.setForeground(SUBTEXT_COLOR);
        sessionSummary.add(summaryLabel, "growx, wrap");

        JLabel summaryText = new JLabel("The student is undergoing a comprehensive guidance session to address behavioral concerns and develop coping strategies.");
        summaryText.setFont(CARD_VALUE_FONT);
        summaryText.setForeground(TEXT_COLOR);
        sessionSummary.add(summaryText, "growx");

        return sessionSummary;
    }

    // Create the participants table
    private JScrollPane createParticipantsTable() {
        String[] columnNames = {"#", "Participant Name", "Participant Type", "Violation Type", "Actions"};
        Object[][] data = {{"1", "Jane Doe", "Student", "Behavioral Misconduct", ""}};

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the "Actions" column is editable
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(SUBTEXT_COLOR);
        table.getTableHeader().setFont(TABLE_HEADER_FONT);
        table.setBorder(new LineBorder(new Color(0xDEE2E6), 1));

        // Add "View Details" button in the Actions column
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), table));

        return new JScrollPane(table);
    }

    // Create the action buttons panel
    private JPanel createActionButtons() {
        JPanel actionButtons = new JPanel(new MigLayout("fill", "[grow][]", "[]"));
        actionButtons.setBackground(CONTAINER_BG);

        JButton createIncidentBtn = createButton("Create Incident Report", BLUE_BUTTON);
        JButton closeSessionBtn = createButton("Close Session", RED_BUTTON);

        actionButtons.add(createIncidentBtn, "left");
        actionButtons.add(closeSessionBtn, "right");

        return actionButtons;
    }

    // Helper method to create a styled button
    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Custom renderer for the "View Details" button in the table
    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("View Details");
            setFont(BUTTON_FONT);
            setForeground(Color.WHITE);
            setBackground(BLUE_BUTTON);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Custom editor for the "View Details" button in the table
    static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;
        private final JTable table;

        public ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            this.table = table;
            button = new JButton();
            button.setOpaque(true);
            button.setText("View Details");
            button.setFont(BUTTON_FONT);
            button.setForeground(Color.WHITE);
            button.setBackground(BLUE_BUTTON);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText("View Details");
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                showParticipantDetailsModal();
            }
            isPushed = false;
            return label;
        }

        private void showParticipantDetailsModal() {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(table), "Participant Details", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(600, 300);
            dialog.setLocationRelativeTo(table);

            JPanel modalContent = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][][]"));
            modalContent.setBackground(CONTAINER_BG);

            // Modal Header
            JPanel modalHeader = new JPanel(new MigLayout("fill", "[grow][]", "[]"));
            modalHeader.setBackground(CONTAINER_BG);
            modalHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

            JLabel modalTitle = new JLabel("Participant Details");
            modalTitle.setFont(MODAL_TITLE_FONT);
            modalHeader.add(modalTitle, "growx");

            JButton closeBtn = new JButton("×");
            closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            closeBtn.setForeground(MODAL_CLOSE_COLOR);
            closeBtn.setBorderPainted(false);
            closeBtn.setContentAreaFilled(false);
            closeBtn.addActionListener(e -> dialog.dispose());
            modalHeader.add(closeBtn, "right");

            modalContent.add(modalHeader, "growx, wrap");

            // Modal Grid
            JPanel detailGrid = new JPanel(new MigLayout("wrap 2, fill", "[grow][grow]", "[][][]"));
            detailGrid.setBackground(CONTAINER_BG);

            detailGrid.add(createDetailItem("Full Name", "Jane Doe"), "grow");
            detailGrid.add(createDetailItem("Participant Type", "Student"), "grow");
            detailGrid.add(createDetailItem("Violation Type", "Behavioral Misconduct"), "grow");
            detailGrid.add(createDetailItem("Contact Number", "+1 (555) 123-4567"), "grow");
            detailGrid.add(createDetailItem("Grade Level", "10th Grade"), "grow");
            detailGrid.add(createDetailItem("Section", "Maple"), "grow");

            modalContent.add(detailGrid, "growx");

            dialog.add(modalContent);
            dialog.setVisible(true);
        }

        private JPanel createDetailItem(String label, String value) {
            JPanel item = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
            item.setBackground(CONTAINER_BG);

            JLabel labelLbl = new JLabel(label);
            labelLbl.setFont(MODAL_LABEL_FONT);
            labelLbl.setForeground(MODAL_CLOSE_COLOR);
            item.add(labelLbl, "growx, wrap");

            JLabel valueLbl = new JLabel(value);
            valueLbl.setFont(MODAL_VALUE_FONT);
            valueLbl.setForeground(TEXT_COLOR);
            item.add(valueLbl, "growx");

            return item;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    // Main method for testing the panel
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Session Record Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 600);
            frame.setLocationRelativeTo(null);

            SessionsRecordData panel = new SessionsRecordData();
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}