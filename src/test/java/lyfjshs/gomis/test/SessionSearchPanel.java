package lyfjshs.gomis.test;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import net.miginfocom.swing.MigLayout;

public class SessionSearchPanel extends JPanel {

    // Constructor
    public SessionSearchPanel() {
        initComponents();
        setupLayout();
        setupActionListeners();
    }
    
    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Session Search");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            
            SessionSearchPanel searchPanel = new SessionSearchPanel();
            frame.add(searchPanel);
            
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
        });
    }

    // Search Components
    private JComboBox<String> consultationTypeComboBox;
    private JComboBox<String> AppointmentTypeComboBox;
    private JDatePicker datePicker;
    private JButton searchButton;
    private JButton resetButton;

    

    private void initComponents() {
        // Consultation Type Combo Box
        consultationTypeComboBox = new JComboBox<>(new String[]{
            "Group Consultations", "Career Guidance", "Academic Consultation", "Personal Consultation", "Behavioral Consultation"
        });
        consultationTypeComboBox.setSelectedIndex(0);

        // Status Combo Box
        AppointmentTypeComboBox = new JComboBox<>(new String[]{
            "Scheduled", "Walk-in"
        });
        AppointmentTypeComboBox.setSelectedIndex(0);

        // Date Picker
        datePicker = new JDatePicker();

        // Buttons
        searchButton = createStyledButton("Search", new Color(81, 139, 111));
        resetButton = createStyledButton("Reset", new Color(224, 224, 224));
    }

    private void setupLayout() {
        setLayout(new MigLayout(
            "wrap 2, gap 10", 
            "[grow,fill][grow,fill]", 
            "[]10[]"
        ));

        // Custom label style
        Font labelFont = new Font("Arial", Font.BOLD, 12);
        Color labelColor = new Color(51, 51, 51);

        // Consultation Type
        JLabel consultationTypeLabel = new JLabel("Consultation Type");
        styleLabel(consultationTypeLabel, labelFont, labelColor);
        add(consultationTypeLabel);

        // Status
        JLabel AppointmentTypeLabel = new JLabel("Appointment Type");
        styleLabel(AppointmentTypeLabel, labelFont, labelColor);
        add(AppointmentTypeLabel);

        // Date
        JLabel dateLabel = new JLabel("Date");
        styleLabel(dateLabel, labelFont, labelColor);
        add(dateLabel);

        // First Row of Inputs
        add(consultationTypeComboBox);
        add(AppointmentTypeComboBox);
        add(datePicker);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(resetButton);
        buttonPanel.add(searchButton);
        add(buttonPanel, "span 2, right");

        // Add some padding
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private void styleLabel(JLabel label, Font font, Color color) {
        label.setFont(font);
        label.setForeground(color);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        
        if (backgroundColor.getRed() > 200) {
            // Light background (reset button)
            button.setForeground(Color.BLACK);
        } else {
            // Dark background (search button)
            button.setForeground(Color.WHITE);
        }
        
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(adjustBrightness(backgroundColor, 0.9));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    private Color adjustBrightness(Color color, double factor) {
        int red = (int) Math.min(255, color.getRed() * factor);
        int green = (int) Math.min(255, color.getGreen() * factor);
        int blue = (int) Math.min(255, color.getBlue() * factor);
        return new Color(red, green, blue);
    }

    private void setupActionListeners() {
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetSearch();
            }
        });
    }

    private void performSearch() {
        try {
            // Extract search parameters
            String consultationType = getSelectedComboBoxValue(consultationTypeComboBox);
            String AppointmentType = getSelectedComboBoxValue(AppointmentTypeComboBox);
            LocalDate date = datePicker.getDate();

           

           
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, 
                "Error searching session data: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void resetSearch() {
        // Reset all search fields
        consultationTypeComboBox.setSelectedIndex(0);
        AppointmentTypeComboBox.setSelectedIndex(0);
        datePicker.setDate(null);

        // Reload all session data
     
    }

    private String getSelectedComboBoxValue(JComboBox<String> comboBox) {
        String selected = (String) comboBox.getSelectedItem();
        return (selected == null || selected.startsWith("All ")) ? null : selected;
    }
}

// Custom Date Picker Component
class JDatePicker extends JPanel {
    private JSpinner dateSpinner;
    private LocalDate selectedDate;

    public JDatePicker() {
        setLayout(new BorderLayout());
        
        SpinnerDateModel model = new SpinnerDateModel();
        dateSpinner = new JSpinner(model);
        
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);

        add(dateSpinner, BorderLayout.CENTER);

        // Add a clear button
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> setDate(null));
        add(clearButton, BorderLayout.EAST);
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