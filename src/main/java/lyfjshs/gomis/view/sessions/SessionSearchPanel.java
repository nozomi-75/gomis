package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class SessionSearchPanel extends JPanel {
    // Update color constants to use UIManager colors for theme compatibility
    private static final Color PRIMARY_COLOR = new Color(67, 97, 238);
    private static final Color SECONDARY_COLOR = new Color(63, 55, 201);
        // Components
        private JComboBox<String> counselorFilter;
        private JComboBox<String> appointmentTypeFilter;
        private JComboBox<String> consultationTypeFilter;
        private JComboBox<String> statusFilter;
        private JComboBox<String> sectionFilter;
        private DatePicker dateFromPicker;
        private DatePicker dateToPicker;
        private JFormattedTextField dateFromField;
        private JFormattedTextField dateToField;
        private JTextField participantFilter;
        private JPanel filterBadgesPanel;
        private JButton searchButton;
        private JButton resetButton;
        private List<FilterBadge> activeBadges;
    
        public SessionSearchPanel() {
            activeBadges = new ArrayList<>();
            initComponents();
            setupLayout();
            setupActionListeners();
            
            // Set overall panel aesthetics
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }
    
        private void initComponents() {
            // Initialize filter components
            counselorFilter = createStyledComboBox(new String[]{
                "All Counselors", "Alice Smith", "John Doe", "Maria Garcia"
            });
    
            appointmentTypeFilter = createStyledComboBox(new String[]{
                "All Types", "Individual", "Group", "Parent Conference"
            });
    
            consultationTypeFilter = createStyledComboBox(new String[]{
                "All Types", "Academic", "Behavioral", "Career", "Personal"
            });
    
            statusFilter = createStyledComboBox(new String[]{
                "All Statuses", "Completed", "Pending", "Cancelled"
            });
    
            sectionFilter = createStyledComboBox(new String[]{
                "All Sections", "Section A", "Section B", "Section C"
            });
    
            // Initialize date fields with proper format
            dateFromField = new JFormattedTextField();
            dateToField = new JFormattedTextField();
            dateFromField.setColumns(10);
            dateToField.setColumns(10);

            // Initialize date pickers with null check handling
            dateFromPicker = new DatePicker();
            dateToPicker = new DatePicker();
            
            // Set initial dates to avoid null pointer
            LocalDate today = LocalDate.now();
            dateFromPicker.setSelectedDate(today);
            dateToPicker.setSelectedDate(today);
            
            // Connect fields to pickers
            dateFromPicker.setEditor(dateFromField);
            dateToPicker.setEditor(dateToField);
            
            // Style fields
            styleTextField(dateFromField);
            styleTextField(dateToField);
    
            participantFilter = new JTextField();
            participantFilter.setPreferredSize(new Dimension(200, 35));
            
            filterBadgesPanel = new JPanel();
            filterBadgesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    
            searchButton = createModernButton("Search", PRIMARY_COLOR);
        resetButton = createModernButton("Reset", Color.GRAY);
    }

    private void styleTextField(JFormattedTextField field) {
        field.setPreferredSize(new Dimension(200, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker(), 1),
            BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(getBackground());
        comboBox.setForeground(getForeground());
        
        // Custom renderer for theme compatibility
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, 
                boolean isSelected, boolean cellHasFocus) {
                
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                if (isSelected) {
                    label.setBackground(PRIMARY_COLOR);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(getBackground());
                    label.setForeground(getForeground());
                }
                
                return label;
            }
        });

        return comboBox;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Create main content panel with theme-aware background
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        // Add filter section with theme-aware background
        JPanel filterPanel = new JPanel(new MigLayout(
            "wrap 2, fillx, insets 10", 
            "[grow,fill][grow,fill]", 
            "[]10[]10[]10[]"
        ));
        filterPanel.setBackground(UIManager.getColor("Panel.background"));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Add components to filter panel
        addLabelAndComponent(filterPanel, "Counselor", counselorFilter);
        addLabelAndComponent(filterPanel, "Appointment Type", appointmentTypeFilter);
        addLabelAndComponent(filterPanel, "Consultation Type", consultationTypeFilter);
        addLabelAndComponent(filterPanel, "Status", statusFilter);
        addLabelAndComponent(filterPanel, "Date From", dateFromField);
        addLabelAndComponent(filterPanel, "Date To", dateToField);
        addLabelAndComponent(filterPanel, "Participant Name", participantFilter);
        addLabelAndComponent(filterPanel, "Section", sectionFilter);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));
        buttonPanel.add(resetButton);
        buttonPanel.add(searchButton);
        filterPanel.add(buttonPanel, "span 2, right");

        // Add filter badges panel with scroll
        JScrollPane badgesScroll = new JScrollPane(filterBadgesPanel);
        badgesScroll.setBorder(null);
        badgesScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        badgesScroll.setPreferredSize(new Dimension(0, 50));

        contentPanel.add(filterPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(badgesScroll);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createModernButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Use UIManager colors for theme compatibility
        Color buttonBackground = UIManager.getColor("Button.background");
        Color buttonForeground = UIManager.getColor("Button.foreground");
        
        button.setBackground(baseColor != null ? baseColor : buttonBackground);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor != null ? baseColor : buttonBackground);
            }
        });
        
        return button;
    }

    private void setupActionListeners() {
        searchButton.addActionListener(e -> performSearch());
        resetButton.addActionListener(e -> resetFilters());

        // Add change listeners to all filters
        counselorFilter.addActionListener(e -> updateFilterBadges());
        appointmentTypeFilter.addActionListener(e -> updateFilterBadges());
        consultationTypeFilter.addActionListener(e -> updateFilterBadges());
        statusFilter.addActionListener(e -> updateFilterBadges());
        sectionFilter.addActionListener(e -> updateFilterBadges());
        participantFilter.addActionListener(e -> updateFilterBadges());
    }

    private void performSearch() {
        try {
            // Extract search parameters
            String counselor = (String) counselorFilter.getSelectedItem();
            String appointmentType = (String) appointmentTypeFilter.getSelectedItem();
            String consultationType = (String) consultationTypeFilter.getSelectedItem();
            String status = (String) statusFilter.getSelectedItem();
            String section = (String) sectionFilter.getSelectedItem();
            String participant = participantFilter.getText();
            LocalDate dateFrom = dateFromPicker.getSelectedDate();
            LocalDate dateTo = dateToPicker.getSelectedDate();

            // TODO: Implement actual search logic
            JOptionPane.showMessageDialog(this, 
                "Search Parameters:\n" +
                "Counselor: " + counselor + "\n" +
                "Appointment Type: " + appointmentType + "\n" +
                "Consultation Type: " + consultationType + "\n" +
                "Status: " + status + "\n" +
                "Section: " + section + "\n" +
                "Participant: " + participant + "\n" +
                "Date From: " + (dateFrom != null ? dateFrom : "Not Selected") + "\n" +
                "Date To: " + (dateTo != null ? dateTo : "Not Selected"),
                "Search Results", 
                JOptionPane.INFORMATION_MESSAGE);
           
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error searching session data: " + ex.getMessage(), 
                "Search Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFilters() {
        try {
            // Reset combo boxes
            counselorFilter.setSelectedIndex(0);
            appointmentTypeFilter.setSelectedIndex(0);
            consultationTypeFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            sectionFilter.setSelectedIndex(0);
            
            // Reset text field
            participantFilter.setText("");
            
            // Reset date fields safely
            dateFromField.setText("");
            dateToField.setText("");
            
            // Clear date pickers without triggering NPE
            LocalDate today = LocalDate.now();
            dateFromPicker.setSelectedDate(today);
            dateToPicker.setSelectedDate(today);
            dateFromField.setValue(null);
            dateToField.setValue(null);
            
            // Clear badges
            filterBadgesPanel.removeAll();
            filterBadgesPanel.revalidate();
            filterBadgesPanel.repaint();
            activeBadges.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFilterBadges() {
        filterBadgesPanel.removeAll();
        activeBadges.clear();

        addFilterBadgeIfSelected(counselorFilter, "Counselor");
        addFilterBadgeIfSelected(appointmentTypeFilter, "Appointment");
        addFilterBadgeIfSelected(consultationTypeFilter, "Consultation");
        addFilterBadgeIfSelected(statusFilter, "Status");
        addFilterBadgeIfSelected(sectionFilter, "Section");
        
        if (!participantFilter.getText().isEmpty()) {
            addFilterBadge("Participant", participantFilter.getText(), () -> {
                participantFilter.setText("");
                updateFilterBadges();
            });
        }

        // Handle dates properly
        if (dateFromPicker.getSelectedDate() != null) {
            addFilterBadge("Date From", dateFromPicker.getSelectedDate().toString(), () -> {
                dateFromField.setValue(null);
                dateFromPicker.setSelectedDate(null);
                updateFilterBadges();
            });
        }

        if (dateToPicker.getSelectedDate() != null) {
            addFilterBadge("Date To", dateToPicker.getSelectedDate().toString(), () -> {
                dateToField.setValue(null);
                dateToPicker.setSelectedDate(null);
                updateFilterBadges();
            });
        }

        filterBadgesPanel.revalidate();
        filterBadgesPanel.repaint();
    }

    private void addFilterBadgeIfSelected(JComboBox<String> comboBox, String label) {
        String selected = (String) comboBox.getSelectedItem();
        if (selected != null && !selected.startsWith("All")) {
            addFilterBadge(label, selected, () -> {
                comboBox.setSelectedIndex(0);
                updateFilterBadges();
            });
        }
    }

    private void addFilterBadge(String key, String value, Runnable onRemove) {
        FilterBadge badge = new FilterBadge(key, value, onRemove);
        activeBadges.add(badge);
        filterBadgesPanel.add(badge);
    }

    private void addLabelAndComponent(JPanel panel, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label, "");
        panel.add(component, "height 35!");
    }


       // Main method for testing
       public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();
            JFrame frame = new JFrame("Aesthetic Session Search");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            
            SessionSearchPanel searchPanel = new SessionSearchPanel();
            frame.add(searchPanel);
            
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Update FilterBadge to be theme-aware
    class FilterBadge extends JPanel {
        private final String filterKey;
        private final String filterValue;
        private final Runnable onRemove;
    
        public FilterBadge(String key, String value, Runnable onRemove) {
            this.filterKey = key;
            this.filterValue = value;
            this.onRemove = onRemove;
            
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
            setBackground(UIManager.getColor("Panel.background").brighter());
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker(), 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
            
            JLabel label = new JLabel(key + ": " + value);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(UIManager.getColor("Label.foreground"));
            
            JButton removeBtn = new JButton("×");
            removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            removeBtn.setBorderPainted(false);
            removeBtn.setContentAreaFilled(false);
            removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeBtn.addActionListener(e -> onRemove.run());
            
            add(label);
            add(removeBtn);
        }
    
        public String getFilterKey() {
            return filterKey;
        }
    
        public String getFilterValue() {
            return filterValue;
        }
    }
}