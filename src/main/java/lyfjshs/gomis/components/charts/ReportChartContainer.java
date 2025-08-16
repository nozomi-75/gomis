package lyfjshs.gomis.components.charts;

import java.awt.CardLayout;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import net.miginfocom.swing.MigLayout;

public class ReportChartContainer extends JPanel {
    private static final String BAR_CHART = "Bar Chart";
    private static final String LINE_CHART = "Line Chart";
    private static final String PIE_CHART = "Pie Chart";

    private final CardLayout cardLayout;
    private final JPanel chartPanelContainer;
    private final JPanel barChartPanel;
    private final JPanel lineChartPanel;
    private final JPanel pieChartPanel;

    public ReportChartContainer() {
        super(new MigLayout("fill, insets 10", "[grow]", "[][grow]"));
        // Toolbar
        JToolBar chartToolbar = new JToolBar();
        chartToolbar.setFloatable(false);
        chartToolbar.setOpaque(false);
        chartToolbar.setBorder(null);

        JToggleButton barButton = new JToggleButton("Violations by Type");
        JToggleButton lineButton = new JToggleButton("Violation Trend");
        JToggleButton pieButton = new JToggleButton("Violation Distribution");
        barButton.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(barButton);
        group.add(lineButton);
        group.add(pieButton);

        chartToolbar.add(new JLabel("Chart View:"));
        chartToolbar.add(barButton);
        chartToolbar.add(lineButton);
        chartToolbar.add(pieButton);
        add(chartToolbar, "ax right, wrap");

        // CardLayout for charts
        cardLayout = new CardLayout();
        chartPanelContainer = new JPanel(cardLayout);
        barChartPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
        lineChartPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
        pieChartPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
        chartPanelContainer.add(barChartPanel, BAR_CHART);
        chartPanelContainer.add(lineChartPanel, LINE_CHART);
        chartPanelContainer.add(pieChartPanel, PIE_CHART);
        add(chartPanelContainer, "grow");

        // Button listeners
        barButton.addActionListener(e -> cardLayout.show(chartPanelContainer, BAR_CHART));
        lineButton.addActionListener(e -> cardLayout.show(chartPanelContainer, LINE_CHART));
        pieButton.addActionListener(e -> cardLayout.show(chartPanelContainer, PIE_CHART));
    }

    public void updateBarChart(Map<String, Map<String, Integer>> data, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Set<String> categories = new TreeSet<>();
        data.values().forEach(innerMap -> categories.addAll(innerMap.keySet()));
        for (String violationType : data.keySet()) {
            Map<String, Integer> categoryCounts = data.get(violationType);
            for (String category : categories) {
                dataset.addValue(categoryCounts.getOrDefault(category, 0), category, violationType);
            }
        }
        JFreeChart barChart = ChartFactory.createBarChart(
            title,
            "Violation Type",
            "Count",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator("<html><b>{1}</b> ({0})<br>Count: {2}</html>", NumberFormat.getInstance()));
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setOpaque(false);
        barChartPanel.removeAll();
        barChartPanel.add(chartPanel, "grow");
        barChartPanel.revalidate();
        barChartPanel.repaint();
    }

    public void updateLineChart(Map<String, Map<String, Integer>> data, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String month : data.keySet()) {
            int total = data.get(month).values().stream().mapToInt(Integer::intValue).sum();
            dataset.addValue(total, "Violations", month);
        }
        JFreeChart lineChart = ChartFactory.createLineChart(
            title,
            "Month",
            "Count",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setOpaque(false);
        lineChartPanel.removeAll();
        lineChartPanel.add(chartPanel, "grow");
        lineChartPanel.revalidate();
        lineChartPanel.repaint();
    }

    public void updatePieChart(Map<String, Integer> data, String title) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        JFreeChart pieChart = ChartFactory.createPieChart(
            title,
            dataset,
            true,
            true,
            false
        );
        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setOpaque(false);
        pieChartPanel.removeAll();
        pieChartPanel.add(chartPanel, "grow");
        pieChartPanel.revalidate();
        pieChartPanel.repaint();
    }
} 