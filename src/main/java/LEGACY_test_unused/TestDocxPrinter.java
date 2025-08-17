/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package LEGACY_test_unused;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Text;

public class TestDocxPrinter {
    private JFrame frame;
    private JTextField templateField;
    private JTextField outputFolderField;
    private JTable keyValueTable;
    private DefaultTableModel tableModel;
    private File templateFile;
    private File outputFolder;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestDocxPrinter::new);
    }

    public TestDocxPrinter() {
        frame = new JFrame("Test DOCX Printer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        templateField = new JTextField();
        templateField.setEditable(false);
        JButton templateBtn = new JButton("Select Template");
        templateBtn.addActionListener(this::selectTemplate);
        outputFolderField = new JTextField();
        outputFolderField.setEditable(false);
        JButton outputBtn = new JButton("Select Output Folder");
        outputBtn.addActionListener(this::selectOutputFolder);
        topPanel.add(new JLabel("Template:"));
        topPanel.add(templateField);
        topPanel.add(templateBtn);
        topPanel.add(new JLabel("Output Folder:"));
        topPanel.add(outputFolderField);
        topPanel.add(outputBtn);

        frame.add(topPanel, BorderLayout.NORTH);

        // Table for key-value pairs
        tableModel = new DefaultTableModel(new Object[]{"Key", "Value"}, 0);
        keyValueTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(keyValueTable);
        frame.add(tableScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton addRowBtn = new JButton("Add Field");
        addRowBtn.addActionListener(e -> tableModel.addRow(new Object[]{"", ""}));
        JButton removeRowBtn = new JButton("Remove Field");
        removeRowBtn.addActionListener(e -> {
            int selected = keyValueTable.getSelectedRow();
            if (selected != -1) tableModel.removeRow(selected);
        });
        JButton generateBtn = new JButton("Generate DOCX");
        generateBtn.addActionListener(this::generateDocx);
        bottomPanel.add(addRowBtn);
        bottomPanel.add(removeRowBtn);
        bottomPanel.add(generateBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void selectTemplate(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select DOCX Template");
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            templateFile = chooser.getSelectedFile();
            templateField.setText(templateFile.getAbsolutePath());
        }
    }

    private void selectOutputFolder(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Output Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputFolder = chooser.getSelectedFile();
            outputFolderField.setText(outputFolder.getAbsolutePath());
        }
    }

    private void generateDocx(ActionEvent e) {
        if (templateFile == null || outputFolder == null) {
            JOptionPane.showMessageDialog(frame, "Please select a template and output folder.");
            return;
        }
        try {
            Map<String, String> values = new HashMap<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String key = (String) tableModel.getValueAt(i, 0);
                String value = (String) tableModel.getValueAt(i, 1);
                if (key != null && !key.isBlank()) {
                    values.put(key, value != null ? value : "");
                }
            }
            WordprocessingMLPackage pkg = WordprocessingMLPackage.load(templateFile);
            pkg.getMainDocumentPart().variableReplace(values);
            replaceContentControls(pkg, values);
            File out = new File(outputFolder, "TestOutput_" + System.currentTimeMillis() + ".docx");
            pkg.save(out);
            JOptionPane.showMessageDialog(frame, "DOCX generated: " + out.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    private void replaceContentControls(WordprocessingMLPackage pkg, Map<String, String> values) {
        replaceContentControlsInPart(pkg.getMainDocumentPart(), values);
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

    private void replaceContentControlsInPart(ContentAccessor part, Map<String, String> values) {
        List<Object> sdtList = getAllElementFromObject(part, SdtElement.class);
        for (Object obj : sdtList) {
            SdtElement sdt = (SdtElement) obj;
            SdtPr sdtPr = sdt.getSdtPr();
            if (sdtPr != null && sdtPr.getTag() != null) {
                String tagVal = sdtPr.getTag().getVal();
                System.out.println("Found content control tag: " + tagVal);
                if (values.containsKey(tagVal)) {
                    System.out.println("Replacing tag '" + tagVal + "' with value: " + values.get(tagVal));
                    setContentControlText(sdt, values.get(tagVal));
                } else {
                    System.out.println("No value found for tag: " + tagVal);
                }
            }
        }
    }

    private List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new java.util.ArrayList<>();
        if (obj instanceof jakarta.xml.bind.JAXBElement) obj = ((jakarta.xml.bind.JAXBElement<?>) obj).getValue();
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

    private void setContentControlText(SdtElement sdt, String value) {
        List<Object> texts = getAllElementFromObject(sdt, Text.class);
        for (Object t : texts) {
            ((Text) t).setValue(value);
        }
    }
} 