package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;

import net.miginfocom.swing.MigLayout;

public class SessionSearchPanel extends JPanel {
    // Color Palette
    private static final Color PRIMARY_COLOR = new Color(73, 128, 108); // Soft green
    private static final Color SECONDARY_COLOR = new Color(240, 240, 240); // Light gray
    private static final Color TEXT_COLOR = new Color(33, 33, 33); // Dark gray
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250); // Off-white

    // Components
    private JComboBox<String> consultationTypeComboBox;
    private JComboBox<String> appointmentTypeComboBox;
    private AestheticDatePicker dateFromPicker;
    private AestheticDatePicker dateToPicker;
    private JButton searchButton;

    public SessionSearchPanel() {
        initComponents();
        setupLayout();
        setupActionListeners();
        
        // Set overall panel aesthetics
        setBackground(BACKGROUND_COLOR);
        setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
    }

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Aesthetic Session Search");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            
            SessionSearchPanel searchPanel = new SessionSearchPanel();
            frame.add(searchPanel);
            
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void initComponents() {
        // Consultation Type Combo Box with modern styling
        consultationTypeComboBox = createStyledComboBox(new String[]{
            "Group Consultations", "Career Guidance", "Academic Consultation", 
            "Personal Consultation", "Behavioral Consultation"
        });

        // Appointment Type Combo Box
        appointmentTypeComboBox = createStyledComboBox(new String[]{
            "Scheduled", "Walk-in"
        });

        // Date Pickers with custom styling
        dateFromPicker = new AestheticDatePicker();
        dateToPicker = new AestheticDatePicker();

        // Styled Buttons
        searchButton = createModernButton("Search", PRIMARY_COLOR);
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_COLOR);
        
        // Custom renderer for a modern look
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, 
                boolean isSelected, boolean cellHasFocus) {
                
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                if (isSelected) {
                    label.setBackground(PRIMARY_COLOR.brighter());
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                
                return label;
            }
        });

        return comboBox;
    }

    private void setupLayout() {
        setLayout(new MigLayout(
            "wrap 2, gap 15", 
            "[grow,fill][grow,fill]", 
            "[]15[]15[]"
        ));

        // Custom label style
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        // Consultation Type Label
        JLabel consultationTypeLabel = createStyledLabel("Consultation Type", labelFont);
        add(consultationTypeLabel);

        // Appointment Type Label
        JLabel appointmentTypeLabel = createStyledLabel("Appointment Type", labelFont);
        add(appointmentTypeLabel);

        // Combo Boxes
        add(consultationTypeComboBox, "height 40!");
        add(appointmentTypeComboBox, "height 40!");

        // Date From Label
        JLabel dateFromLabel = createStyledLabel("Date From", labelFont);
        add(dateFromLabel);

        // Date To Label
        JLabel dateToLabel = createStyledLabel("Date To", labelFont);
        add(dateToLabel);

        // Date Pickers
        add(dateFromPicker, "height 40!");
        add(dateToPicker, "height 40!");

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(searchButton);
        add(buttonPanel, "span 2, right");
    }

    private JLabel createStyledLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JButton createModernButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        
        // Button styling
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        
        // Remove border and focus paint
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });
        
        return button;
    }

    private void setupActionListeners() {
        searchButton.addActionListener(e -> performSearch());
    }

    private void performSearch() {
        try {
            // Extract search parameters
            String consultationType = (String) consultationTypeComboBox.getSelectedItem();
            String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();
            
            LocalDate dateFrom = dateFromPicker.getDate();
            LocalDate dateTo = dateToPicker.getDate();

            // TODO: Implement actual search logic
            JOptionPane.showMessageDialog(this, 
                "Search Parameters:\n" +
                "Consultation Type: " + consultationType + "\n" +
                "Appointment Type: " + appointmentType + "\n" +
                "Date From: " + (dateFrom != null ? dateFrom : "Not Selected") + "\n" +
                "Date To: " + (dateTo != null ? dateTo : "Not Selected") + "\n\n",
                "Search Results", 
                JOptionPane.INFORMATION_MESSAGE);
           
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, 
                "Error searching session data: " + ex.getMessage(), 
                "Search Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

// Enhanced Date Picker with modern styling
class AestheticDatePicker extends JPanel {
    private JSpinner dateSpinner;
    private LocalDate selectedDate;

    public AestheticDatePicker() {
        setLayout(new BorderLayout(5, 0));
        setOpaque(false);
        
        // Spinner styling
        SpinnerDateModel model = new SpinnerDateModel();
        dateSpinner = new JSpinner(model);
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Custom editor
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        add(dateSpinner, BorderLayout.CENTER);
    }

    public void setDate(LocalDate date) {
        this.selectedDate = date;
        if (date == null) {
            dateSpinner.setValue(null);
        } else {
            dateSpinner.setValue(java.sql.Date.valueOf(date));
        }
    }

    public LocalDate getDate() {
        Object value = dateSpinner.getValue();
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        }
        return null;
    }
}