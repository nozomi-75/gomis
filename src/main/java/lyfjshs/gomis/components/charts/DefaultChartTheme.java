package lyfjshs.gomis.components.charts;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.RectangularShape;

import javax.swing.UIManager;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleEdge;

import com.formdev.flatlaf.util.UIScale;

public class DefaultChartTheme extends StandardChartTheme {

    public static DefaultChartTheme getInstance() {
        return instance;
    }

    private static DefaultChartTheme instance = new DefaultChartTheme();
    public ColorThemes colorThemes = ColorThemes.DEFAULT;

    private DefaultChartTheme() {
        super("Default Themes", false);
        init();
    }

    private void init() {
        Color background = new Color(0, 0, 0, 0);
        Color foreground = UIManager.getColor("Label.foreground");
        Color border = UIManager.getColor("Component.borderColor");
        Font font = UIManager.getFont("Label.font");

        setDrawingSupplier(new ChartDrawingSupplier(colorThemes));
        // chart
        setChartBackgroundPaint(background);

        // plot
        setPlotBackgroundPaint(background);
        setPlotOutlinePaint(background);

        // renderer
        setDomainGridlinePaint(border);
        setRangeGridlinePaint(border);

        setBarPainter(new AlphaBarPainter());
        setXYBarPainter(new StandardXYBarPainter());

        // text
        setRegularFont(font);
        setTitlePaint(foreground);
        setSubtitlePaint(foreground);
        setTickLabelPaint(foreground);
        setItemLabelPaint(foreground);
        setLabelLinkPaint(border);

        // legend
        setLegendBackgroundPaint(background);
        setLegendItemPaint(foreground);

        // other
    }

    public static boolean setChartColors(ColorThemes colorThemes) {
        if (instance.colorThemes != colorThemes) {
            instance.colorThemes = colorThemes;
            instance.setDrawingSupplier(new ChartDrawingSupplier(colorThemes));
            return true;
        }
        return false;
    }

    public static Color getColor(int index) {
        Color[] colors = instance.colorThemes.getColors();
        if (index > colors.length - 1) {
            return colors[colors.length - 1];
        }
        return colors[index];
    }

    public static Color[] getColors() {
        return instance.colorThemes.getColors();
    }

    public static void applyTheme(JFreeChart chart) {
        instance.init();
        instance.apply(chart);
    }

    @Override
    protected void applyToSpiderWebPlot(SpiderWebPlot plot) {
        Color border = UIManager.getColor("Component.borderColor");
        plot.setLabelFont(getRegularFont());
        plot.setLabelPaint(getAxisLabelPaint());
        plot.setAxisLinePaint(border);
        int index = 0;
        for (Color color : instance.colorThemes.getColors()) {
            boolean alpha = false;
            Paint olePaint = plot.getSeriesPaint(index);
            if (olePaint instanceof Color) {
                alpha = ((Color) olePaint).getAlpha() < 255;
            }
            Paint c = alpha ? ChartDrawingSupplier.alpha(color, 0.3f) : color;
            plot.setSeriesPaint(index, c);
            plot.setSeriesOutlinePaint(index, c);
            plot.setSeriesOutlineStroke(index, new BasicStroke(UIScale.scale(1f)));
            index++;
        }
    }

    @Override
    protected void applyToXYItemRenderer(XYItemRenderer renderer) {
        super.applyToXYItemRenderer(renderer);
        if (renderer != null) {
            if (renderer instanceof XYDifferenceRenderer) {
                XYDifferenceRenderer r = (XYDifferenceRenderer) renderer;
                if (r.getAutoPopulateSeriesPaint()) {
                    r.setPositivePaint(getColor(0));
                    r.setNegativePaint(getColor(1));
                }
            } else if (renderer instanceof CandlestickRenderer) {
                CandlestickRenderer r = (CandlestickRenderer) renderer;
                if (r.getAutoPopulateSeriesPaint()) {
                    r.setDownPaint(getColor(0));
                    r.setUpPaint(getColor(1));
                }
            }
        }
    }

    @Override
    protected void applyToAbstractRenderer(AbstractRenderer renderer) {
        super.applyToAbstractRenderer(renderer);

        // apply null to series paint to get the new series paint
        if (renderer.getAutoPopulateSeriesOutlinePaint()) {
            int index = 0;
            while (renderer.getSeriesOutlinePaint(index) != null) {
                renderer.setSeriesOutlinePaint(index, null);
                index++;
            }
        }
    }

    @Override
    protected void applyToPiePlot(PiePlot plot) {
        plot.setLabelPaint(getItemLabelPaint());
        super.applyToPiePlot(plot);
    }

    public class AlphaBarPainter extends StandardBarPainter {
        @Override
        public void paintBar(Graphics2D g2, BarRenderer renderer, int row, int column, RectangularShape bar, RectangleEdge base) {
            g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
            super.paintBar(g2, renderer, row, column, bar, base);
        }
    }

    public static enum ColorThemes {
        DEFAULT(
                Color.decode("#fd7f6f"),
                Color.decode("#7eb0d5"),
                Color.decode("#b2e061"),
                Color.decode("#bd7ebe"),
                Color.decode("#ffb55a"),
                Color.decode("#ffee65"),
                Color.decode("#beb9db"),
                Color.decode("#fdcce5"),
                Color.decode("#8bd3c7")
        ),
        RETRO_METRO(
                Color.decode("#ea5545"),
                Color.decode("#f46a9b"),
                Color.decode("#ef9b20"),
                Color.decode("#edbf33"),
                Color.decode("#ede15b"),
                Color.decode("#bdcf32"),
                Color.decode("#87bc45"),
                Color.decode("#27aeef"),
                Color.decode("#b33dc6")
        ),
        BLUE_TO_YELLOW(
                Color.decode("#115f9a"),
                Color.decode("#1984c5"),
                Color.decode("#22a7f0"),
                Color.decode("#48b5c4"),
                Color.decode("#76c68f"),
                Color.decode("#a6d75b"),
                Color.decode("#c9e52f"),
                Color.decode("#d0ee11"),
                Color.decode("#d0f400")
        ),
        SALMON_TO_AQUA(
                Color.decode("#e27c7c"),
                Color.decode("#a86464"),
                Color.decode("#6d4b4b"),
                Color.decode("#503f3f"),
                Color.decode("#333333"),
                Color.decode("#3c4e4b"),
                Color.decode("#466964"),
                Color.decode("#599e94"),
                Color.decode("#6cd4c5")
        ),
        LIGHT(
                Color.decode("#ffffff"),
                Color.decode("#f0f0f0"),
                Color.decode("#d9d9d9"),
                Color.decode("#b3b3b3"),
                Color.decode("#999999"),
                Color.decode("#808080"),
                Color.decode("#666666"),
                Color.decode("#333333"),
                Color.decode("#000000")
        ),
        DARK(
                Color.decode("#444444"),
                Color.decode("#555555"),
                Color.decode("#666666"),
                Color.decode("#777777"),
                Color.decode("#888888"),
                Color.decode("#999999"),
                Color.decode("#aaaaaa"),
                Color.decode("#bbbbbb"),
                Color.decode("#cccccc")
        );

        private Color[] colors;

        ColorThemes(Color... colors) {
            this.colors = colors;
        }

        public Color[] getColors() {
            return colors;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
