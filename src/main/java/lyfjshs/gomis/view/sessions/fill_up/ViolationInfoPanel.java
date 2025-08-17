/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.sessions.fill_up;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public class ViolationInfoPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(ViolationInfoPanel.class);
    private final JComboBox<String> violationTypeCombo = new JComboBox<>(new String[]{
        "-- Select Violation --",
        "No Violation",
        "Absence/Late",
        "Minor Property Damage",
        "Threatening/Intimidating",
        "Pornographic Materials",
        "Gadget Use in Class",
        "Cheating",
        "Stealing",
        "No Pass",
        "Bullying",
        "Sexual Abuse",
        "Illegal Drugs",
        "Alcohol",
        "Smoking/Vaping",
        "Gambling",
        "Public Display of Affection",
        "Fighting/Weapons",
        "Severe Property Damage",
        "Others"
    });
    private final JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
        "-- Select Category --",
        "No Violation",
        "Physical",
        "Verbal",
        "Emotional",
        "Sexual",
        "Cyber",
        "Others"
    });
    private final JTextArea descriptionArea = new JTextArea(4, 0);
    private final JTextArea reinforcementArea = new JTextArea(4, 0);
    private final JFormattedTextField otherViolationField = new JFormattedTextField();
    private final JLabel categoryLabel = new JLabel("Category");
    private final JLabel otherViolationLabel = new JLabel("Other Violation");
    private final JPanel content;
    private final JFormattedTextField otherCategoryField = new JFormattedTextField();
    private final JLabel otherCategoryLabel = new JLabel("Other Category");

    public ViolationInfoPanel() {
        setOpaque(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Describe the violation using WH questions: What, Who, Why, When, Where, How...");
        reinforcementArea.setLineWrap(true);
        reinforcementArea.setWrapStyleWord(true);
        reinforcementArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "What action will be taken? (e.g., community service, counseling sessions, parent conference, etc.)");
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.putClientProperty("JComponent.arc", 8);
        JScrollPane reinfScroll = new JScrollPane(reinforcementArea);
        reinfScroll.setBorder(BorderFactory.createEmptyBorder());
        reinfScroll.putClientProperty("JComponent.arc", 8);
        content = new JPanel(new MigLayout("fillx, insets 0", "[grow][grow]", "[][][][][][][][]"));
        content.setOpaque(false);
        JLabel label = new JLabel("Violation Type");
        content.add(label, "flowx,cell 0 0");
        content.add(categoryLabel, "cell 1 0");
        content.add(violationTypeCombo, "flowx,cell 0 1,grow");
        content.add(categoryCombo, "cell 1 1,growx");
        categoryCombo.addActionListener(e -> updatePanelsVisibility());
        content.add(otherViolationLabel, "cell 0 2");
        content.add(otherCategoryLabel, "cell 1 2");
        content.add(otherViolationField, "cell 0 3,growx");
        content.add(otherCategoryField, "cell 1 3,growx");
        content.add(new JLabel("Violation Description"), "cell 0 4 2 1");
        content.add(descScroll, "cell 0 5 2 1,growx,hmin 80");
        content.add(new JLabel("Reinforcement/Intervention"), "cell 0 6 2 1");
        content.add(reinfScroll, "cell 0 7 2 1,growx,hmin 80");
        updatePanelsVisibility();
        violationTypeCombo.addActionListener(e -> updatePanelsVisibility());
    }
    private void updatePanelsVisibility() {
        String selectedViolation = (String) violationTypeCombo.getSelectedItem();
        String selectedCategory = (String) categoryCombo.getSelectedItem();

        // Hide all optional fields by default
        otherCategoryField.setVisible(false);
        otherCategoryLabel.setVisible(false);
        otherViolationField.setVisible(false);
        otherViolationLabel.setVisible(false);
        categoryCombo.setVisible(false);
        categoryLabel.setVisible(false);

        // Show category combo and label for Bullying or Others
        if ("Bullying".equals(selectedViolation) || "Others".equals(selectedViolation)) {
            categoryCombo.setVisible(true);
            categoryLabel.setVisible(true);
        }

        // Show 'Other Violation' field and label if violation is Others
        if ("Others".equals(selectedViolation)) {
            otherViolationField.setVisible(true);
            otherViolationLabel.setVisible(true);
        }

        // Show 'Other Category' field and label if category is Others (and category combo is visible)
        if (categoryCombo.isVisible() && "Others".equals(selectedCategory)) {
            otherCategoryField.setVisible(true);
            otherCategoryLabel.setVisible(true);
        }
    }
    public String getViolationType() {
        String selected = (String) violationTypeCombo.getSelectedItem();
        if ("Other".equals(selected)) {
            return otherViolationField.getText().trim();
        }
        return selected;
    }
    public String getCategory() { return (String) categoryCombo.getSelectedItem(); }
    public String getDescription() { return descriptionArea.getText(); }
    public String getReinforcement() { return reinforcementArea.getText(); }
    public void setViolationType(String type) { violationTypeCombo.setSelectedItem(type); updatePanelsVisibility(); }
    public void setCategory(String category) { categoryCombo.setSelectedItem(category); updatePanelsVisibility(); }
    public void setDescription(String desc) { descriptionArea.setText(desc); }
    public void setReinforcement(String reinforcement) { reinforcementArea.setText(reinforcement); }
    public void setOtherViolation(String other) { otherViolationField.setText(other); }

    public void clearFields() {
        setViolationType("");
        setCategory("");
        setDescription("");
        setReinforcement("");
        setOtherViolation("");
    }

    public JPanel getContentPanel() { return content; }

    public void getData(SessionFormData data) {
        data.violationType = getViolationType();
        data.category = getCategory();
        data.description = getDescription();
        data.reinforcement = getReinforcement();
        // If 'Other' violation, also store the text
        if ("Other".equals(getViolationType())) {
            data.otherViolation = otherViolationField.getText();
        } else {
            data.otherViolation = null;
        }
    }
    public void setData(SessionFormData data) {
        setViolationType(data.violationType);
        setCategory(data.category);
        setDescription(data.description);
        setReinforcement(data.reinforcement);
        if ("Other".equals(data.violationType)) {
            setOtherViolation(data.otherViolation);
        }
    }

} 