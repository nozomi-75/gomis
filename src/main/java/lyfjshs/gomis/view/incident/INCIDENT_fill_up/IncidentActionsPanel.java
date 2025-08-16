package lyfjshs.gomis.view.incident.INCIDENT_fill_up;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.miginfocom.swing.MigLayout;

public class IncidentActionsPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(IncidentActionsPanel.class);
    private JTextArea actionsTakenField;
    private JTextArea recommendationsField;

    public IncidentActionsPanel() {
        setOpaque(false);
        setLayout(new MigLayout("", "[grow][grow]", "[][grow]"));
        initComponents();
    }

    private void initComponents() {
        JLabel actionsLabel = new JLabel("Action Taken");
        add(actionsLabel, "cell 0 0");
        JLabel recommendationsLabel = new JLabel("Recommendation");
        add(recommendationsLabel, "cell 1 0");
        actionsTakenField = new JTextArea();
        actionsTakenField.setLineWrap(true);
        actionsTakenField.setWrapStyleWord(true);
        JScrollPane actionsScroll = new JScrollPane(actionsTakenField);
        actionsScroll.setPreferredSize(new java.awt.Dimension(200, 100));
        add(actionsScroll, "cell 0 1,grow");
        recommendationsField = new JTextArea();
        recommendationsField.setLineWrap(true);
        recommendationsField.setWrapStyleWord(true);
        JScrollPane recommendationsScroll = new JScrollPane(recommendationsField);
        recommendationsScroll.setPreferredSize(new java.awt.Dimension(200, 100));
        add(recommendationsScroll, "cell 1 1,grow");
    }

    public String getActionsTaken() { return actionsTakenField.getText(); }
    public void setActionsTaken(String text) { actionsTakenField.setText(text); }
    public String getRecommendations() { return recommendationsField.getText(); }
    public void setRecommendations(String text) { recommendationsField.setText(text); }

    public void clearFields() {
        actionsTakenField.setText("");
        recommendationsField.setText("");
    }

    public boolean isValidPanel() {
        boolean valid = actionsTakenField.getText().trim().length() >= 10 && recommendationsField.getText().trim().length() >= 10;
        if (!valid) {
            logger.warn("Actions validation failed");
        }
        return valid;
    }

    private void onActionChange(String action, String value) {
        logger.info("Action '{}' changed to: {}", action, value);
    }
} 