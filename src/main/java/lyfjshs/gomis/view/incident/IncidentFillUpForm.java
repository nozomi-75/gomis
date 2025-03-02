package lyfjshs.gomis.view.incident;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;
import lyfjshs.gomis.view.appointment.StudentSearchUI;

public class IncidentFillUpForm extends Form {

    private static final long serialVersionUID = 1L;
    private JTextField Nametxt;
    private JTextField txtAddress;
    private JTextField txtDateTime;
    private JTextField txtIncidentType;
    private JTextArea txtNarrativeReport;
    private JTextArea txtActionsTaken;
    private JTextArea txtRecommendations;
    private JTextField txtReportedBy;
    private JTextField guardianName;
    private JTextField GuardianNumbertxt;
    private JTextField contacttxt;
    private JTextField textField;
    private JTextField sextxt;
    private JTextField Reportedbytxt;
    private JTextField Narrativetxt;
    private JTextArea Narrativetxtfield;
    private JTextArea ActionTakentxt;
    private JTextArea Recommendationtxt;
    private Connection conn;

    public IncidentFillUpForm(Connection connectDB) {
        this.conn = connectDB;
        setLayout(new MigLayout("", "[grow][grow]", "[38px][][200px][200px][pref]"));

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("", "[grow]", "[]"));
        JLabel lblTitle = new JLabel("INCIDENT Fill-Up Form");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(lblTitle, "grow");
        add(headerPanel, "cell 0 0 2 1,grow");

        // Incident Details Panel
        JPanel detailsPanel = new JPanel(new MigLayout("", "[][grow][][][][grow]", "[][][]"));
        detailsPanel
                .setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 177)),
                        "INCIDENT DETAILS", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        new Font("Arial", Font.BOLD, 12), new Color(100, 149, 177)));

        // Add components to details panel
        detailsPanel.add(new JLabel("Name:"), "cell 0 0");
        detailsPanel.add(Nametxt = new JTextField(), "grow");

        JLabel ageLabel = new JLabel("Age:");
        detailsPanel.add(ageLabel, "cell 2 0,alignx trailing,aligny baseline");

        textField = new JTextField();
        detailsPanel.add(textField, "cell 3 0");
        textField.setColumns(10);

        JLabel ReportedbyLabel = new JLabel("Reported By:");
        detailsPanel.add(ReportedbyLabel, "cell 4 0,alignx left");

        Reportedbytxt = new JTextField();
        Reportedbytxt.setColumns(10);
        detailsPanel.add(Reportedbytxt, "cell 5 0,growx");

        JLabel Contacttxt = new JLabel("Contact:");
        detailsPanel.add(Contacttxt, "cell 0 1,alignx left");

        contacttxt = new JTextField();
        detailsPanel.add(contacttxt, "cell 1 1,growx");

        JLabel sexLabel = new JLabel("Sex:");
        detailsPanel.add(sexLabel, "cell 2 1,alignx trailing");

        sextxt = new JTextField();
        sextxt.setColumns(10);
        detailsPanel.add(sextxt, "cell 3 1");

        JLabel GuardianLabel = new JLabel("Guardian:");
        detailsPanel.add(GuardianLabel, "cell 4 1,alignx left");

        guardianName = new JTextField();
        detailsPanel.add(guardianName, "cell 5 1,growx");

        JLabel GuardianNumber = new JLabel("Guardian Contact Number:");
        detailsPanel.add(GuardianNumber, "cell 0 2,alignx trailing");

        GuardianNumbertxt = new JTextField();
        detailsPanel.add(GuardianNumbertxt, "cell 1 2,growx");
        JLabel label_1 = new JLabel("Type of Incident:");
        detailsPanel.add(label_1, "cell 4 2");
        detailsPanel.add(txtIncidentType = new JTextField(), "grow");

        add(detailsPanel, "cell 0 1 2 1,grow");

        JPanel Narrativepanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
        Narrativepanel.add(new JLabel("Narrative Report"), "cell 0 0");
        Narrativetxtfield = new JTextArea();
        Narrativetxtfield.setLineWrap(true);
        Narrativetxtfield.setWrapStyleWord(true);
        JScrollPane narativeSPane = new JScrollPane(Narrativetxtfield);
        narativeSPane
                .setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 177)),
                        "Narrative Report:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        new Font("Tahoma", Font.BOLD, 17), new Color(0, 0, 0)));

        add(narativeSPane, "cell 0 2 2 1,grow");

        JPanel Actionpanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
        Actionpanel.add(new JLabel("Action Taken"), "cell 0 0");
        ActionTakentxt = new JTextArea();
        ActionTakentxt.setLineWrap(true);
        ActionTakentxt.setWrapStyleWord(true);
        JScrollPane actionsSPane = new JScrollPane(ActionTakentxt);
        actionsSPane
                .setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 177)),
                        "Action Taken:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        new Font("Tahoma", Font.BOLD, 17), new Color(0, 0, 0)));
        add(actionsSPane, "cell 0 3,grow");

        JPanel Recommendpanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
        Recommendpanel.add(new JLabel("Recommendation"), "cell 0 0");
        Recommendationtxt = new JTextArea();
        Recommendationtxt.setLineWrap(true);
        Recommendationtxt.setWrapStyleWord(true);
        JScrollPane recommSPane = new JScrollPane(Recommendationtxt);
        recommSPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 177)),
                "Recommendation:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 17), new Color(0, 0, 0)));
        add(recommSPane, "cell 1 3,grow");

        JPanel footPanel = new JPanel(new MigLayout("", "[grow,fill][center]", "[]"));
        add(footPanel, "cell 0 4 2 1,grow");

        JButton btnNewButton = new JButton("Submit");
        footPanel.add(btnNewButton, "cell 1 0");

        // Add Student Search Button
        JButton studentSearchButton = new JButton("Student Search");
        detailsPanel.add(studentSearchButton, "cell 6 0");

        // Add action listener to the Student Search button
        studentSearchButton.addActionListener(e -> openStudentSearchUI());
    }

    private void openStudentSearchUI() {
        StudentSearchUI studentSearchUI = new StudentSearchUI();
        studentSearchUI.createAndShowGUI();
    }
}