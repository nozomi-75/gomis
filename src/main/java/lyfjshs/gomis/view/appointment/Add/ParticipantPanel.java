package lyfjshs.gomis.view.appointment.Add;

import java.awt.Color;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class ParticipantPanel extends JPanel {
    private JComboBox<String> typeComboBox;
    private StudentParticipantPanel studentPanel;
    private NonStudentParticipantPanel nonStudentPanel;

    public ParticipantPanel() {
        setLayout(new MigLayout("wrap 1", "[grow]", "[][]"));
        setBorder(BorderFactory.createEtchedBorder());

        // Dropdown for selecting participant type
        typeComboBox = new JComboBox<>(new String[]{"Student", "Non-Student"});
        add(new JLabel("Type:"));
        add(typeComboBox, "growx");

        studentPanel = new StudentParticipantPanel();
        nonStudentPanel = new NonStudentParticipantPanel();
        add(studentPanel, "growx");
        add(nonStudentPanel, "growx");

        nonStudentPanel.setVisible(false);

        typeComboBox.addActionListener(e -> switchPanel());

        // Remove Button
        JButton removeButton = new JButton("Remove Participant");
        removeButton.setBackground(new Color(244, 67, 54));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).remove(this);
            SwingUtilities.getWindowAncestor(this).revalidate();
            SwingUtilities.getWindowAncestor(this).repaint();
        });
        add(removeButton, "alignx center");
    }

    private void switchPanel() {
        boolean isStudent = typeComboBox.getSelectedItem().equals("Student");
        studentPanel.setVisible(isStudent);
        nonStudentPanel.setVisible(!isStudent);
        revalidate();
        repaint();
    }

    public StudentParticipantPanel getStudentPanel() {
        return studentPanel;
    }

    public NonStudentParticipantPanel getNonStudentPanel() {
        return nonStudentPanel;
    }

    public String getParticipantType() {
        return (String) typeComboBox.getSelectedItem();
    }
}

