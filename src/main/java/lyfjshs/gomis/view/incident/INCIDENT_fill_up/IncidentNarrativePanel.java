package lyfjshs.gomis.view.incident.INCIDENT_fill_up;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.miginfocom.swing.MigLayout;

public class IncidentNarrativePanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(IncidentNarrativePanel.class);
    private JTextArea narrativeReportField;

    public IncidentNarrativePanel() {
        setOpaque(false);
        setLayout(new MigLayout("", "[grow]", "[][grow]"));
        initComponents();
    }

    private void initComponents() {
        JLabel label = new JLabel("Describe the incident in detail. (What happened, who was involved, where, when, and why?)");
        add(label, "cell 0 0,wrap");
        narrativeReportField = new JTextArea();
        narrativeReportField.setLineWrap(true);
        narrativeReportField.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(narrativeReportField);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 120));
        add(scrollPane, "cell 0 1,grow");
    }

    public String getNarrativeReport() { return narrativeReportField.getText(); }
    public void setNarrativeReport(String text) { narrativeReportField.setText(text); }

    public void clearFields() {
        narrativeReportField.setText("");
    }

    public boolean isValidPanel() {
        boolean valid = narrativeReportField.getText().trim().length() >= 10;
        if (!valid) {
            logger.warn("Narrative validation failed");
        }
        return valid;
    }

    private void onNarrativeChange(String newText) {
        logger.info("Narrative changed: {}", newText);
    }
} 