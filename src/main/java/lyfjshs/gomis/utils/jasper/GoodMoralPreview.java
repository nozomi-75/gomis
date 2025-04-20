package lyfjshs.gomis.utils.jasper;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.DropPanel;
import lyfjshs.gomis.utils.ImageCropper;
import lyfjshs.gomis.utils.JasperPreviewPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.Toast;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;

public class GoodMoralPreview extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_DEPED_SEAL = "src/main/resources/images/DepEd_Seal.png";
	private static final String DEFAULT_DEPED_MATATAG = "src/main/resources/images/DepEd-MATATAG_BagongPilipinas.png";
	private static final String DEFAULT_LYFJSHS_LOGO = "src/main/resources/images/LYFJSHS.png";

	private final Image[] images = new Image[3];
	private final JLabel[] imageLabels = new JLabel[3];
	private final JTextArea purposeField;
	private final JLabel formattedDateLabel;
	private final JComboBox<String> signerComboBox;
	private final JTextField fullNameField;
	private final JTextField workPositionField;
	private final JasperPreviewPanel previewPanel;
	private final Component parent;

	public GoodMoralPreview(Component parent) {
		this.parent = parent;
		setLayout(new MigLayout("", "[350:350:350][grow]", "[grow]"));

		// Initialize components
		purposeField = new JTextArea(4, 20);
		formattedDateLabel = new JLabel();
		signerComboBox = new JComboBox<>();
		fullNameField = new JTextField(10);
		workPositionField = new JTextField(10);
		previewPanel = new JasperPreviewPanel("src/main/resources/jasperTemplates/templates/GoodMoral_Final.jasper");

		// Setup UI
		setupUI();
		
		// Load default images
		loadDefaultImages();
	}

	private void setupUI() {
		DatePicker datePicker = new DatePicker();
		datePicker.setSelectedDate(LocalDate.now());

		// Create content panel
		JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 10", "[right][grow,fill]", "[100px][][][][][][][][][][]"));
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		
		// Purpose field setup
		purposeField.setLineWrap(true);
		purposeField.setWrapStyleWord(true);
		JScrollPane purposeScrollPane = new JScrollPane(purposeField);
		contentPanel.add(new JLabel("Purpose:"), "cell 0 0");
		contentPanel.add(purposeScrollPane, "cell 1 0,grow");

		// Date picker setup
		JFormattedTextField dateGivenField = new JFormattedTextField();
		datePicker.setEditor(dateGivenField);
		contentPanel.add(new JLabel("Date Given:"), "cell 0 1");
		contentPanel.add(dateGivenField, "cell 1 1");

		// Date label setup
		updateFormattedDateLabel(datePicker.getSelectedDate());
		datePicker.addDateSelectionListener(e -> updateFormattedDateLabel(datePicker.getSelectedDate()));
		contentPanel.add(formattedDateLabel, "cell 0 2 2 1,alignx center");

		// Signer setup
		setupSignerComponents(contentPanel);

		// Font notes
		addFontNotes(contentPanel);

		// Images section
		setupImagesSection(contentPanel);

		// Add to main panel
		add(scrollPane, "cell 0 0,grow");
		previewPanel.setPreferredSize(new Dimension(600, 800));
		add(previewPanel, "cell 1 0,grow");
	}

	private void setupSignerComponents(JPanel contentPanel) {
		String currentSigner = "SALLY P. GENUINO, Principal II";
		if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
			GuidanceCounselor counselor = Main.formManager.getCounselorObject();
			currentSigner = counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition();
		}

		String[] signersAndPosition = new String[] { 
			"-Select Who to Sign-", 
			currentSigner,
			"SALLY P. GENUINO, Principal II", 
			"Other" 
		};
		signerComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(signersAndPosition));
		
		contentPanel.add(new JLabel("Signer:"), "cell 0 3");
		contentPanel.add(signerComboBox, "cell 1 3");

		// Other signer panel
		DropPanel dropDownPanel = new DropPanel();
		JPanel otherSignerPanel = new JPanel(new MigLayout("", "[][grow]", "[][]"));
		otherSignerPanel.add(new JLabel("Full Name:"), "cell 0 0,alignx trailing");
		otherSignerPanel.add(fullNameField, "cell 1 0,growx");
		otherSignerPanel.add(new JLabel("Position:"), "cell 0 1,alignx trailing");
		otherSignerPanel.add(workPositionField, "cell 1 1,growx");
		dropDownPanel.setContent(otherSignerPanel);
		contentPanel.add(dropDownPanel, "cell 0 4 2 1,grow");

		signerComboBox.addActionListener(e -> 
			dropDownPanel.setDropdownVisible("Other".equals(signerComboBox.getSelectedItem())));
	}

	private void setupImagesSection(JPanel contentPanel) {
		DropPanel dropDownPanelLogosSection = new DropPanel();
		JPanel imagesSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[grow][]"));
		imagesSection.setBorder(BorderFactory.createTitledBorder("Logos"));

		JPanel imageGrid = new JPanel(new MigLayout("insets 0, gap 10", "[grow][grow][grow]", "[grow]"));
		
		// Setup image panels
		String[] titles = {"DepEd Seal", "DepEd MATATAG", "LYFJSHS Logo"};
		for (int i = 0; i < 3; i++) {
			JPanel imagePanel = createImagePanel(titles[i], i);
			imageGrid.add(imagePanel, "cell " + i + " 0,grow");
		}

		imagesSection.add(imageGrid, "cell 0 0,grow");

		JButton loadDefaultsBtn = new JButton("Load Default Images");
		loadDefaultsBtn.addActionListener(e -> loadDefaultImages());
		imagesSection.add(loadDefaultsBtn, "cell 0 1,growx");
		
				// Preview button
				JButton previewBtn = new JButton("Preview");
				previewBtn.addActionListener(e -> updatePreview());
				contentPanel.add(previewBtn, "cell 0 8 2 1,growx");

		JButton toggleLogosBtn = new JButton("Toggle Logos Section");
		contentPanel.add(toggleLogosBtn, "cell 0 9 2 1,growx");
		
		dropDownPanelLogosSection.setContent(imagesSection);
		contentPanel.add(dropDownPanelLogosSection, "cell 0 10 2 1,grow");
		
		toggleLogosBtn.addActionListener(e -> 
			dropDownPanelLogosSection.setDropdownVisible(!dropDownPanelLogosSection.isDropdownVisible()));
	}

	private JPanel createImagePanel(String title, int index) {
		JPanel panel = new JPanel(new MigLayout("", "[grow,center]", "[grow][][]"));
		
		imageLabels[index] = new JLabel("No image");
		imageLabels[index].setPreferredSize(new Dimension(100, 100));
		imageLabels[index].setHorizontalAlignment(JLabel.CENTER);
		imageLabels[index].setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
		panel.add(imageLabels[index], "cell 0 0,grow");

		JButton selectBtn = new JButton("Select");
		selectBtn.addActionListener(e -> selectImage(title, index));
		panel.add(selectBtn, "cell 0 1");

		JButton editBtn = new JButton("Edit");
		editBtn.addActionListener(e -> editImage(index));
		panel.add(editBtn, "cell 0 2");

		return panel;
	}

	private void selectImage(String title, int index) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory() || 
					   f.getName().toLowerCase().endsWith(".jpg") ||
					   f.getName().toLowerCase().endsWith(".jpeg") ||
					   f.getName().toLowerCase().endsWith(".png") ||
					   f.getName().toLowerCase().endsWith(".gif");
			}
			public String getDescription() {
				return "Image files (*.jpg, *.jpeg, *.png, *.gif)";
			}
		});

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				BufferedImage img = ImageIO.read(chooser.getSelectedFile());
				if (img != null) {
					images[index] = img;
					Image scaled = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
					imageLabels[index].setIcon(new ImageIcon(scaled));
					imageLabels[index].setText(null);
				}
			} catch (IOException ex) {
				showToast("Error loading image: " + ex.getMessage(), Toast.Type.ERROR);
			}
		}
	}

	private void editImage(int index) {
		if (images[index] == null) {
			showToast("No image loaded to edit.", Toast.Type.ERROR);
			return;
		}

		// Convert Image to BufferedImage if needed
		BufferedImage bufferedImage;
		if (images[index] instanceof BufferedImage) {
			bufferedImage = (BufferedImage) images[index];
		} else {
			// Convert Image to BufferedImage
			bufferedImage = new BufferedImage(
				images[index].getWidth(null),
				images[index].getHeight(null),
				BufferedImage.TYPE_INT_ARGB
			);
			Graphics2D g2d = bufferedImage.createGraphics();
			g2d.drawImage(images[index], 0, 0, null);
			g2d.dispose();
		}

		ImageCropper.showImageCropper(this, bufferedImage, croppedImage -> {
			if (croppedImage != null) {
				images[index] = croppedImage;
				Image scaled = croppedImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
				imageLabels[index].setIcon(new ImageIcon(scaled));
				imageLabels[index].setText(null);
				showToast("Image edited successfully", Toast.Type.SUCCESS);
			}
		});
	}

	private void loadDefaultImages() {
		try {
			images[0] = ImageIO.read(new File(DEFAULT_DEPED_SEAL));
			images[1] = ImageIO.read(new File(DEFAULT_DEPED_MATATAG));
			images[2] = ImageIO.read(new File(DEFAULT_LYFJSHS_LOGO));
			
			for (int i = 0; i < 3; i++) {
				Image scaled = images[i].getScaledInstance(100, 100, Image.SCALE_SMOOTH);
				imageLabels[i].setIcon(new ImageIcon(scaled));
				imageLabels[i].setText(null);
			}
			
			showToast("Default images loaded successfully", Toast.Type.SUCCESS);
		} catch (IOException e) {
			showToast("Error loading default images: " + e.getMessage(), Toast.Type.ERROR);
		}
	}

	private void updateFormattedDateLabel(LocalDate date) {
		int day = date.getDayOfMonth();
		String suffix = getOrdinalSuffix(day);
		DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
		String formattedDate = day + suffix + " day of " + date.format(monthYearFormatter);
		formattedDateLabel.setText(formattedDate);
	}

	private static String getOrdinalSuffix(int day) {
		if (day >= 11 && day <= 13) {
			return "th";
		}
		switch (day % 10) {
			case 1:  return "st";
			case 2:  return "nd";
			case 3:  return "rd";
			default: return "th";
		}
	}

	private void updatePreview() {
		String[] signerParts = getSelectedSigner().split(", ");
		String nameToSign = signerParts.length > 0 ? signerParts[0] : "Unknown";
		String workPosition = signerParts.length > 1 ? signerParts[1] : "Unknown";

		java.util.Map<String, Object> parameters = new java.util.HashMap<>();
		parameters.put("purpose", purposeField.getText().trim());
		parameters.put("DateGiven", formattedDateLabel.getText());
		parameters.put("nameToSign", nameToSign);
		parameters.put("workPosition", workPosition);
		parameters.put("deped_seal", images[0]);
		parameters.put("deped_matatag", images[1]);
		parameters.put("LYFJSHS_logo", images[2]);

		previewPanel.updatePreview(parameters);
	}

	private void showToast(String message, Toast.Type type) {
		ToastOption toastOption = Toast.createOption();
		toastOption.getLayoutOption()
				.setMargin(0, 0, 50, 0)
				.setDirection(ToastDirection.TOP_TO_BOTTOM);
		Toast.show(parent, type, message, ToastLocation.BOTTOM_CENTER, toastOption);
	}

	private void addFontNotes(JPanel contentPanel) {
		JLabel fontNoteLabel = new JLabel("Note: The preview may show different fonts than");
		contentPanel.add(fontNoteLabel, "cell 0 6 2 1,growx");
		JLabel fontNote2Label = new JLabel(" the final output due to system font availability.");
		contentPanel.add(fontNote2Label, "cell 0 7 2 1,growx");
	}

	// Getters for accessing form data
	public String getPurpose() {
		return purposeField.getText().trim();
	}

	public String getFormattedDate() {
		return formattedDateLabel.getText();
	}

	public String getSelectedSigner() {
		if ("Other".equals(signerComboBox.getSelectedItem())) {
			return fullNameField.getText().trim() + ", " + workPositionField.getText().trim();
		}
		return (String) signerComboBox.getSelectedItem();
	}

	public Image[] getImages() {
		return images;
	}
}
