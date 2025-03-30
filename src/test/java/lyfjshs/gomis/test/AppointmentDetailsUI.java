package lyfjshs.gomis.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;

public class AppointmentDetailsUI extends JPanel {
	public AppointmentDetailsUI() {

		// Set layout with MigLayout - simplified row configuration
		setLayout(new MigLayout("wrap, fillx", "[grow]", "[]20[]20[]20[200px]20[]20[]20[]20[][]"));

		// **Appointment Details Section**
		JPanel appointmentDetailsHeader = createSectionHeader("Appointment Details", "Appointment ID here!");
		add(appointmentDetailsHeader, "cell 0 0, growx");

		JPanel detailsGrid = new JPanel(new MigLayout("wrap 2, gap 15", "[grow][grow]"));
		detailsGrid.setOpaque(false);

		// Manually add Appointment Title
		JPanel titlePanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		titlePanel.setOpaque(false);
		JLabel titleLabel = new JLabel("Appointment Title");
		titleLabel.setForeground(new Color(44, 62, 80));
		titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
		titlePanel.add(titleLabel, "growx");
		JLabel titleValue = new JLabel("Career Guidance Consultation");
		titleValue.setBackground(new Color(248, 249, 250));
		titleValue.setOpaque(true);
		titleValue
				.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)),
						BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		titleValue.setFont(new Font("Arial", Font.PLAIN, 12));
		titlePanel.add(titleValue, "growx");
		detailsGrid.add(titlePanel, "cell 0 0, grow");

		// Manually add Consultation Type
		JPanel typePanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		typePanel.setOpaque(false);
		JLabel typeLabel = new JLabel("Consultation Type");
		typeLabel.setForeground(new Color(44, 62, 80));
		typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
		typePanel.add(typeLabel, "growx");
		JLabel typeValue = new JLabel("Personal Consultation");
		typeValue.setBackground(new Color(248, 249, 250));
		typeValue.setOpaque(true);
		typeValue.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)),
				BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		typeValue.setFont(new Font("Arial", Font.PLAIN, 12));
		typePanel.add(typeValue, "growx");
		detailsGrid.add(typePanel, "cell 1 0, grow");

		// Manually add Date & Time
		JPanel datePanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		datePanel.setOpaque(false);
		JLabel dateLabel = new JLabel("Date & Time");
		dateLabel.setForeground(new Color(44, 62, 80));
		dateLabel.setFont(new Font("Arial", Font.BOLD, 12));
		datePanel.add(dateLabel, "growx");
		JLabel dateValue = new JLabel("2025-03-21 10:00 AM");
		dateValue.setBackground(new Color(248, 249, 250));
		dateValue.setOpaque(true);
		dateValue.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)),
				BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		dateValue.setFont(new Font("Arial", Font.PLAIN, 12));
		datePanel.add(dateValue, "growx");
		detailsGrid.add(datePanel, "cell 0 1, grow");

		// Manually add Status
		JPanel statusPanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		statusPanel.setOpaque(false);
		JLabel statusLabel = new JLabel("Status");
		statusLabel.setForeground(new Color(44, 62, 80));
		statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
		statusPanel.add(statusLabel, "growx");
		JLabel statusBadge = new JLabel("on-going");
		statusBadge.setOpaque(true);
		statusBadge.setBackground(new Color(243, 156, 18));
		statusBadge.setForeground(Color.WHITE);
		statusBadge.setHorizontalAlignment(JLabel.CENTER);
		statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		statusPanel.add(statusBadge, "growx");
		detailsGrid.add(statusPanel, "cell 1 1, grow");

		add(detailsGrid, "cell 0 1, growx");

		// **Participants Section**
		JPanel participantsHeader = createSectionHeader("Participants", null);
		add(participantsHeader, "cell 0 2, growx");

		// Participants Table
		String[] columnNames = { "Name", "Participant Type", "Contact Number", "Gender" };
		Object[][] data = { { "Juan Dela Cruz", "Student", "+63 912 345 6789", "Male" },
				{ "Maria Santos", "Parent", "+63 987 654 3210", "Female" } };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable participantsTable = new JTable(model);
		participantsTable.setRowHeight(40);
		participantsTable.setShowGrid(true);
		participantsTable.setGridColor(new Color(221, 221, 221));

		// Custom header renderer
		participantsTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				label.setBackground(new Color(241, 243, 245));
				label.setForeground(new Color(44, 62, 80));
				label.setFont(new Font("Arial", Font.BOLD, 12));
				label.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 221, 221)),
						BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				return label;
			}
		});

		// Custom cell renderer
		participantsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				label.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 221, 221)),
						BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				return label;
			}
		});

		JScrollPane tableScrollPane = new JScrollPane(participantsTable);
		add(tableScrollPane, "cell 0 3, growx");

		// **Guidance Counselor Information Section**
		JPanel counselorHeader = createSectionHeader("Guidance Counselor Information", null);
		add(counselorHeader, "cell 0 4, growx");

		JPanel counselorGrid = new JPanel(new MigLayout("wrap 2, gap 15", "[grow][grow]"));
		counselorGrid.setOpaque(false);

		// Manually add Name
		JPanel namePanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		namePanel.setOpaque(false);
		JLabel nameLabel = new JLabel("Name");
		nameLabel.setForeground(new Color(44, 62, 80));
		nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
		namePanel.add(nameLabel, "growx");
		JLabel nameValue = new JLabel("Alice B. Smith");
		nameValue.setBackground(new Color(248, 249, 250));
		nameValue.setOpaque(true);
		nameValue.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)),
				BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		nameValue.setFont(new Font("Arial", Font.PLAIN, 12));
		namePanel.add(nameValue, "growx");
		counselorGrid.add(namePanel, "cell 0 0, grow");

		// Manually add Specialization
		JPanel specPanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		specPanel.setOpaque(false);
		JLabel specLabel = new JLabel("Specialization");
		specLabel.setForeground(new Color(44, 62, 80));
		specLabel.setFont(new Font("Arial", Font.BOLD, 12));
		specPanel.add(specLabel, "growx");
		JLabel specValue = new JLabel("Career Counseling");
		specValue.setBackground(new Color(248, 249, 250));
		specValue.setOpaque(true);
		specValue.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)),
				BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		specValue.setFont(new Font("Arial", Font.PLAIN, 12));
		specPanel.add(specValue, "growx");
		counselorGrid.add(specPanel, "cell 1 0, grow");

		// Manually add Contact Number
		JPanel contactPanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		contactPanel.setOpaque(false);
		JLabel contactLabel = new JLabel("Contact Number");
		contactLabel.setForeground(new Color(44, 62, 80));
		contactLabel.setFont(new Font("Arial", Font.BOLD, 12));
		contactPanel.add(contactLabel, "growx");
		JLabel contactValue = new JLabel("+63 987 654 3210");
		contactValue.setBackground(new Color(248, 249, 250));
		contactValue.setOpaque(true);
		contactValue
				.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)),
						BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		contactValue.setFont(new Font("Arial", Font.PLAIN, 12));
		contactPanel.add(contactValue, "growx");
		counselorGrid.add(contactPanel, "cell 0 1, grow");

		// Manually add Email
		JPanel emailPanel = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
		emailPanel.setOpaque(false);
		JLabel emailLabel = new JLabel("Email");
		emailLabel.setForeground(new Color(44, 62, 80));
		emailLabel.setFont(new Font("Arial", Font.BOLD, 12));
		emailPanel.add(emailLabel, "growx");
		JLabel emailValue = new JLabel("alice.smith@school.edu");
		emailValue.setBackground(new Color(248, 249, 250));
		emailValue.setOpaque(true);
		emailValue
				.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)),
						BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		emailValue.setFont(new Font("Arial", Font.PLAIN, 12));
		emailPanel.add(emailValue, "growx");
		counselorGrid.add(emailPanel, "cell 1 1, grow");

		add(counselorGrid, "cell 0 5, growx");

		// **Additional Information Section**
		JPanel additionalHeader = createSectionHeader("Additional Information", null);
		add(additionalHeader, "cell 0 6, growx");

		JPanel notesSectionPanel = new JPanel(new MigLayout("wrap, insets 15", "[grow]", "[]10[]"));
		notesSectionPanel.setBackground(new Color(248, 249, 250));
		notesSectionPanel.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));

		JLabel notesLabel = new JLabel("Appointment Notes");
		notesLabel.setForeground(new Color(44, 62, 80));
		notesLabel.setFont(new Font("Arial", Font.BOLD, 12));

		JTextArea notesArea = new JTextArea(
				"Career path discussion, future academic planning, and potential internship opportunities. "
						+ "Student seeks guidance on career trajectory and subject selection for upcoming semester.");
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.setEditable(false);
		notesArea.setOpaque(false);
		notesArea.setBorder(null);
		notesArea.setFont(new Font("Arial", Font.PLAIN, 12));

		notesSectionPanel.add(notesLabel, "growx");
		notesSectionPanel.add(notesArea, "growx"); // Removed 'h 100!'

		add(notesSectionPanel, "cell 0 7, growx");
	}

	private JPanel createSectionHeader(String title, String badge) {
		JPanel headerPanel = new JPanel(new MigLayout("insets 12 15, fillx"));
		headerPanel.setBackground(new Color(52, 152, 219));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

		headerPanel.add(titleLabel, "grow, pushx");

		if (badge != null) {
			JLabel badgeLabel = new JLabel(badge);
			badgeLabel.setForeground(Color.WHITE);
			badgeLabel.setBackground(new Color(41, 128, 185));
			badgeLabel.setOpaque(true);
			badgeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			headerPanel.add(badgeLabel);
		}

		return headerPanel;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			// Set up FlatLaf Look and Feel
			try {
				UIManager.setLookAndFeel(new FlatLightLaf());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			JFrame frame = new JFrame("Comprehensive Appointment Details");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppointmentDetailsUI panel = new AppointmentDetailsUI();
			frame.setContentPane(new JScrollPane(panel));
			frame.setLocationRelativeTo(null);
			frame.setSize(500, 500);
			frame.setVisible(true);
		});
	}
}