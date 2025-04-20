package lyfjshs.gomis.utils;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lyfjshs.gomis.utils.jasper.ReportGenerator;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.swing.JRViewer;

/**
 * A reusable panel for previewing JasperReports with caching for better performance.
 */
public class JasperPreviewPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(JasperPreviewPanel.class.getName());

    private final Map<String, JasperReport> compiledReports = new ConcurrentHashMap<>();
    private JRViewer currentViewer;
    private ReportGenerator reportGenerator;
    private JLabel statusLabel;
    private JPanel previewContainer;
    
    public JasperPreviewPanel(String jasperTemplate) {
        this.reportGenerator = new ReportGenerator(jasperTemplate);
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
        
        // Create preview container
        previewContainer = new JPanel(new BorderLayout());
        previewContainer.setBorder(BorderFactory.createEmptyBorder());
        
        // Add scroll pane for the preview
        JScrollPane scrollPane = new JScrollPane(previewContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Updates the preview with new parameters.
     * Uses cached compiled report if available.
     */
    public void updatePreview(Map<String, Object> parameters) {
        if (parameters == null) return;
        
        try {
            // Generate the report
            JasperPrint jasperPrint = reportGenerator.generateReport(parameters);
            
            if (jasperPrint != null) {
                // Remove old viewer if exists
                previewContainer.removeAll();
                
                // Create and add new viewer
                currentViewer = new JRViewer(jasperPrint);
                previewContainer.add(currentViewer, BorderLayout.CENTER);
                previewContainer.revalidate();
                previewContainer.repaint();
            } else {
                previewContainer.removeAll();
                previewContainer.revalidate();
                previewContainer.repaint();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating preview", e);
        }
    }
    
    /**
     * Clears the preview and cached reports
     */
    public void clearPreview() {
        if (currentViewer != null) {
            previewContainer.removeAll();
            currentViewer = null;
        }
        compiledReports.clear();
        previewContainer.revalidate();
        previewContainer.repaint();
        statusLabel.setText("Preview cleared");
    }
    
    /**
     * Updates the status message
     */
    public void updateStatus(String message) {
        statusLabel.setText(message);
    }
} 