package lyfjshs.gomis.view;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.components.charts.ReportChartContainer;
import lyfjshs.gomis.components.charts.ReportFilterPanel;
import lyfjshs.gomis.components.charts.ReportTablePanel;
import net.miginfocom.swing.MigLayout;

public class ChartPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ChartPanel.class);
	private final ViolationDAO violationDAO;
	private final ReportFilterPanel filterPanel;
	private final ReportChartContainer chartContainer;
	private final ReportTablePanel tablePanel;

	public ChartPanel(Connection connection) {
		super(new MigLayout("fill, insets 10", "[grow]", "[][500px:500px:500px][]"));
		this.violationDAO = new ViolationDAO(connection);

		// Initialize the new modular components
		filterPanel = new ReportFilterPanel();
		chartContainer = new ReportChartContainer();
		tablePanel = new ReportTablePanel();

		// Add components to this panel
		add(filterPanel, "cell 0 0,growx,wrap");
		add(chartContainer, "cell 0 1,grow,wrap");
		add(tablePanel, "cell 0 2,grow");

		// Set up the listener to connect the filter panel to the data rendering
		filterPanel.setFilterListener(this::updateDataAndRender);

		// Perform the initial data load
		updateDataAndRender(filterPanel.getStartDate(), filterPanel.getEndDate(), filterPanel.getViolationType());
	}

	/**
	 * Fetches data from the database based on the filter criteria and updates the chart and table panels.
	 *
	 * @param start The start date for the filter.
	 * @param end The end date for the filter.
	 * @param violationType The violation type to filter by.
	 */
	private void updateDataAndRender(LocalDate start, LocalDate end, String violationType) {
		try {
			Date startDate = (start != null) ? Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null;
			Date endDate = (end != null) ? Date.from(end.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null;
			String vt = (violationType != null && !"All".equals(violationType)) ? violationType : null;

			// Fetch all necessary data using the DAO
			Map<String, Map<String, Integer>> trendData = violationDAO.getViolationCountsByMonthAndCategory(startDate, endDate, vt);
			Map<String, Map<String, Integer>> countsByTypeAndCategory = violationDAO.getViolationCountsByTypeAndCategory(startDate, endDate, vt);
			Map<String, Integer> countsByType = new TreeMap<>();
			countsByTypeAndCategory.forEach((vType, categoryMap) -> 
				countsByType.put(vType, categoryMap.values().stream().mapToInt(Integer::intValue).sum())
			);
			List<Object[]> tableData = violationDAO.getViolationReportData(startDate, endDate, vt, null, null);

			// Update the UI components with the new data
			chartContainer.updateBarChart(countsByTypeAndCategory, "Violations by Type");
			chartContainer.updateLineChart(trendData, "Violation Trend Over Time");
			chartContainer.updatePieChart(countsByType, "Violation Distribution");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String summaryText = String.format("Showing %d violation records from %s to %s for %s",
					tableData.size(),
					startDate != null ? sdf.format(startDate) : "start",
					endDate != null ? sdf.format(endDate) : "end",
					vt != null ? vt : "All Violation Types");
			tablePanel.updateTableData(tableData, summaryText);

		} catch (SQLException e) {
			logger.error("Error fetching data from the database", e);
			JOptionPane.showMessageDialog(this, "Error fetching data from the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Public method to allow external components to trigger a data refresh.
	 */
	public void refreshData() {
		updateDataAndRender(filterPanel.getStartDate(), filterPanel.getEndDate(), filterPanel.getViolationType());
	}
}
