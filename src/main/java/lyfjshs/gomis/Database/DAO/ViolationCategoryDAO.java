/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.ViolationCategory;

public class ViolationCategoryDAO {
    private Connection connection;

    public ViolationCategoryDAO(Connection connection) {
        this.connection = connection;
    }

    public List<ViolationCategory> getAllCategories() throws SQLException {
        List<ViolationCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM VIOLATION_CATEGORIES ORDER BY CATEGORY_NAME";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ViolationCategory category = new ViolationCategory(
                    rs.getInt("CATEGORY_ID"),
                    rs.getString("CATEGORY_NAME"),
                    rs.getString("CATEGORY_DESCRIPTION"),
                    rs.getTimestamp("CREATED_AT")
                );
                categories.add(category);
            }
        }

        return categories;
    }

    public ViolationCategory getCategoryById(int categoryId) throws SQLException {
        String sql = "SELECT * FROM VIOLATION_CATEGORIES WHERE CATEGORY_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ViolationCategory(
                        rs.getInt("CATEGORY_ID"),
                        rs.getString("CATEGORY_NAME"),
                        rs.getString("CATEGORY_DESCRIPTION"),
                        rs.getTimestamp("CREATED_AT")
                    );
                }
            }
        }

        return null;
    }

    public ViolationCategory getCategoryByName(String categoryName) throws SQLException {
        String sql = "SELECT * FROM VIOLATION_CATEGORIES WHERE CATEGORY_NAME = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ViolationCategory(
                        rs.getInt("CATEGORY_ID"),
                        rs.getString("CATEGORY_NAME"),
                        rs.getString("CATEGORY_DESCRIPTION"),
                        rs.getTimestamp("CREATED_AT")
                    );
                }
            }
        }

        return null;
    }

    public boolean addCategory(ViolationCategory category) throws SQLException {
        String sql = "INSERT INTO VIOLATION_CATEGORIES (CATEGORY_NAME, CATEGORY_DESCRIPTION) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getCategoryDescription());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateCategory(ViolationCategory category) throws SQLException {
        String sql = "UPDATE VIOLATION_CATEGORIES SET CATEGORY_NAME = ?, CATEGORY_DESCRIPTION = ? WHERE CATEGORY_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getCategoryDescription());
            pstmt.setInt(3, category.getCategoryId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCategory(int categoryId) throws SQLException {
        String sql = "DELETE FROM VIOLATION_CATEGORIES WHERE CATEGORY_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            return pstmt.executeUpdate() > 0;
        }
    }
} 