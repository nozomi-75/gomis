package LEGACY_test_unused;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Text;

import jakarta.xml.bind.JAXBElement;
import net.miginfocom.swing.MigLayout;

//this class is the test class on how should docx4j works with word docx file
//this is also the way we input-output values/data from template to a generated docx file 
public class test_priting_feature {
    private JFrame frame;
    private JTextField yearField;
    private JTextField dateField;
    private File templateFile;
    private File outputFolder;
    private JCheckBox chckbxNewCheckBox;
    private JCheckBox chckbxNewCheckBox_1;
    private JCheckBox chckbxNewCheckBox_2;
    private JTextField nameField1;
    private JTextField nameField2;
    private JTextField nameField3;
    private JButton testPrinter;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new test_priting_feature().createGUI());
    }

    private void createGUI() {
        frame = new JFrame("Certificate Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 400);
        frame.getContentPane().setLayout(new MigLayout("", "[315.00,grow][grow]", "[grow]"));

        JPanel leftPanel = new JPanel(new MigLayout("", "[grow][grow]", "[][][][][][][][][][]"));
        JButton loadTemplateButton = new JButton("Load Template");
        loadTemplateButton.addActionListener(e -> loadTemplate());
        leftPanel.add(loadTemplateButton, "cell 0 0,growx");

        leftPanel.add(new JLabel("School Year:"), "cell 0 1,growx");
        yearField = new JTextField();
        leftPanel.add(yearField, "cell 1 1,growx");

        leftPanel.add(new JLabel("Date Given:"), "cell 0 2,growx");
        dateField = new JTextField();
        leftPanel.add(dateField, "cell 1 2,growx");

        JButton chooseFolder = new JButton("Choose Output Folder");
        chooseFolder.addActionListener(e -> selectOutputFolder());
        leftPanel.add(chooseFolder, "cell 0 3,growx");

        JButton generateDocx = new JButton("Generate DOCX");
        generateDocx.addActionListener(this::generateDocxFile);
        leftPanel.add(generateDocx, "cell 0 4,growx");

        JButton viewFolderLocation = new JButton("View Folder Location");
        viewFolderLocation.addActionListener(e -> {
            if (outputFolder != null && outputFolder.exists()) {
                try {
                    Desktop.getDesktop().open(outputFolder);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Cannot open folder: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an output folder first.");
            }
        });
        leftPanel.add(viewFolderLocation, "cell 0 5,growx");

        frame.getContentPane().add(leftPanel, "cell 0 0,grow");

        JButton printWord = new JButton("Print");
        printWord.addActionListener(e -> printUsingWord());
        testPrinter = new JButton("Test Print Services");
        JButton silentConsolePrint = new JButton("Print (Silent + Console)");
        silentConsolePrint.addActionListener(e -> printSilentlyWithConsole());
        leftPanel.add(silentConsolePrint, "cell 0 10, growx");

        testPrinter.addActionListener(e -> {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (InterruptedException | IOException e1) {
                System.out.print("\033\143");

            }

            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            System.out.println("Installed Printers:");
            for (PrintService ps : printServices) {
                System.out.println(" - " + ps.getName());
            }
        });
        leftPanel.add(testPrinter, "cell 0 7");
        leftPanel.add(printWord, "cell 0 8,growx");

        JPanel rightPanel = new JPanel(new MigLayout("", "[][grow][]", "[][][][]"));
        rightPanel.add(new JLabel("Name 1:"));
        nameField1 = new JTextField();
        rightPanel.add(nameField1, "growx");
        chckbxNewCheckBox = new JCheckBox("Include");
        rightPanel.add(chckbxNewCheckBox, "wrap");

        rightPanel.add(new JLabel("Name 2:"));
        nameField2 = new JTextField();
        rightPanel.add(nameField2, "growx");
        chckbxNewCheckBox_1 = new JCheckBox("Include");
        rightPanel.add(chckbxNewCheckBox_1, "wrap");

        rightPanel.add(new JLabel("Name 3:"));
        nameField3 = new JTextField();
        rightPanel.add(nameField3, "growx");
        chckbxNewCheckBox_2 = new JCheckBox("Include");
        rightPanel.add(chckbxNewCheckBox_2, "wrap");

        frame.getContentPane().add(rightPanel, "cell 1 0,grow");

        frame.setVisible(true);
    }

    private void loadTemplate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select DOCX Template");
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            templateFile = chooser.getSelectedFile();
            JOptionPane.showMessageDialog(frame, "Template Loaded: " + templateFile.getName());
        }
    }

    private void selectOutputFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Output Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputFolder = chooser.getSelectedFile();
            JOptionPane.showMessageDialog(frame, "Output folder: " + outputFolder.getAbsolutePath());
        }
    }

    private void generateDocxFile(ActionEvent e) {
        if (templateFile == null || outputFolder == null) {
            JOptionPane.showMessageDialog(frame, "Please load a template and select output folder first.");
            return;
        }
        try {
            String[] names = new String[] { chckbxNewCheckBox.isSelected() ? nameField1.getText() : null,
                    chckbxNewCheckBox_1.isSelected() ? nameField2.getText() : null,
                    chckbxNewCheckBox_2.isSelected() ? nameField3.getText() : null };
            boolean any = false;
            for (String name : names) {
                if (name == null || name.isBlank())
                    continue;
                any = true;
                WordprocessingMLPackage pkg = WordprocessingMLPackage.load(templateFile);
                HashMap<String, String> vars = new HashMap<>();
                vars.put("studentName", name);
                vars.put("schoolYear", yearField.getText());
                vars.put("formatDateGiven", dateField.getText());

                // Replace both types
                pkg.getMainDocumentPart().variableReplace(vars); // for ${variable}
                replaceContentControls(pkg, vars); // for content controls
                File out = new File(outputFolder, name.replace(" ", "_") + "_" + dateField.getText().replace("/", "-")
                        + "_Certificate Of Enrollment.docx");
                pkg.save(out);
            }
            if (any) {
                JOptionPane.showMessageDialog(frame, "DOCX(s) saved to selected folder.");
            } else {
                JOptionPane.showMessageDialog(frame, "No students selected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    // Global progress dialog for printing
    private class GlobalPrintProgressDialog extends JDialog {
        private final JProgressBar progressBar;
        private final JLabel label;
        public GlobalPrintProgressDialog(JFrame owner, int max) {
            super(owner, "Printing...", false); // non-modal
            progressBar = new JProgressBar(0, max);
            label = new JLabel("Starting printing...");
            JPanel panel = new JPanel(new java.awt.BorderLayout());
            panel.add(label, java.awt.BorderLayout.NORTH);
            panel.add(progressBar, java.awt.BorderLayout.CENTER);
            add(panel);
            setSize(400, 120);
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        }
        public void updateProgress(int value, String name) {
            progressBar.setValue(value);
            label.setText("Printing: " + name);
        }
        public void finish() {
            setVisible(false);
            dispose();
        }
    }

    private void printUsingWord() {
        if (outputFolder == null) return;
        String[] names = {
            chckbxNewCheckBox.isSelected() ? nameField1.getText() : null,
            chckbxNewCheckBox_1.isSelected() ? nameField2.getText() : null,
            chckbxNewCheckBox_2.isSelected() ? nameField3.getText() : null
        };
        java.util.List<String> validNames = new java.util.ArrayList<>();
        for (String name : names) {
            if (name != null && !name.isBlank()) validNames.add(name);
        }
        if (validNames.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No students selected.");
            return;
        }
        // Confirmation dialog before printing
        StringBuilder msg = new StringBuilder();
        msg.append("You are about to print the following student(s):\n\n");
        for (String name : validNames) {
            msg.append("- ").append(name).append("\n");
        }
        msg.append("\nThere is NO TURNING BACK.\nPrinting cannot be cancelled once started.\n\nProceed?");
        int confirm = JOptionPane.showConfirmDialog(frame, msg.toString(), "Confirm Print", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        GlobalPrintProgressDialog progressDialog = new GlobalPrintProgressDialog(frame, validNames.size());
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int idx = 0;
                for (String name : validNames) {
                    File docxFile = new File(outputFolder, name.replace(" ", "_") + "_" + dateField.getText().replace("/", "-") + "_Certificate Of Enrollment.docx");
                    if (!docxFile.exists()) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Missing DOCX for: " + name + ". Generate it first."));
                        continue;
                    }
                    try {
                        ProcessBuilder builder = new ProcessBuilder(
                            "src/main/resources/silent_docx_printer/silent_docx_printer.exe",
                            docxFile.getAbsolutePath()
                        );
                        builder.redirectErrorStream(true);
                        Process process = null;
                        try {
                            process = builder.start();
                            int exitCode = process.waitFor();
                            if (exitCode != 0) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Printing failed for " + name + ". Exit code: " + exitCode));
                            }
                        } catch (InterruptedException ex) {
                            if (process != null) process.destroy();
                            return null;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Error printing " + name + ": " + ex.getMessage()));
                            return null;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Error printing " + name + ": " + ex.getMessage()));
                    }
                    idx++;
                    final int progress = idx;
                    SwingUtilities.invokeLater(() -> progressDialog.updateProgress(progress, name));
                }
                return null;
            }
            @Override
            protected void done() {
                progressDialog.finish();
                JOptionPane.showMessageDialog(frame, "Printing complete.");
            }
        };
        worker.execute();
        progressDialog.setVisible(true);
    }
  
    private void printSilentlyWithConsole() {
        if (outputFolder == null) return;
        String[] names = {
            chckbxNewCheckBox.isSelected() ? nameField1.getText() : null,
            chckbxNewCheckBox_1.isSelected() ? nameField2.getText() : null,
            chckbxNewCheckBox_2.isSelected() ? nameField3.getText() : null
        };
        java.util.List<String> validNames = new java.util.ArrayList<>();
        for (String name : names) {
            if (name != null && !name.isBlank()) validNames.add(name);
        }
        if (validNames.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No students selected.");
            return;
        }
        // Confirmation dialog before printing
        StringBuilder msg = new StringBuilder();
        msg.append("You are about to print the following student(s):\n\n");
        for (String name : validNames) {
            msg.append("- ").append(name).append("\n");
        }
        msg.append("\nThere is NO TURNING BACK.\nPrinting cannot be cancelled once started.\n\nProceed?");
        int confirm = JOptionPane.showConfirmDialog(frame, msg.toString(), "Confirm Print", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        GlobalPrintProgressDialog progressDialog = new GlobalPrintProgressDialog(frame, validNames.size());
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int idx = 0;
                for (String name : validNames) {
                    File docxFile = new File(outputFolder, name.replace(" ", "_") + "_" + dateField.getText().replace("/", "-") + "_Certificate Of Enrollment.docx");
                    if (!docxFile.exists()) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Missing DOCX for: " + name + ". Generate it first."));
                        continue;
                    }
                    try {
                        ProcessBuilder pb = new ProcessBuilder(
                            "src/main/resources/silent_docx_printer/silent_docx_printer.exe",
                            docxFile.getAbsolutePath()
                        );
                        pb.redirectErrorStream(true);
                        Process process = null;
                        try {
                            process = pb.start();
                            try (var reader = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(process.getInputStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                }
                            }
                            int exitCode = process.waitFor();
                            if (exitCode != 0) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Printing failed for " + name + ". Exit code: " + exitCode));
                            }
                        } catch (InterruptedException ex) {
                            if (process != null) process.destroy();
                            return null;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Error printing " + name + ": " + ex.getMessage()));
                            return null;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Error printing " + name + ": " + ex.getMessage()));
                    }
                    idx++;
                    final int progress = idx;
                    SwingUtilities.invokeLater(() -> progressDialog.updateProgress(progress, name));
                }
                return null;
            }
            @Override
            protected void done() {
                progressDialog.finish();
                JOptionPane.showMessageDialog(frame, "Printing complete.");
            }
        };
        worker.execute();
        progressDialog.setVisible(true);
    }    

    // Helper method to replace content controls by tag
    private void replaceContentControls(WordprocessingMLPackage pkg, Map<String, String> values) {
        // Replace in main document
        replaceContentControlsInPart(pkg.getMainDocumentPart(), values);
        // Replace in headers/footers
        pkg.getDocumentModel().getSections().forEach(section -> {
            try {
                if (section.getHeaderFooterPolicy() != null) {
                    if (section.getHeaderFooterPolicy().getDefaultHeader() != null)
                        replaceContentControlsInPart(section.getHeaderFooterPolicy().getDefaultHeader(), values);
                    if (section.getHeaderFooterPolicy().getFirstHeader() != null)
                        replaceContentControlsInPart(section.getHeaderFooterPolicy().getFirstHeader(), values);
                    if (section.getHeaderFooterPolicy().getEvenHeader() != null)
                        replaceContentControlsInPart(section.getHeaderFooterPolicy().getEvenHeader(), values);
                    if (section.getHeaderFooterPolicy().getDefaultFooter() != null)
                        replaceContentControlsInPart(section.getHeaderFooterPolicy().getDefaultFooter(), values);
                    if (section.getHeaderFooterPolicy().getFirstFooter() != null)
                        replaceContentControlsInPart(section.getHeaderFooterPolicy().getFirstFooter(), values);
                    if (section.getHeaderFooterPolicy().getEvenFooter() != null)
                        replaceContentControlsInPart(section.getHeaderFooterPolicy().getEvenFooter(), values);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // Helper to replace content controls in a part
    private void replaceContentControlsInPart(ContentAccessor part, Map<String, String> values) {
        List<Object> sdtList = getAllElementFromObject(part, SdtElement.class);
        for (Object obj : sdtList) {
            SdtElement sdt = (SdtElement) obj;
            SdtPr sdtPr = sdt.getSdtPr();
            if (sdtPr != null && sdtPr.getTag() != null) {
                String tagVal = sdtPr.getTag().getVal();
                if (values.containsKey(tagVal)) {
                    setContentControlText(sdt, values.get(tagVal));
                }
            }
        }
    }

    // Helper to get all elements of a type
    private List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new java.util.ArrayList<>();
        if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();
        if (obj == null) return result;
        if (toSearch.isAssignableFrom(obj.getClass())) {
            result.add(obj);
        } else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementFromObject(child, toSearch));
            }
        }
        return result;
    }

    // Helper to set text in a content control
    private void setContentControlText(SdtElement sdt, String value) {
        List<Object> texts = getAllElementFromObject(sdt, Text.class);
        for (Object t : texts) {
            ((Text) t).setValue(value);
        }
    }

}
