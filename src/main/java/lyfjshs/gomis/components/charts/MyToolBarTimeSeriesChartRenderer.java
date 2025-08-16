package lyfjshs.gomis.components.charts;

import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import org.jfree.chart.renderer.xy.XYBezierRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import com.formdev.flatlaf.util.UIScale;

public class MyToolBarTimeSeriesChartRenderer extends ToolBarSelection<XYItemRenderer> {

    public MyToolBarTimeSeriesChartRenderer(MyTimeSeriesChart chart) {
        super(getRenderers(), renderer -> {
            chart.setRenderer(renderer);
        });
    }

    public static class ChartXYCurveRenderer extends XYBezierRenderer {

        private static final int precision = 10;
        private static final double tension = 25;

        public ChartXYCurveRenderer() {
            this(UIScale.scale(precision), UIScale.scale((float) tension));
        }

        public ChartXYCurveRenderer(int precision, double tension) {
            super(precision, tension);
            initStyle();
        }

        private void initStyle() {
            setAutoPopulateSeriesOutlinePaint(true);
            setAutoPopulateSeriesOutlineStroke(true);
            setUseOutlinePaint(true);
        }

        @Override
        public String toString() {
            return "Curve";
        }
    }

    public static class ChartXYLineRenderer extends ChartXYCurveRenderer {

        public ChartXYLineRenderer() {
            super(1, 1);
        }

        @Override
        public String toString() {
            return "Line";
        }
    }

    public static class ChartXYBarRenderer extends ClusteredXYBarRenderer {

        public ChartXYBarRenderer() {
            setBarPainter(DefaultChartTheme.getInstance().getXYBarPainter());
            setShadowVisible(DefaultChartTheme.getInstance().isShadowVisible());
            setMargin(0.3);
        }

        @Override
        public String toString() {
            return "Bar";
        }
    }

    private static XYItemRenderer[] getRenderers() {
        XYItemRenderer[] renderers = new XYItemRenderer[]{
                new ChartXYBarRenderer(),
                new ChartXYCurveRenderer(),
                new ChartXYLineRenderer(),
        };
        // All renderer classes are now static nested classes, resolving potential 'no enclosing instance' issues.
        return renderers;
    }
} 