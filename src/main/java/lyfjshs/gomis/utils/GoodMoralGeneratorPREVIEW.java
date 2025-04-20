package lyfjshs.gomis.utils;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import lyfjshs.gomis.utils.jasper.ReportGenerator;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class GoodMoralGeneratorPREVIEW extends JPanel {
    private static final String DEFAULT_JASPER_TEMPLATE = "src/main/resources/jasperTemplates/templates/GoodMoral.jrxml";
    private static final Logger LOGGER = Logger.getLogger(GoodMoralGeneratorPREVIEW.class.getName());

    private static final String DEFAULT_DEPED_SEAL = "src/main/resources/images/DepEd_Seal.png";
    private static final String DEFAULT_DEPED_MATATAG = "src/main/resources/images/DepEd-MATATAG_BagongPilipinas.png";
    private static final String DEFAULT_LYFJSHS_LOGO = "src/main/resources/images/LYFJSHS.png";

    private Image depedSealImage;
    private Image depedMatatagImage;
    private Image lyfjshsLogoImage;
    private JPanel otherSignerPanel;

    private JButton editDepedSealBtn;
    private JButton editMatatagBtn;
    private JButton editLyfjshsBtn;

    private JButton previewButton;
    private JasperPreviewPanel previewPanel;
    private JLabel statusLabel;
    private JLabel fontNoteLabel;
    private JProgressBar progressBar;

    private JTextField nameField;
    private JTextField schoolYearField;
    private JTextField strandField;
    private JTextField trackField;
    private JTextField purposeField;
    private JTextField outputNameField;
    private JComboBox<String> signerComboBox;
    private JTextField fullNameField;
    private JTextField workPositionField;
    private DatePicker datePicker;
    private JFormattedTextField dateGivenField;
    private JLabel formattedDateLabel;

    private static final int PREVIEW_WIDTH = 100;
    private static final int PREVIEW_HEIGHT = 100;

    private JLabel depedSealPreview;
    private JLabel matatagPreview;
    private JLabel lyfjshsPreview;

    private JScrollPane scrollPane;

    public GoodMoralGeneratorPREVIEW(String jasperTemplate) {
        // Configure FlatLaf properties
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.track", UIManager.getColor("Panel.background"));
        UIManager.put("TitlePane.unifiedBackground", true);

        setLayout(new BorderLayout());

        // Create main container with sidebar and preview
        JPanel mainContainer = new JPanel();
        mainContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Create sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(350, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));

        // Create header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 1, 0),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel titleLabel = new JLabel("Good Moral Certificate Generator");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        // Create scrollable content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new MigLayout("insets 15, gap 10", "[grow]", ""));
        
        // ===== STUDENT INFO SECTION =====
        JPanel studentInfoSection = new JPanel(new MigLayout("insets 10, gap 5", "[grow]", "[]"));
        studentInfoSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel studentInfoTitle = new JLabel("Student Information");
        studentInfoSection.add(studentInfoTitle, "wrap");

        // Create and style form fields
        nameField = new JTextField(20);
        nameField.setToolTipText("Enter the full name of the student");
        styleTextField(nameField);

        schoolYearField = new JTextField(20);
        schoolYearField.setToolTipText("Enter the school year (e.g., 2023-2024)");
        styleTextField(schoolYearField);

        strandField = new JTextField(20);
        strandField.setToolTipText("Enter the student's strand");
        styleTextField(strandField);

        trackField = new JTextField(20);
        trackField.setToolTipText("Enter the student's track");
        styleTextField(trackField);

        // Add student information fields
        JLabel nameLabel = new JLabel("Name:");
        studentInfoSection.add(nameLabel, "split 2, width 80!");
        studentInfoSection.add(nameField, "growx, wrap");

        JLabel schoolYearLabel = new JLabel("School Year:");
        studentInfoSection.add(schoolYearLabel, "split 2, width 80!");
        studentInfoSection.add(schoolYearField, "growx, wrap");

        JLabel strandLabel = new JLabel("Strand:");
        studentInfoSection.add(strandLabel, "split 2, width 80!");
        studentInfoSection.add(strandField, "growx, wrap");

        JLabel trackLabel = new JLabel("Track:");
        studentInfoSection.add(trackLabel, "split 2, width 80!");
        studentInfoSection.add(trackField, "growx, wrap");

        contentPanel.add(studentInfoSection, "growx, wrap 10");

        // ===== CERTIFICATE DETAILS SECTION =====
        JPanel certificateDetailsSection = new JPanel(new MigLayout("insets 10, gap 5", "[grow]", "[]"));
        certificateDetailsSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel certificateDetailsTitle = new JLabel("Certificate Details");
        certificateDetailsSection.add(certificateDetailsTitle, "wrap");

        // Create and style form fields
        purposeField = new JTextField(20);
        purposeField.setToolTipText("Enter the purpose of the certificate");
        styleTextField(purposeField);

        outputNameField = new JTextField(20);
        outputNameField.setToolTipText("GeneratedReport");
        styleTextField(outputNameField);
        outputNameField.setText("GeneratedReport");

        // Create date picker
        datePicker = new DatePicker();
        datePicker.setSelectedDate(LocalDate.now());
        dateGivenField = new JFormattedTextField();
        datePicker.setEditor(dateGivenField);
        styleTextField(dateGivenField);

        // Initialize formatted date label
        formattedDateLabel = new JLabel();
        updateFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate());
        datePicker.addDateSelectionListener(
                e -> updateFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate()));

        // Create signer combo box
        String[] signersAndPosition = { "-Select Who to Sign-", "SALLY P. GENUINO, Principal II",
                "RACQUEL D. COMANDANTE, Guidance Designate", "Other" };
        signerComboBox = new JComboBox<>(signersAndPosition);
        signerComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Add certificate details fields
        JLabel purposeLabel = new JLabel("Purpose:");
        certificateDetailsSection.add(purposeLabel, "split 2, width 80!");
        certificateDetailsSection.add(purposeField, "growx, wrap");

        JLabel dateGivenLabel = new JLabel("Date Given:");
        certificateDetailsSection.add(dateGivenLabel, "split 2, width 80!");
        certificateDetailsSection.add(dateGivenField, "growx, wrap");

        certificateDetailsSection.add(formattedDateLabel, "span 2, center, wrap");

        JLabel signerLabel = new JLabel("Signer:");
        certificateDetailsSection.add(signerLabel, "split 2, width 80!");
        certificateDetailsSection.add(signerComboBox, "growx, wrap");

        // Create other signer panel with animation
        otherSignerPanel = new JPanel(new MigLayout("fillx", "[][grow]", "[]"));
        otherSignerPanel.setVisible(false);

        fullNameField = new JTextField(20);
        fullNameField.setToolTipText("Enter signer's full name");
        styleTextField(fullNameField);

        workPositionField = new JTextField(20);
        workPositionField.setToolTipText("Enter signer's position");
        styleTextField(workPositionField);

        // Add fields to otherSignerPanel
        JLabel fullNameLabel = new JLabel("Full Name:");
        otherSignerPanel.add(fullNameLabel, "right");
        otherSignerPanel.add(fullNameField, "growx, wrap");

        JLabel workPositionLabel = new JLabel("Position:");
        otherSignerPanel.add(workPositionLabel, "right");
        otherSignerPanel.add(workPositionField, "growx");

        // Add otherSignerPanel to section
        certificateDetailsSection.add(otherSignerPanel, "span 2, growx, wrap");

        JLabel outputNameLabel = new JLabel("Output Name:");
        certificateDetailsSection.add(outputNameLabel, "split 2, width 80!");
        certificateDetailsSection.add(outputNameField, "growx, wrap");

        // Add signer combo box listener with fade-in animation
        signerComboBox.addActionListener(e -> {
            boolean isOther = signerComboBox.getSelectedItem().equals("Other");
            otherSignerPanel.setVisible(isOther);
            if (isOther) {
                // Fade-in animation
                otherSignerPanel.setOpaque(false);
                Timer timer = new Timer(50, new ActionListener() {
                    float opacity = 0f;

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        opacity += 0.1f;
                        if (opacity >= 1f) {
                            opacity = 1f;
                            ((Timer) evt.getSource()).stop();
                            otherSignerPanel.setOpaque(true);
                        }
                        otherSignerPanel.repaint();
                    }
                });
                timer.start();
            }
        });

        contentPanel.add(certificateDetailsSection, "growx, wrap 10");

        // ===== IMAGES SECTION =====
        JPanel imagesSection = new JPanel(new MigLayout("insets 10, gap 5", "[grow]", "[]"));
        imagesSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel imagesTitle = new JLabel("School Logos");
        imagesSection.add(imagesTitle, "wrap");

        // Create image previews with placeholder icons
        depedSealPreview = new JLabel();
        depedSealPreview.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        depedSealPreview.setHorizontalAlignment(JLabel.CENTER);
        depedSealPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        depedSealPreview.setOpaque(true);
        depedSealPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        depedSealPreview.setToolTipText("Click to select DepEd Seal");
        depedSealPreview.setText("No image");

        // Add hover effect
        depedSealPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                depedSealPreview.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor"), 2),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                depedSealPreview.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }
        });

        matatagPreview = new JLabel();
        matatagPreview.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        matatagPreview.setHorizontalAlignment(JLabel.CENTER);
        matatagPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        matatagPreview.setOpaque(true);
        matatagPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        matatagPreview.setToolTipText("Click to select DepEd MATATAG");
        matatagPreview.setText("No image");

        // Add hover effect
        matatagPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                matatagPreview.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor"), 2),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                matatagPreview.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }
        });

        lyfjshsPreview = new JLabel();
        lyfjshsPreview.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        lyfjshsPreview.setHorizontalAlignment(JLabel.CENTER);
        lyfjshsPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        lyfjshsPreview.setOpaque(true);
        lyfjshsPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lyfjshsPreview.setToolTipText("Click to select LYFJSHS Logo");
        lyfjshsPreview.setText("No image");

        // Add hover effect
        lyfjshsPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                lyfjshsPreview.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor"), 2),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                lyfjshsPreview.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }
        });

        // Create image grid
        JPanel imageGrid = new JPanel(new MigLayout("insets 0, gap 10", "[grow][grow][grow]", "[]"));

        // Create DepEd Seal panel
        JPanel depedSealPanel = new JPanel(new MigLayout("fillx", "[grow]", "[]5[]"));
        depedSealPanel.add(depedSealPreview, "span 2, center, wrap");

        JButton depedSealSelectBtn = new JButton("Select");
        depedSealSelectBtn.setFocusPainted(false);
        depedSealSelectBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        editDepedSealBtn = new JButton("Edit");
        editDepedSealBtn.setFocusPainted(false);
        editDepedSealBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        editDepedSealBtn.setEnabled(false);

        depedSealSelectBtn.addActionListener(e -> selectAndPreviewImage("DepEd Seal", file -> {
            try {
                BufferedImage img = ImageIO.read(file);
                if (validateImage(img, "DepEd Seal")) {
                    updateImageAndPreview("DepEd Seal", img, depedSealPreview, editDepedSealBtn);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error loading DepEd Seal", ex);
                updateStatus("Error loading DepEd Seal: " + ex.getMessage());
                showError("Error loading image: " + ex.getMessage(), "Image Loading Error");
            }
        }));

        editDepedSealBtn.addActionListener(e -> editImage("DepEd Seal", (BufferedImage) depedSealImage,
                img -> updateImageAndPreview("DepEd Seal", img, depedSealPreview, editDepedSealBtn)));

        depedSealPanel.add(depedSealSelectBtn, "growx");
        depedSealPanel.add(editDepedSealBtn, "growx");

        // Create DepEd MATATAG panel
        JPanel matatagPanel = new JPanel(new MigLayout("fillx", "[grow]", "[]5[]"));
        matatagPanel.add(matatagPreview, "span 2, center, wrap");

        JButton matatagSelectBtn = new JButton("Select");
        matatagSelectBtn.setFocusPainted(false);
        matatagSelectBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        editMatatagBtn = new JButton("Edit");
        editMatatagBtn.setFocusPainted(false);
        editMatatagBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        editMatatagBtn.setEnabled(false);

        matatagSelectBtn.addActionListener(e -> selectAndPreviewImage("DepEd MATATAG", file -> {
            try {
                BufferedImage img = ImageIO.read(file);
                if (validateImage(img, "DepEd MATATAG")) {
                    updateImageAndPreview("DepEd MATATAG", img, matatagPreview, editMatatagBtn);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error loading DepEd MATATAG", ex);
                updateStatus("Error loading DepEd MATATAG: " + ex.getMessage());
                showError("Error loading image: " + ex.getMessage(), "Image Loading Error");
            }
        }));

        editMatatagBtn.addActionListener(e -> editImage("DepEd MATATAG", (BufferedImage) depedMatatagImage,
                img -> updateImageAndPreview("DepEd MATATAG", img, matatagPreview, editMatatagBtn)));

        matatagPanel.add(matatagSelectBtn, "growx");
        matatagPanel.add(editMatatagBtn, "growx");

        // Create LYFJSHS Logo panel
        JPanel lyfjshsPanel = new JPanel(new MigLayout("fillx", "[grow]", "[]5[]"));
        lyfjshsPanel.add(lyfjshsPreview, "span 2, center, wrap");

        JButton lyfjshsSelectBtn = new JButton("Select");
        lyfjshsSelectBtn.setFocusPainted(false);
        lyfjshsSelectBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        editLyfjshsBtn = new JButton("Edit");
        editLyfjshsBtn.setFocusPainted(false);
        editLyfjshsBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        editLyfjshsBtn.setEnabled(false);

        lyfjshsSelectBtn.addActionListener(e -> selectAndPreviewImage("LYFJSHS Logo", file -> {
            try {
                BufferedImage img = ImageIO.read(file);
                if (validateImage(img, "LYFJSHS Logo")) {
                    updateImageAndPreview("LYFJSHS Logo", img, lyfjshsPreview, editLyfjshsBtn);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error loading LYFJSHS Logo", ex);
                updateStatus("Error loading LYFJSHS Logo: " + ex.getMessage());
                showError("Error loading image: " + ex.getMessage(), "Image Loading Error");
            }
        }));

        editLyfjshsBtn.addActionListener(e -> editImage("LYFJSHS Logo", (BufferedImage) lyfjshsLogoImage,
                img -> updateImageAndPreview("LYFJSHS Logo", img, lyfjshsPreview, editLyfjshsBtn)));

        lyfjshsPanel.add(lyfjshsSelectBtn, "growx");
        lyfjshsPanel.add(editLyfjshsBtn, "growx");

        // Add image panels to grid
        imageGrid.add(depedSealPanel, "growx");
        imageGrid.add(matatagPanel, "growx");
        imageGrid.add(lyfjshsPanel, "growx");

        imagesSection.add(imageGrid, "growx, wrap");

        JButton loadDefaultsBtn = new JButton("Load Default Images");
        loadDefaultsBtn.setFocusPainted(false);
        loadDefaultsBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        loadDefaultsBtn.setToolTipText("Load default images for DepEd Seal, MATATAG, and LYFJSHS Logo");
        loadDefaultsBtn.addActionListener(e -> loadDefaultImages());
        imagesSection.add(loadDefaultsBtn, "growx, wrap");

        contentPanel.add(imagesSection, "growx, wrap 10");

        // ===== ACTIONS SECTION =====
        JPanel actionsSection = new JPanel(new MigLayout("insets 10, gap 5", "[grow]", "[]"));
        actionsSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel actionsTitle = new JLabel("Actions");
        actionsSection.add(actionsTitle, "wrap");

        // Create action buttons with icons
        previewButton = new JButton("Preview Report");
        previewButton.setFocusPainted(false);
        previewButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        previewButton.setToolTipText("Generate a preview of the certificate");
        previewButton.addActionListener(e -> previewReport(DEFAULT_JASPER_TEMPLATE));

        JButton printButton = new JButton("Print Report");
        printButton.setFocusPainted(false);
        printButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        printButton.setToolTipText("Print the certificate");
        printButton.addActionListener(
                e -> processReport(jasperTemplate, "print", collectParameters(), outputNameField.getText().trim()));

        JButton exportDocxButton = new JButton("Export to DOCX");
        exportDocxButton.setFocusPainted(false);
        exportDocxButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        exportDocxButton.setToolTipText("Export the certificate as a DOCX file");
        exportDocxButton.addActionListener(e -> exportReport(jasperTemplate, "docx", collectParameters(),
                outputNameField.getText().trim(), ".docx"));

        JButton exportPdfButton = new JButton("Export to PDF");
        exportPdfButton.setFocusPainted(false);
        exportPdfButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        exportPdfButton.setToolTipText("Export the certificate as a PDF file");
        exportPdfButton.addActionListener(e -> exportReport(jasperTemplate, "pdf", collectParameters(),
                outputNameField.getText().trim(), ".pdf"));

        JButton clearButton = new JButton("Clear Form");
        clearButton.setFocusPainted(false);
        clearButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        clearButton.setToolTipText("Clear all form fields and images");
        clearButton.addActionListener(e -> clearForm());

        // Add buttons to section
        actionsSection.add(previewButton, "growx, wrap");
        actionsSection.add(printButton, "growx, wrap");
        actionsSection.add(exportDocxButton, "growx, wrap");
        actionsSection.add(exportPdfButton, "growx, wrap");
        actionsSection.add(clearButton, "growx, wrap");

        contentPanel.add(actionsSection, "growx, wrap 10");

        // Add progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        contentPanel.add(progressBar, "growx, wrap");

        // Add font note and status
        fontNoteLabel = new JLabel(
                "Note: The preview may show different fonts than the final output due to system font availability.");
        contentPanel.add(fontNoteLabel, "growx, wrap");

        statusLabel = new JLabel("Ready");
        contentPanel.add(statusLabel, "growx, wrap");

        // Add scroll pane
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContainer.setLayout(new MigLayout("", "[grow 41][grow]", "[1086px]"));
        sidebar.add(scrollPane, BorderLayout.CENTER);

        mainContainer.add(sidebar, "cell 0 0,grow");

        // Create preview panel
        previewPanel = new JasperPreviewPanel(jasperTemplate);
        mainContainer.add(previewPanel, "cell 1 0,grow");

        add(mainContainer, BorderLayout.CENTER);
    }

    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private boolean validateImage(Image image, String imageType) {
        if (image == null || image.getWidth(null) <= 0 || image.getHeight(null) <= 0) {
            showError("Failed to load " + imageType + " image. The file may be corrupted or not a valid image.",
                    "Image Validation Error");
            return false;
        }
        return true;
    }

    private void clearForm() {
        nameField.setText("");
        schoolYearField.setText("");
        strandField.setText("");
        trackField.setText("");
        purposeField.setText("");
        outputNameField.setText("GeneratedReport");
        datePicker.setSelectedDate(LocalDate.now());
        signerComboBox.setSelectedIndex(0);
        fullNameField.setText("");
        workPositionField.setText("");

        depedSealImage = null;
        depedMatatagImage = null;
        lyfjshsLogoImage = null;

        updateImagePreview(depedSealPreview, null);
        updateImagePreview(matatagPreview, null);
        updateImagePreview(lyfjshsPreview, null);

        editDepedSealBtn.setEnabled(false);
        editMatatagBtn.setEnabled(false);
        editLyfjshsBtn.setEnabled(false);

        previewPanel.clearPreview();

        updateStatus("Form cleared");
    }

    private void previewReport(String jasperTemplate) {
        updateStatus("Generating preview...");
        previewButton.setEnabled(false);
        showProgressBar(true);
        progressBar.setIndeterminate(true);

        Map<String, Object> parameters = collectParameters();
        if (parameters != null) {
            previewPanel.updatePreview(parameters);
            updateStatus("Preview generated successfully");
        } else {
            updateStatus("Failed to generate preview");
        }

        previewButton.setEnabled(true);
        showProgressBar(false);
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showProgressBar(boolean show) {
        if (progressBar != null) {
            progressBar.setVisible(show);
        }
    }

    private void updateFormattedDateLabel(JLabel label, LocalDate date) {
        int day = date.getDayOfMonth();
        String suffix = getOrdinalSuffix(day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
        label.setText(day + suffix + " day of " + date.format(formatter));
    }

    private String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13)
            return "th";
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    private Map<String, Object> collectParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Name", nameField.getText().trim());
        parameters.put("SchoolYear", schoolYearField.getText().trim());
        parameters.put("Strand", strandField.getText().trim());
        parameters.put("TrackAndSpecialization", trackField.getText().trim());
        parameters.put("purpose", purposeField.getText().trim());
        parameters.put("DateGiven", formattedDateLabel.getText());

        String selectedSigner = (String) signerComboBox.getSelectedItem();
        if (selectedSigner.equals("Other")) {
            parameters.put("nameToSign", fullNameField.getText().trim());
            parameters.put("workPosition", workPositionField.getText().trim());
        } else if (!selectedSigner.equals("-Select Who to Sign-")) {
            String[] signerParts = selectedSigner.split(", ");
            parameters.put("nameToSign", signerParts.length > 0 ? signerParts[0] : "Unknown");
            parameters.put("workPosition", signerParts.length > 1 ? signerParts[1] : "Unknown");
        }

        parameters.put("deped_seal", depedSealImage);
        parameters.put("deped_matatag", depedMatatagImage);
        parameters.put("LYFJSHS_logo", lyfjshsLogoImage);
        return parameters;
    }

    private void selectAndPreviewImage(String imageType, java.util.function.Consumer<File> imageSetter) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg")
                        || f.getName().toLowerCase().endsWith(".jpeg")
                        || f.getName().toLowerCase().endsWith(".png")
                        || f.getName().toLowerCase().endsWith(".gif");
            }

            @Override
            public String getDescription() {
                return "Image files (*.jpg, *.jpeg, *.png, *.gif)";
            }
        });

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imageSetter.accept(chooser.getSelectedFile());
        }
    }

    private void processReport(String jasperTemplate, String action, Map<String, Object> parameters,
            String outputName) {
        if (parameters == null)
            return;
        final String finalOutputName = outputName.isEmpty() ? "GeneratedReport" : outputName;

        updateStatus("Processing report...");
        showProgressBar(true);
        progressBar.setIndeterminate(true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ReportGenerator reportGenerator = new ReportGenerator(jasperTemplate);
                reportGenerator.processReport(parameters, finalOutputName, action);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    updateStatus("Report processed successfully");
                } catch (Exception e) {
                    String errorMessage = e.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "Unknown error occurred while processing report";
                    } else if (errorMessage.contains("No printer available")) {
                        errorMessage = "No printer found. Please make sure a printer is installed and configured.";
                    }
                    LOGGER.log(Level.SEVERE, "Error processing report", e);
                    showError(errorMessage, "Processing Error");
                    updateStatus("Error: " + errorMessage);
                } finally {
                    showProgressBar(false);
                }
            }
        };

        worker.execute();
    }

    private void exportReport(String jasperTemplate, String action, Map<String, Object> parameters, String outputName,
            String extension) {
        if (parameters == null)
            return;
        final String finalOutputName = outputName.isEmpty() ? "GeneratedReport" : outputName;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(finalOutputName + extension));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            final String baseName = fileChooser.getSelectedFile().getAbsolutePath().replace(extension, "");

            updateStatus("Exporting report...");
            showProgressBar(true);
            progressBar.setIndeterminate(true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    ReportGenerator reportGenerator = new ReportGenerator(jasperTemplate);
                    reportGenerator.processReport(parameters, baseName, action);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                        updateStatus("Report exported successfully");
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error exporting report", e);
                        showError("Error exporting report: " + e.getMessage(), "Export Error");
                    } finally {
                        showProgressBar(false);
                    }
                }
            };

            worker.execute();
        }
    }

    private void loadDefaultImages() {
        try {
            loadImage(DEFAULT_DEPED_SEAL, "DepEd Seal", depedSealPreview, editDepedSealBtn);
            loadImage(DEFAULT_DEPED_MATATAG, "DepEd MATATAG", matatagPreview, editMatatagBtn);
            loadImage(DEFAULT_LYFJSHS_LOGO, "LYFJSHS Logo", lyfjshsPreview, editLyfjshsBtn);
            updateStatus("Default images loaded successfully");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading default images", e);
            showError("Error loading default images: " + e.getMessage(), "Image Loading Error");
        }
    }

    private void loadImage(String filePath, String imageType, JLabel preview, JButton editButton) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            BufferedImage img = ImageIO.read(file);
            if (validateImage(img, imageType)) {
                JButton targetEditButton = null;
                switch (imageType) {
                    case "DepEd Seal":
                        targetEditButton = editDepedSealBtn;
                        break;
                    case "DepEd MATATAG":
                        targetEditButton = editMatatagBtn;
                        break;
                    case "LYFJSHS Logo":
                        targetEditButton = editLyfjshsBtn;
                        break;
                }
                if (targetEditButton != null) {
                    updateImageAndPreview(imageType, img, preview, targetEditButton);
                }
            }
        } else {
            LOGGER.warning("Default image not found: " + filePath);
            updateStatus("Warning: Default image not found: " + imageType);
        }
    }

    private void editImage(String imageType, BufferedImage image,
            java.util.function.Consumer<BufferedImage> onEditComplete) {
        if (image == null) {
            showError("No image loaded to edit.", "Edit Error");
            return;
        }

        ImageCropper.showImageCropper(this, image, croppedImage -> {
            if (croppedImage != null) {
                onEditComplete.accept(croppedImage);
            }
        });
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void updateImageAndPreview(String imageType, BufferedImage img, JLabel preview, JButton editButton) {
        switch (imageType) {
            case "DepEd Seal":
                depedSealImage = img;
                editButton.setEnabled(true);
                break;
            case "DepEd MATATAG":
                depedMatatagImage = img;
                editButton.setEnabled(true);
                break;
            case "LYFJSHS Logo":
                lyfjshsLogoImage = img;
                editButton.setEnabled(true);
                break;
        }
        updateImagePreview(preview, img);
        updateStatus(imageType + " loaded successfully");
    }

    private void updateImagePreview(JLabel preview, Image image) {
        if (image != null) {
            Image scaledImage = image.getScaledInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, Image.SCALE_SMOOTH);
            preview.setIcon(new ImageIcon(scaledImage));
            preview.setText(null);
        } else {
            preview.setIcon(null);
            preview.setText("No image");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error setting look and feel", ex);
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Good Moral Certificate Generator");
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            GoodMoralGeneratorPREVIEW panel = new GoodMoralGeneratorPREVIEW(DEFAULT_JASPER_TEMPLATE);
            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}