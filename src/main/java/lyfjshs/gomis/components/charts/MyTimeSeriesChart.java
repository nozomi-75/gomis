package lyfjshs.gomis.components.charts;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.UIManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimeSeriesCollection;

import lyfjshs.gomis.components.charts.MyToolBarTimeSeriesChartRenderer.ChartXYBarRenderer;

public class MyTimeSeriesChart extends DefaultChartPanel {

    private XYItemRenderer renderer;

    public MyTimeSeriesChart() {
        super();
    }

    @Override
    protected JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Violations Over Time",
            "Date",
            "Count",
            new TimeSeriesCollection(),
            true,
            true,
            false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM dd"));
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
        rangeAxis.setLabel("Count");
        
        return chart;
    }

    public void setRenderer(XYItemRenderer renderer) {
        if (this.renderer != renderer) {
            this.renderer = renderer;
            XYPlot plot = (XYPlot) freeChart.getPlot();
            plot.setRenderer(renderer);
            DefaultChartTheme.applyTheme(freeChart);
        }
    }

    @Override
    protected void defaultStyleChart(JFreeChart chart, ChartPanel panel) {
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        DateAxis domain = (DateAxis) plot.getDomainAxis();

        range.setAxisLineVisible(false);
        range.setTickMarksVisible(false);
        range.setUpperMargin(0.2);
        range.setLowerMargin(0.1);

        domain.setAxisLineVisible(false);
        domain.setTickMarksVisible(false);

        plot.setDomainPannable(true);
        plot.setRangeGridlinesVisible(false);

        plot.setRenderer(getDefaultRender());
    }

    @Override
    protected void styleChart(JFreeChart chart, ChartPanel panel) {
        XYPlot plot = (XYPlot) chart.getPlot();
        Color background = getBackground();
        Color foreground = getForeground();
        Font font = getFont();
        Color selectionColor = UIManager.getColor("List.selectionBackground");
        Color border = UIManager.getColor("Component.borderColor");

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        DateAxis domain = (DateAxis) plot.getDomainAxis();

        range.setTickLabelInsets(ChartDrawingSupplier.scaleRectangleInsets(Axis.DEFAULT_TICK_LABEL_INSETS));
        domain.setTickLabelInsets(ChartDrawingSupplier.scaleRectangleInsets(Axis.DEFAULT_TICK_LABEL_INSETS));

        plot.setDomainGridlineStroke(ChartDrawingSupplier.getDefaultGridlineStroke());
        plot.setInsets(ChartDrawingSupplier.scaleRectangleInsets(4, 8, 15, 8));

        // annotation
        MultiXYTextAnnotation annotation = (MultiXYTextAnnotation) plot.getAnnotations().get(0);
        annotation.setBackgroundPaint(background);
        annotation.setDefaultPaint(foreground);
        annotation.setFont(font);
        annotation.setOutlinePaint(border);
        annotation.setTitleLinePain(border);
        annotation.setGridLinePaint(selectionColor);
    }


    @Override
    protected void createAnnotation(JFreeChart chart, ChartPanel chartPanel) {
        XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
        MultiXYTextAnnotation annotation = new MultiXYTextAnnotation();

        DateFormat titleFormat = DateFormat.getDateInstance();
        NumberFormat valueFormat = NumberFormat.getNumberInstance();
        annotation.setTitleGenerator(xValue -> titleFormat.format(new Date((long) xValue)));
        annotation.setNumberFormat(valueFormat);

        plot.addAnnotation(annotation);
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                Rectangle2D dataArea = chartPanel.getScreenDataArea();
                if (!dataArea.contains(event.getTrigger().getPoint())) {
                    annotation.setLabels(null);
                    return;
                }
                double x = plot.getDomainAxis().java2DToValue(event.getTrigger().getX(), dataArea, plot.getDomainAxisEdge());
                TimeSeriesCollection dataset = (TimeSeriesCollection) plot.getDataset();
                int seriesCount = plot.getSeriesCount();

                double minDistance = Double.MAX_VALUE;
                double closestX = 0;
                boolean found = false;

                for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
                    for (int itemIndex = 0; itemIndex < plot.getDataset().getItemCount(seriesIndex); itemIndex++) {
                        double dataX = dataset.getXValue(seriesIndex, itemIndex);
                        double distance = Math.abs(x - dataX);
                        if (distance < minDistance) {
                            minDistance = distance;
                            closestX = dataX;
                            found = true;
                        }
                    }
                }
                if (found) {
                    annotation.autoCalculateX(closestX, dataset);
                }
            }
        });
    }

    public XYItemRenderer getDefaultRender() {
        if (renderer == null) {
            // Set the default renderer to the Bar Chart renderer
            renderer = new ChartXYBarRenderer();
        }
        return renderer;
    }

    @SuppressWarnings("unchecked")
    public void setDataset(TimeSeriesCollection dataset) {
        freeChart.getXYPlot().setDataset(dataset);
    }
}
