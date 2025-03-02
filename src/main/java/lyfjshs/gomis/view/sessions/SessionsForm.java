package lyfjshs.gomis.view.sessions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.view.appointment.StudentSearchUI;
import net.miginfocom.swing.MigLayout;

public class SessionsForm extends Form implements Printable {
    private JTextField dateField, violationField, recordedByField;
    private JFormattedTextField startSessionTimeField, endSessionTimeField;
    private JTextArea sessionSummaryArea, notesArea;
    private JButton saveButton, printButton, searchStudentButton;
    private JComboBox<String> participantsComboBox;
    private Connection connect;

    public SessionsForm(Connection conn) {
        this.connect = conn;
        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {

        dateField = new JTextField(20);
        participantsComboBox = new JComboBox<>(new String[]{"Student", "Non-Student"});
        participantsComboBox.addActionListener(e -> toggleSearchStudentButton());
        notesArea = new JTextArea(5, 20);
        sessionSummaryArea = new JTextArea(8, 50);

        saveButton = new JButton("SAVE");
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveSession());

        printButton = new JButton("PRINT");
        printButton.setBackground(new Color(70, 130, 180));
        printButton.setForeground(Color.WHITE);
        printButton.setFocusPainted(false);
        printButton.addActionListener(e -> printSessionDetails());

        searchStudentButton = new JButton("Search Student");
        searchStudentButton.setEnabled(false);
        searchStudentButton.addActionListener(e -> openStudentSearchUI());
    }

    private void toggleSearchStudentButton() {
        searchStudentButton.setEnabled("Student".equals(participantsComboBox.getSelectedItem()));
    }

    private void openStudentSearchUI() {
        StudentSearchUI studentSearchUI = new StudentSearchUI();
        studentSearchUI.createAndShowGUI();
    }

    private void saveSession() {
        try {
            String date = dateField.getText();
            String participants = (String) participantsComboBox.getSelectedItem();
            String violation = violationField.getText();
            String recordedBy = recordedByField.getText();
            String notes = notesArea.getText();
            String summary = sessionSummaryArea.getText();

            // Create a new Session object using the constructor
            Sessions session = new Sessions(
            	0,
                0, // appointmentId (not retrieved in the query)
                0, // counselorsId (not retrieved in the query)
                participants.equals("Student") ? 1 : 0, // Use 1 for Student, 0 for Non-Student
                0, // violationId (not retrieved in the query)
                violation, // sessionType
                null, // sessionDateTime (not retrieved in the query)
                notes, // sessionNotes
                "Active", // sessionStatus
                new java.sql.Timestamp(System.currentTimeMillis()) // updatedAt
            );

            // Use SessionsDAO to save the session
            SessionsDAO sessionsDAO = new SessionsDAO(connect);
            sessionsDAO.addSession(session); // Assuming you have an addSession method in SessionsDAO

            JOptionPane.showMessageDialog(this, "Session saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving session: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void layoutComponents() {
        this.setLayout(new MigLayout("wrap 4", "[][right]10[grow]20[right]10[grow]", "[][][][][][][][][fill][]"));
        this.setBorder(BorderFactory.createTitledBorder("Session Form"));
        this.add(new JLabel("DATE:"), "cell 3 0");
        this.add(dateField, "cell 4 0");

        this.add(new JLabel("PARTICIPANTS:"), "cell 1 1");
        this.add(participantsComboBox, "cell 2 1");
        this.add(new JLabel("NOTES:"), "cell 3 1,aligny top");
        this.add(new JScrollPane(notesArea), "cell 4 1 1 2,grow");

        this.add(searchStudentButton, "cell 2 2");

        JLabel label_3 = new JLabel("START SESSION TIME:");
        this.add(label_3, "cell 1 3");
        startSessionTimeField = new JFormattedTextField();
        startSessionTimeField.setColumns(20);
        this.add(startSessionTimeField, "cell 2 3,alignx left");

        JLabel label_4 = new JLabel("VIOLATION:");
        this.add(label_4, "cell 3 3");
        violationField = new JTextField(20);
        this.add(violationField, "cell 4 3");

        JLabel label = new JLabel("END SESSION TIME:");
        this.add(label, "flowx,cell 1 4");
        endSessionTimeField = new JFormattedTextField();
        endSessionTimeField.setColumns(20);
        this.add(endSessionTimeField, "cell 2 4,alignx left");

        JLabel label_2 = new JLabel("RECORDED BY:");
        this.add(label_2, "cell 3 4");
        recordedByField = new JTextField(20);
        this.add(recordedByField, "cell 4 4");

        JLabel label_1 = new JLabel("SESSION SUMMARY:");
        this.add(label_1, "cell 0 6 2 1,alignx center");
        this.add(new JScrollPane(sessionSummaryArea), "cell 0 7 5 1,grow");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(printButton);
        this.add(buttonPanel, "cell 1 8 4 1,alignx center,growy");
    }

    private void printSessionDetails() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(this);
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Printing Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        this.printAll(g);
        return PAGE_EXISTS;
    }
}
