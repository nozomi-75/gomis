package LEGACY_test_unused;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.components.charts.ReportChartContainer;
import lyfjshs.gomis.components.charts.ReportFilterPanel;
import lyfjshs.gomis.components.charts.ReportTablePanel;
import net.miginfocom.swing.MigLayout;

public class TestCharts_PANELS_MAIN_FRAME extends JFrame {
	private static final long serialVersionUID = 1L;
	private ReportFilterPanel filterPanel;
	private ReportChartContainer chartContainer;
	private ReportTablePanel tablePanel;

	public static void main(String[] args) {
		try {
			FlatLaf.registerCustomDefaultsSource("lyfjshs.gomis.themes");
			UIManager.setLookAndFeel(new FlatMacLightLaf());
		} catch (Exception ex) {
			System.err.println("Failed to initialize LaF");
		}
		java.awt.EventQueue.invokeLater(() -> {
			TestCharts_PANELS_MAIN_FRAME frame = new TestCharts_PANELS_MAIN_FRAME();
			frame.setVisible(true);
		});
	}

	public TestCharts_PANELS_MAIN_FRAME() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1366, 768);
		setLocationRelativeTo(null);
		setTitle("Bullying Violation Statistics Report");

		JPanel contentPane = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[][grow][]"));
		setContentPane(contentPane);

		filterPanel = new ReportFilterPanel();
		chartContainer = new ReportChartContainer();
		tablePanel = new ReportTablePanel();

		contentPane.add(filterPanel, "cell 0 0,growx,wrap");
		contentPane.add(chartContainer, "cell 0 1,growx,wrap");
		contentPane.add(tablePanel, "cell 0 2,growx,growy");

		filterPanel.setFilterListener((start, end, violationType) -> updateDataAndRender(start, end, violationType));
		// Initial load
		updateDataAndRender(filterPanel.getStartDate(), filterPanel.getEndDate(), filterPanel.getViolationType());
	}

	private void updateDataAndRender(LocalDate start, LocalDate end, String violationType) {
		try (Connection conn = DBConnection.getConnection()) {
			ViolationDAO violationDAO = new ViolationDAO(conn);
			Date startDate = (start != null) ? Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null;
			Date endDate = (end != null) ? Date.from(end.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null;
			String vt = (violationType != null && !"All".equals(violationType)) ? violationType : null;

			Map<String, Map<String, Integer>> trendData = violationDAO.getViolationCountsByMonthAndCategory(startDate, endDate, vt);
			Map<String, Map<String, Integer>> countsByTypeAndCategory = violationDAO.getViolationCountsByTypeAndCategory(startDate, endDate, vt);
			Map<String, Integer> countsByType = new TreeMap<>();
			countsByTypeAndCategory.forEach((vType, categoryMap) -> {
				countsByType.put(vType, categoryMap.values().stream().mapToInt(Integer::intValue).sum());
			});
			List<Object[]> tableData = violationDAO.getViolationReportData(startDate, endDate, vt, null, null);

			chartContainer.updateBarChart(countsByTypeAndCategory, "Violations by Type");
			chartContainer.updateLineChart(trendData, "Violation Trend Over Time");
			chartContainer.updatePieChart(countsByType, "Violation Distribution");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String summaryText = String.format("Showing %d violation records from %s to %s for %s", tableData.size(),
					startDate != null ? sdf.format(startDate) : "start",
					endDate != null ? sdf.format(endDate) : "end",
					vt != null ? vt : "All Violation Types");
			tablePanel.updateTableData(tableData, summaryText);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error fetching data from the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
