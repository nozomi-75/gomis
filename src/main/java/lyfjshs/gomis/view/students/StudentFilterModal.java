package lyfjshs.gomis.view.students;

import java.awt.Component;
import java.sql.Connection;
import java.util.Map;

import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;

public class StudentFilterModal {
    private static StudentFilterModal instance;

    private StudentFilterModal() {
        // Private constructor to enforce singleton pattern
    }

    public static StudentFilterModal getInstance() {
        if (instance == null) {
            instance = new StudentFilterModal();
        }
        return instance;
    }

    public void showModal(Connection connection, Component parent, StudentMangementGUI studentManagementGUI, 
                         Map<String, String> activeFilters, int width, int height) {
        try {
            // Create the filter panel
            StudentFilterPanel filterPanel = new StudentFilterPanel(connection, activeFilters);

            // Configure modal options
            Option option = ModalDialog.getDefaultOption();
            option.setOpacity(0f)
                  .setAnimationOnClose(false)
                  .getBorderOption()
                  .setBorderWidth(0.5f)
                  .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

            // Configure layout options
            option.getLayoutOption()
                  .setMargin(0, 0, 0, 0)
                  .setSize(width, height);

            // Show modal with proper size and validation
            ModalDialog.showModal(parent, new SimpleModalBorder(filterPanel, "Filter Students",
                    new SimpleModalBorder.Option[] { 
                        new SimpleModalBorder.Option("Apply", SimpleModalBorder.YES_OPTION),
                        new SimpleModalBorder.Option("Reset", SimpleModalBorder.NO_OPTION),
                        new SimpleModalBorder.Option("Close", SimpleModalBorder.CANCEL_OPTION) 
                    },
                    (controller, action) -> {
                        if (action == SimpleModalBorder.YES_OPTION) {
                            // Apply filters
                            Map<String, String> filters = filterPanel.getFilters();
                            studentManagementGUI.setActiveFilters(filters);
                            filterPanel.updateFilterCount(filters.size());
                            controller.close();
                        } else if (action == SimpleModalBorder.NO_OPTION) {
                            // Reset filters
                            studentManagementGUI.setActiveFilters(null);
                            filterPanel.resetFilters();
                            filterPanel.updateFilterCount(0);
                            controller.consume(); // Don't close the dialog
                        } else if (action == SimpleModalBorder.CANCEL_OPTION) {
                            // Close button clicked
                            controller.close();
                        } else {
                            // Any other action (like clicking outside) should be consumed
                            controller.consume();
                        }
                    }), option, "student-filter");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 