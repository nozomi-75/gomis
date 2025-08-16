package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;

public class ViolationCategory {
    private int categoryId;
    private String categoryName;
    private String categoryDescription;
    private Timestamp createdAt;

    public ViolationCategory() {
    }

    public ViolationCategory(int categoryId, String categoryName, String categoryDescription, Timestamp createdAt) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.createdAt = createdAt;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return categoryName;
    }
} 