/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

public class SchoolForm {
    private int SF_ID;
    private String SF_SCHOOL_NAME;
    private String SF_SCHOOL_ID;
    private String SF_DISTRICT;
    private String SF_DIVISION;
    private String SF_REGION;
    private String SF_SEMESTER;
    private String SF_SCHOOL_YEAR;
    private String SF_GRADE_LEVEL;
    private String SF_SECTION;
    private String SF_TRACK_AND_STRAND;
    private String SF_COURSE;

    // ✅ Default Constructor
    public SchoolForm() {
    }

    // ✅ Constructor with all fields
    public SchoolForm(int SF_ID, String SF_SCHOOL_NAME, String SF_SCHOOL_ID, String SF_DISTRICT,
            String SF_DIVISION, String SF_REGION, String SF_SEMESTER, String SF_SCHOOL_YEAR,
            String SF_GRADE_LEVEL, String SF_SECTION, String SF_TRACK_AND_STRAND, String SF_COURSE) {
        this.SF_ID = SF_ID;
        this.SF_SCHOOL_NAME = SF_SCHOOL_NAME;
        this.SF_SCHOOL_ID = SF_SCHOOL_ID;
        this.SF_DISTRICT = SF_DISTRICT;
        this.SF_DIVISION = SF_DIVISION;
        this.SF_REGION = SF_REGION;
        this.SF_SEMESTER = SF_SEMESTER;
        this.SF_SCHOOL_YEAR = SF_SCHOOL_YEAR;
        this.SF_GRADE_LEVEL = SF_GRADE_LEVEL;
        this.SF_SECTION = SF_SECTION;
        this.SF_TRACK_AND_STRAND = SF_TRACK_AND_STRAND;
        this.SF_COURSE = SF_COURSE;
    }

    // ✅ Getters and Setters
    public int getSF_ID() {
        return SF_ID;
    }

    public void setSF_ID(int SF_ID) {
        this.SF_ID = SF_ID;
    }

    public String getSF_SCHOOL_NAME() {
        return SF_SCHOOL_NAME;
    }

    public void setSF_SCHOOL_NAME(String SF_SCHOOL_NAME) {
        this.SF_SCHOOL_NAME = SF_SCHOOL_NAME;
    }

    public String getSF_SCHOOL_ID() {
        return SF_SCHOOL_ID;
    }

    public void setSF_SCHOOL_ID(String SF_SCHOOL_ID) {
        this.SF_SCHOOL_ID = SF_SCHOOL_ID;
    }

    public String getSF_DISTRICT() {
        return SF_DISTRICT;
    }

    public void setSF_DISTRICT(String SF_DISTRICT) {
        this.SF_DISTRICT = SF_DISTRICT;
    }

    public String getSF_DIVISION() {
        return SF_DIVISION;
    }

    public void setSF_DIVISION(String SF_DIVISION) {
        this.SF_DIVISION = SF_DIVISION;
    }

    public String getSF_REGION() {
        return SF_REGION;
    }

    public void setSF_REGION(String SF_REGION) {
        this.SF_REGION = SF_REGION;
    }

    public String getSF_SEMESTER() {
        return SF_SEMESTER;
    }

    public void setSF_SEMESTER(String SF_SEMESTER) {
        this.SF_SEMESTER = SF_SEMESTER;
    }

    public String getSF_SCHOOL_YEAR() {
        return SF_SCHOOL_YEAR;
    }

    public void setSF_SCHOOL_YEAR(String SF_SCHOOL_YEAR) {
        this.SF_SCHOOL_YEAR = SF_SCHOOL_YEAR;
    }

    public String getSF_GRADE_LEVEL() {
        return SF_GRADE_LEVEL;
    }

    public void setSF_GRADE_LEVEL(String SF_GRADE_LEVEL) {
        this.SF_GRADE_LEVEL = SF_GRADE_LEVEL;
    }

    public String getSF_SECTION() {
        return SF_SECTION;
    }

    public void setSF_SECTION(String SF_SECTION) {
        this.SF_SECTION = SF_SECTION;
    }

    public String getSF_TRACK_AND_STRAND() {
        return SF_TRACK_AND_STRAND;
    }

    public void setSF_TRACK_AND_STRAND(String SF_TRACK_AND_STRAND) {
        this.SF_TRACK_AND_STRAND = SF_TRACK_AND_STRAND;
    }

    public String getSF_COURSE() {
        return SF_COURSE;
    }

    public void setSF_COURSE(String SF_COURSE) {
        this.SF_COURSE = SF_COURSE;
    }
}