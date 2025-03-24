package lyfjshs.gomis.view.incident;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.entity.Incident;
import net.miginfocom.swing.MigLayout;

public class IncidentFullData extends JPanel {
    private Incident incident;
    private IncidentsDAO incidentsDAO;
    private Runnable backCallback;
    private Runnable refreshCallback;

    // UI components
    private JTextField nameField;
    private JTextField participantTypeField;
    private JTextField sexField;
    private JTextField contactField;
    private JTextField dateField;
    private JTextField statusField;
    private JTextArea descriptionField;
    private JTextArea narrativeField;
    private JTextArea actionTakenField;
    private JTextArea recommendationField;

    public IncidentFullData(Incident incident, IncidentsDAO dao, Runnable backCallback, Runnable refreshCallback) {
        this.incident = incident;
        this.incidentsDAO = dao;
        this.backCallback = backCallback;
        this.refreshCallback = refreshCallback;

        setupGUI();
    }

    private void setupGUI() {
        setLayout(new MigLayout("fillx, insets 20, wrap 2", "[][grow,fill]", "[][][]10[]10[]10[][][][]"));
        String participantName = incident.getParticipants().getParticipantFirstName() + " "
                + incident.getParticipants().getParticipantLastName();
        JButton backButton = new JButton("Back");
        add(backButton, "cell 0 0,alignx left");
        backButton.addActionListener(e -> backCallback.run());
        JLabel lblTitle = new JLabel("INCIDENT DETAILS");
        add(lblTitle, "cell 0 1 2 1,alignx center");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel lblSubtitle = new JLabel(
                "Participant: " + participantName + " | Incident: " + incident.getIncidentDescription());
        add(lblSubtitle, "cell 0 2 2 1,alignx center");
        lblSubtitle.setFont(new Font("Arial", Font.ITALIC, 14));

        // Form fields panel
        JPanel formFieldsPanel = new JPanel(
                new MigLayout("fillx, insets 10", "[right][grow,fill][right][grow,fill]", "[][][]"));
        formFieldsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237)),
                "INCIDENT DETAILS", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12), new Color(100, 149, 237)));

        nameField = new JTextField(incident.getParticipants().getParticipantFirstName() + " "
                + incident.getParticipants().getParticipantLastName());
        participantTypeField = new JTextField(incident.getParticipants().getParticipantType());
        sexField = new JTextField(incident.getParticipants().getSex());
        contactField = new JTextField(incident.getParticipants().getContactNumber());
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(incident.getIncidentDate()));
        statusField = new JTextField(incident.getStatus());
        descriptionField = new JTextArea(incident.getIncidentDescription());

        formFieldsPanel.add(new JLabel("Name:"), "right");
        formFieldsPanel.add(nameField, "growx");
        formFieldsPanel.add(new JLabel("Type:"), "right");
        formFieldsPanel.add(participantTypeField, "growx, wrap");

        formFieldsPanel.add(new JLabel("Sex:"), "right");
        formFieldsPanel.add(sexField, "growx");
        formFieldsPanel.add(new JLabel("Contact:"), "right");
        formFieldsPanel.add(contactField, "growx, wrap");

        formFieldsPanel.add(new JLabel("Date:"), "right");
        formFieldsPanel.add(dateField, "growx");
        formFieldsPanel.add(new JLabel("Status:"), "right");
        formFieldsPanel.add(statusField, "growx, wrap");

        add(formFieldsPanel, "cell 0 4 2 1,grow");

        // Narrative panel
        narrativeField = new JTextArea(incident.getNarrative());
        narrativeField.setLineWrap(true);
        narrativeField.setWrapStyleWord(true);
        narrativeField.setEditable(false);
        JScrollPane narrativeScrollPane = new JScrollPane(narrativeField);
        add(narrativeScrollPane, "cell 0 5 2 1,grow");
        narrativeScrollPane.setPreferredSize(new Dimension(0, 100));
        narrativeScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237)),
                "Narrative Report:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 17), new Color(0, 0, 0)));

        // Action & Recommendation panel
        actionTakenField = new JTextArea(incident.getActionTaken());
        actionTakenField.setLineWrap(true);
        actionTakenField.setWrapStyleWord(true);
        actionTakenField.setEditable(false);
        JScrollPane actionTakenScrollPane = new JScrollPane(actionTakenField);
        actionTakenScrollPane.setPreferredSize(new Dimension(0, 100));
        actionTakenScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237)),
                "Action Taken:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 17), new Color(0, 0, 0)));
        JPanel actionTakenPanel = new JPanel(new MigLayout("fill"));
        actionTakenPanel.add(actionTakenScrollPane, "grow");

        recommendationField = new JTextArea(incident.getRecommendation());
        recommendationField.setLineWrap(true);
        recommendationField.setWrapStyleWord(true);
        recommendationField.setEditable(false);
        JScrollPane recommendationScrollPane = new JScrollPane(recommendationField);
        recommendationScrollPane.setPreferredSize(new Dimension(0, 100));
        recommendationScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237)),
                "Recommendation:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 17), new Color(0, 0, 0)));
        JPanel recommendationPanel = new JPanel(new MigLayout("fill"));
        recommendationPanel.add(recommendationScrollPane, "grow");

        JPanel actionRecommendationPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow,fill]10[grow,fill]", "[]"));
        actionRecommendationPanel.add(actionTakenPanel, "grow");
        actionRecommendationPanel.add(recommendationPanel, "grow");
        add(actionRecommendationPanel, "cell 0 6 2 1,grow");

        // Set initial read-only state
        setFieldsReadOnly();
    }

    private void setFieldsReadOnly() {
        nameField.setEditable(false);
        participantTypeField.setEditable(false);
        sexField.setEditable(false);
        contactField.setEditable(false);
        dateField.setEditable(false);
        statusField.setEditable(false);
        descriptionField.setEditable(false);
        narrativeField.setEditable(false);
        actionTakenField.setEditable(false);
        recommendationField.setEditable(false);
    }
}