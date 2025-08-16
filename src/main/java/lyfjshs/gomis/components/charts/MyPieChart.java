package lyfjshs.gomis.components.charts;

import java.awt.Font;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 * A custom chart panel for displaying a Pie Chart with the application's default theme.
 */
@SuppressWarnings({"rawtypes", "unchecked"})

public class MyPieChart extends JPanel {
    private JFreeChart chart;
    private PiePlot plot;
    private String title;
    private ChartPanel chartPanel;

    public MyPieChart() {
        super(new java.awt.BorderLayout());
        setOpaque(false);
        createChart();
    }

    private void createChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        plot = (PiePlot) chart.getPlot();
        plot.setSectionOutlinesVisible(false);
        plot.setShadowPaint(null);
        plot.setBackgroundPaint(null);
        plot.setOutlinePaint(null);
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        plot.setLabelBackgroundPaint(new java.awt.Color(255, 255, 255, 100));
        chart.setBackgroundPaint(null);

        chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBorder(null);
        add(chartPanel);
    }

    public void setDataset(DefaultPieDataset<String> dataset) {
        plot.setDataset(dataset);
    }

    public void setTitle(String title) {
        this.title = title;
        if (chartPanel != null) {
            chartPanel.getChart().setTitle(title);
        }
    }

    public JFreeChart getChart() {
        if (chartPanel != null) {
            return chartPanel.getChart();
        }
        return null;
    }
}
