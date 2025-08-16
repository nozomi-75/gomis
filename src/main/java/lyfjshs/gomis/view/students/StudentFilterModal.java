package lyfjshs.gomis.view.students;

import java.awt.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;

public class StudentFilterModal {
    private static final Logger logger = LogManager.getLogger(StudentFilterModal.class);
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

    public void showModal(StudentsDataDAO dbManager, Component parent, FilterCriteria filterCriteria, FilterDialogPanel filterPanel, int width, int height) {
        try {
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
                            if (filterPanel.validateFilters()) {
                                filterPanel.applyFilters();
                                controller.close();
                                // Force parent to reload data
                                if (parent instanceof StudentsListMain) {
                                    ((StudentsListMain) parent).loadData();
                                }
                            }
                        } else if (action == SimpleModalBorder.NO_OPTION) {
                            // Reset filters
                            filterPanel.clearFiltersAndApply();
                            controller.close();
                            // Force parent to reload data
                            if (parent instanceof StudentsListMain) {
                                ((StudentsListMain) parent).loadData();
                            }
                        } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                            // Close button clicked
                            controller.close();
                        } else {
                            // Any other action (like clicking outside) should be consumed
                            controller.consume();
                        }
                        
                    }), option, "student-filter");

        } catch (Exception e) {
            logger.error("Error showing student filter modal", e);
        }
    }
} 