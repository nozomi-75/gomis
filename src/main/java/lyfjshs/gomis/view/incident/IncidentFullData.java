/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.incident;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Participants;
import net.miginfocom.swing.MigLayout;

public class IncidentFullData extends JPanel {
    private static final Logger logger = LogManager.getLogger(IncidentFullData.class);

    private Incident incident;
    private Runnable backCallback;
    private IncidentsDAO incidentsDAO;
    private List<Participants> incidentParticipants;
    private DefaultTableModel participantTableModel;
    private JTable participantTable;

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
        this.backCallback = backCallback;
        this.incidentsDAO = dao;
        try {
            this.incidentParticipants = incidentsDAO.getParticipantsByIncidentId(incident.getIncidentId());
        } catch (SQLException e) {
            logger.error("Error loading incident data", e);
            JOptionPane.showMessageDialog(this, "Error loading incident data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.incidentParticipants = new ArrayList<>();
        }

        setupGUI();
    }

    private void setupGUI() {
        setLayout(new MigLayout("fillx, insets 20, wrap 2", "[][grow,fill]", "[][][]10[]10[]10[][][][][]"));
        
        String reporterName = incident.getParticipants().getParticipantFirstName() + " "
                + incident.getParticipants().getParticipantLastName();

        JButton backButton = new JButton("Back");
        add(backButton, "cell 0 0,alignx left");
        backButton.addActionListener(e -> backCallback.run());
        
        JLabel lblTitle = new JLabel("INCIDENT DETAILS");
        add(lblTitle, "cell 0 1 2 1,alignx center");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        
        JLabel lblSubtitle = new JLabel(
                "Reporter: " + reporterName + " | Incident Description: " + incident.getIncidentDescription());
        add(lblSubtitle, "cell 0 2 2 1,alignx center");
        lblSubtitle.setFont(new Font("Arial", Font.ITALIC, 14));

        JPanel formFieldsPanel = new JPanel(
                new MigLayout("fillx, insets 10", "[right][grow,fill][right][grow,fill]", "[][][]"));
        formFieldsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237)),
                "INCIDENT MAIN DATA", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12), new Color(100, 149, 237)));

        nameField = new JTextField(incident.getParticipants().getParticipantFirstName() + " "
                + incident.getParticipants().getParticipantLastName());
        participantTypeField = new JTextField(incident.getParticipants().getParticipantType());
        sexField = new JTextField(incident.getParticipants().getSex());
        contactField = new JTextField(incident.getParticipants().getContactNumber());
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(incident.getIncidentDate()));
        statusField = new JTextField(incident.getStatus());

        formFieldsPanel.add(new JLabel("Reporter Name:"), "right");
        formFieldsPanel.add(nameField, "growx");
        formFieldsPanel.add(new JLabel("Reporter Type:"), "right");
        formFieldsPanel.add(participantTypeField, "growx, wrap");

        formFieldsPanel.add(new JLabel("Reporter Sex:"), "right");
        formFieldsPanel.add(sexField, "growx");
        formFieldsPanel.add(new JLabel("Reporter Contact:"), "right");
        formFieldsPanel.add(contactField, "growx, wrap");

        formFieldsPanel.add(new JLabel("Incident Date:"), "right");
        formFieldsPanel.add(dateField, "growx");
        formFieldsPanel.add(new JLabel("Incident Status:"), "right");
        formFieldsPanel.add(statusField, "growx, wrap");

        add(formFieldsPanel, "cell 0 3 2 1,grow");

        JPanel participantsPanel = new JPanel(new MigLayout("fillx, insets 10", "[grow,fill]", "[][grow,fill]"));
        participantsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237)),
                "INCIDENT PARTICIPANTS", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12), new Color(100, 149, 237)));

        participantTableModel = new DefaultTableModel(
                new Object[]{"Name", "Type", "Sex", "Contact"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        participantTable = new JTable(participantTableModel);
        JScrollPane participantScrollPane = new JScrollPane(participantTable);
        participantsPanel.add(participantScrollPane, "grow, wrap");
        add(participantsPanel, "cell 0 4 2 1,grow");

        populateParticipantTable();

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

        setFieldsReadOnly();
    }
    
    private void populateParticipantTable() {
        if (incidentParticipants != null) {
            for (Participants p : incidentParticipants) {
                participantTableModel.addRow(new Object[]{
                    p.getParticipantFirstName() + " " + p.getParticipantLastName(),
                    p.getParticipantType(),
                    p.getSex(),
                    p.getContactNumber()
                });
            }
        }
    }

    private void setFieldsReadOnly() {
        nameField.setEditable(false);
        participantTypeField.setEditable(false);
        sexField.setEditable(false);
        contactField.setEditable(false);
        dateField.setEditable(false);
        statusField.setEditable(false);
        narrativeField.setEditable(false);
        actionTakenField.setEditable(false);
        recommendationField.setEditable(false);
        if (participantTable != null) {
            participantTable.setEnabled(false);
        }
    }
}