/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.students;
/**
 * FilterCriteria Class
 * Holds the criteria for filtering student data.
 */
public class FilterCriteria {
    String searchTerm = ""; // General search for LRN or parts of name
    String filterFirstName = "";
    String filterLastName = "";
    String filterMiddleName = "";
    boolean middleInitialOnly = false;
    String filterGradeLevel = "All";
    String filterSection = "All"; // Added section filter
    String filterTrackStrand = "All";
    boolean filterMale = true;
    boolean filterFemale = true;
    int minAge = 0;
    int maxAge = 100;

    public FilterCriteria() {}

    /**
     * Resets all filter criteria to their default values.
     * @param dbMinAge The minimum age found in the database (for default setting).
     * @param dbMaxAge The maximum age found in the database (for default setting).
     */
    public void reset(int dbMinAge, int dbMaxAge) {
        searchTerm = "";
        filterFirstName = "";
        filterLastName = "";
        filterMiddleName = "";
        middleInitialOnly = false;
        filterGradeLevel = "All";
        filterSection = "All";
        filterTrackStrand = "All";
        filterMale = true;
        filterFemale = true;
        minAge = dbMinAge;
        maxAge = dbMaxAge > dbMinAge ? dbMaxAge + 5 : dbMinAge + 20; // Default max age with a small buffer
    }

    /**
     * Checks if any specific filters (beyond search term and default age/sex) are active.
     * @param dbMinAge Minimum age from DB for default comparison.
     * @param dbMaxAge Maximum age from DB for default comparison.
     * @return True if specific filters are active, false otherwise.
     */
    public boolean hasActiveSpecificFilters(int dbMinAge, int dbMaxAge) {
        if (filterFirstName != null && !filterFirstName.isEmpty()) return true;
        if (filterLastName != null && !filterLastName.isEmpty()) return true;
        if (filterMiddleName != null && !filterMiddleName.isEmpty()) return true;
        if (!"All".equals(filterGradeLevel)) return true;
        if (!"All".equals(filterSection)) return true;
        if (!"All".equals(filterTrackStrand)) return true;
        // Check if sex filter is non-default (i.e., not both true)
        if (!filterMale || !filterFemale) {
            if (filterMale != filterFemale) return true; // Only one is selected
            if (!filterMale && !filterFemale) return true; // None selected (effectively filtering all out)
        }
        // Check if age is different from default range
        int defaultMaxAge = dbMaxAge > dbMinAge ? dbMaxAge + 5 : dbMinAge + 20;
        if (minAge != dbMinAge || maxAge != defaultMaxAge) return true;

        return false;
    }

    /**
     * Counts the number of active filters.
     * @param dbMinAge Minimum age from DB for default comparison.
     * @param dbMaxAge Maximum age from DB for default comparison.
     * @return The count of active filters.
     */
    public int getActiveFilterCount(int dbMinAge, int dbMaxAge) {
        int count = 0;
        if (searchTerm != null && !searchTerm.isEmpty()) count++;
        if (filterFirstName != null && !filterFirstName.isEmpty()) count++;
        if (filterLastName != null && !filterLastName.isEmpty()) count++;
        if (filterMiddleName != null && !filterMiddleName.isEmpty()) count++;
        if (!"All".equals(filterGradeLevel)) count++;
        if (!"All".equals(filterSection)) count++;
        if (!"All".equals(filterTrackStrand)) count++;
        if (!filterMale || !filterFemale) { // If not both are true (default)
             if (filterMale != filterFemale) count++; // Only one selected counts as a filter
             else if (!filterMale && !filterFemale) count++; // Neither selected also counts
        }
        int defaultMaxAge = dbMaxAge > dbMinAge ? dbMaxAge + 5 : dbMinAge + 20;
        if (minAge != dbMinAge || maxAge != defaultMaxAge) {
            count++;
        }
        return count;
    }
}
