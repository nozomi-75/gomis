package lyfjshs.gomis.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;

public class IncidentFillUpForm extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField nameTxt, ageTxt, reportedByTxt, contactTxt, sexTxt, guardianNameTxt, guardianNumberTxt, incidentTypeTxt, violatorTxt;
    private JTextArea narrativeReportTxt, actionTakenTxt, recommendationTxt;
    private Connection conn;

    public IncidentFillUpForm(Connection connectDB) {
        this.conn = connectDB;
        setTitle("INCIDENT Fill-Up Form");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new MigLayout("", "[grow][grow]", "[50px][300px][200px][150px][pref][pref]"));

        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("", "[grow]", "[]"));
        JLabel lblTitle = new JLabel("INCIDENT Fill-Up Form");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 32));
        headerPanel.add(lblTitle, "grow");
        add(headerPanel, "cell 0 0 2 1,grow");

        // Incident Details Panel
        JPanel detailsPanel = new JPanel(new MigLayout("", "[][][][][][][]", "[][][]"));
        detailsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(70, 130, 180)),
                "INCIDENT DETAILS", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 20), new Color(70, 130, 180)));

        detailsPanel.add(new JLabel("Name:"), "cell 0 0");
        nameTxt = new JTextField(30);
        detailsPanel.add(nameTxt, "growx");

        detailsPanel.add(new JLabel("Age:"), "cell 2 0");
        ageTxt = new JTextField(12);
        detailsPanel.add(ageTxt, "cell 3 0");

        detailsPanel.add(new JLabel("Reported By:"), "cell 4 0");
        reportedByTxt = new JTextField(30);
        detailsPanel.add(reportedByTxt, "growx");

        detailsPanel.add(new JLabel("Contact:"), "cell 0 1");
        contactTxt = new JTextField(30);
        detailsPanel.add(contactTxt, "growx");

        detailsPanel.add(new JLabel("Sex:"), "cell 2 1");
        sexTxt = new JTextField(12);
        detailsPanel.add(sexTxt, "cell 3 1");

        detailsPanel.add(new JLabel("Guardian:"), "cell 4 1");
        guardianNameTxt = new JTextField(30);
        detailsPanel.add(guardianNameTxt, "growx");

        detailsPanel.add(new JLabel("Guardian Contact Number:"), "cell 0 2");
        guardianNumberTxt = new JTextField(30);
        detailsPanel.add(guardianNumberTxt, "growx");

        detailsPanel.add(new JLabel("Type of Incident:"), "cell 4 2");
        incidentTypeTxt = new JTextField(30);
        detailsPanel.add(incidentTypeTxt, "growx");

        JButton studentSearchButton = new JButton("Student Search");
        studentSearchButton.setBackground(new Color(70, 130, 180));
        studentSearchButton.setForeground(Color.WHITE);
        studentSearchButton.setFont(new Font("Arial", Font.BOLD, 14));
        studentSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchStudent();
            }
        });
        detailsPanel.add(studentSearchButton, "cell 6 0");

        add(detailsPanel, "cell 0 1 2 1,grow");

        // Narrative Report
        narrativeReportTxt = new JTextArea(12, 60);
        JScrollPane narrativeScroll = new JScrollPane(narrativeReportTxt);
        narrativeScroll.setBorder(BorderFactory.createTitledBorder("Narrative Report:"));
        add(narrativeScroll, "cell 0 2 2 1,grow");

        // Action Taken and Recommendation
        JPanel actionPanel = new JPanel(new MigLayout("", "[grow][grow]", "[][grow]"));
        actionTakenTxt = new JTextArea(15, 50);
        JScrollPane actionScroll = new JScrollPane(actionTakenTxt);
        actionScroll.setBorder(BorderFactory.createTitledBorder("Action Taken:"));
        actionPanel.add(actionScroll, "cell 0 0,grow");

        recommendationTxt = new JTextArea(15, 50);
        JScrollPane recommendationScroll = new JScrollPane(recommendationTxt);
        recommendationScroll.setBorder(BorderFactory.createTitledBorder("Recommendation:"));
        actionPanel.add(recommendationScroll, "cell 1 0,grow");

        add(actionPanel, "cell 0 3 2 1,grow");

        // Violator Field
        JPanel violatorPanel = new JPanel(new MigLayout("", "[grow]", "[]"));
        violatorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Violator:",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 18), Color.BLACK));
        violatorTxt = new JTextField(25);
        violatorPanel.add(violatorTxt, "grow");
        add(violatorPanel, "cell 1 4,align right");

        // Submit Button
        JPanel footPanel = new JPanel(new MigLayout("", "[grow,fill][center]", "[]"));
        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBackground(new Color(0, 102, 204));
        submitButton.setForeground(Color.WHITE);
        footPanel.add(submitButton, "cell 1 0");
        add(footPanel, "cell 0 5 2 1,grow");
    }

    private void searchStudent() {
        JOptionPane.showMessageDialog(null, "Implementing student search functionality.");
    }

    public static void main(String[] args) {
        new IncidentFillUpForm(null).setVisible(true);
    }
}
