package lyfjshs.gomis.components.charts;

import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class ReportFilterPanel extends JPanel {
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final JFormattedTextField startDateField;
    private final JFormattedTextField endDateField;
    private final JComboBox<String> violationTypeComboBox;
    private final JButton applyBtn;
    private final JButton clearBtn;
    private FilterListener filterListener;

    public interface FilterListener {
        void onFilterChanged(LocalDate start, LocalDate end, String violationType);
    }

    public ReportFilterPanel() {
        super(new MigLayout("insets 10, fillx", "[][grow][][grow][][grow,fill][][]", "[]"));
        // Start Date
        add(new JLabel("Start Date:"));
        startDatePicker = new DatePicker();
        startDateField = new JFormattedTextField();
        startDatePicker.setEditor(startDateField);
        add(startDateField, "growx");

        // End Date
        add(new JLabel("End Date:"));
        endDatePicker = new DatePicker();
        endDateField = new JFormattedTextField();
        endDatePicker.setEditor(endDateField);
        add(endDateField, "growx");

        // Set default dates
        LocalDate today = LocalDate.now();
        startDatePicker.setSelectedDate(today.minusYears(3));
        endDatePicker.setSelectedDate(today);

        // Violation Type Filter
        add(new JLabel("Violation Type:"));
        violationTypeComboBox = new JComboBox<>(
            new String[] { "All", "Bullying", "Fighting/Weapons", "Vandalism", "Theft", "Cyberbullying",
                "Disrespect/Insubordination", "Substance Abuse", "Academic Dishonesty", "Other" });
        add(violationTypeComboBox, "growx");

        // Apply Button
        applyBtn = new JButton("Apply");
        add(applyBtn);

        // Clear Button
        clearBtn = new JButton("Clear");
        add(clearBtn);

        // Listeners
        applyBtn.addActionListener(e -> notifyListener());
        clearBtn.addActionListener(e -> {
            startDatePicker.setSelectedDate(today.minusYears(3));
            endDatePicker.setSelectedDate(today);
            violationTypeComboBox.setSelectedIndex(0);
            notifyListener();
        });
    }

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    private void notifyListener() {
        if (filterListener != null) {
            filterListener.onFilterChanged(
                startDatePicker.getSelectedDate(),
                endDatePicker.getSelectedDate(),
                (String) violationTypeComboBox.getSelectedItem()
            );
        }
    }

    public LocalDate getStartDate() { return startDatePicker.getSelectedDate(); }
    public LocalDate getEndDate() { return endDatePicker.getSelectedDate(); }
    public String getViolationType() { return (String) violationTypeComboBox.getSelectedItem(); }
} 