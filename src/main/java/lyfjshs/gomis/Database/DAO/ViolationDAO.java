package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.Database.entity.ViolationCategory;

public class ViolationDAO {
    // Violation categories
    public static final String CATEGORY_PHYSICAL = "Physical";
    public static final String CATEGORY_VERBAL = "Verbal";
    public static final String CATEGORY_EMOTIONAL = "Emotional";
    public static final String CATEGORY_SEXUAL = "Sexual";
    public static final String CATEGORY_CYBER = "Cyber";

    private final Connection connection;
    private ViolationCategoryDAO categoryDAO;
    private ParticipantsDAO participantsDAO;
    private StudentsDataDAO studentsDataDAO;

    public ViolationDAO(Connection connection) {
        this.connection = connection;
        this.categoryDAO = new ViolationCategoryDAO(connection);
        this.participantsDAO = new ParticipantsDAO(connection);
        this.studentsDataDAO = new StudentsDataDAO(connection);
    }

    // Create a new violation and return the generated ID
    public int createViolation(Violation violation) throws SQLException {
        String sql = "INSERT INTO VIOLATION_RECORD (PARTICIPANT_ID, CATEGORY_ID, VIOLATION_TYPE, " +
                    "VIOLATION_DESCRIPTION, SESSION_SUMMARY, REINFORCEMENT, STATUS, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // First, check if participant exists
            if (!doesParticipantExist(violation.getParticipantId())) {
                throw new SQLException("Participant ID " + violation.getParticipantId() + " does not exist");
            }

            stmt.setInt(1, violation.getParticipantId());
            stmt.setInt(2, violation.getCategoryId());
            stmt.setString(3, violation.getViolationType());
            stmt.setString(4, violation.getViolationDescription());
            stmt.setString(5, violation.getSessionSummary());
            stmt.setString(6, violation.getReinforcement());
            stmt.setString(7, violation.getStatus());
            stmt.setTimestamp(8, violation.getUpdatedAt());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            throw new SQLException("Creating violation failed, no ID obtained.");
        }
    }

    // Add violation with individual parameters
    public boolean addViolation(int participantId, int categoryId, String violationType, String violationDescription,
                              String sessionSummary, String reinforcement, String status, Timestamp updatedAt) throws SQLException {
        Violation violation = new Violation(0, participantId, categoryId, violationType, violationDescription,
                                         sessionSummary, reinforcement, status, updatedAt);
        return createViolation(violation) > 0;
    }

    // Get violations by student ID (active only)
    public List<Violation> getViolationsByStudentId(int studentId) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE p.STUDENT_UID = ? AND v.STATUS = 'Active'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(mapResultSetToViolation(rs));
                }
            }
        }
        return violations;
    }

    // Get violation by LRN
    public Violation getViolationByLRN(String lrn) throws SQLException {
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "JOIN STUDENT s ON p.STUDENT_UID = s.STUDENT_UID " +
                    "WHERE s.STUDENT_LRN = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, lrn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToViolation(rs);
                }
            }
        }
        return null;
    }

    // Get violations by student UID
    public List<Violation> getViolationsByStudentUID(int studentUID) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE p.STUDENT_UID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(mapResultSetToViolation(rs));
                }
            }
        }
        return violations;
    }

    // Get all violations
    public List<Violation> getAllViolations() throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.*, p.*, s.* " +
            "FROM VIOLATION_RECORD v " +
            "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
            "LEFT JOIN STUDENT s ON p.STUDENT_UID = s.STUDENT_UID " +
            "ORDER BY v.UPDATED_AT DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Violation violation = new Violation(
                    rs.getInt("VIOLATION_ID"),
                    rs.getInt("PARTICIPANT_ID"),
                    rs.getInt("CATEGORY_ID"),
                    rs.getString("VIOLATION_TYPE"),
                    rs.getString("VIOLATION_DESCRIPTION"),
                    rs.getString("SESSION_SUMMARY"),
                    rs.getString("REINFORCEMENT"),
                    rs.getString("STATUS"),
                    rs.getTimestamp("UPDATED_AT")
                );
                // Load category
                ViolationCategory category = categoryDAO.getCategoryById(violation.getCategoryId());
                violation.setCategory(category);

                // --- FIX: Use DAOs to get full participant and student objects ---
                lyfjshs.gomis.Database.entity.Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
                if (participant != null && participant.getStudentUid() != null) {
                    lyfjshs.gomis.Database.entity.Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
                    participant.setStudent(student);
                }
                violation.setParticipant(participant);
                // ---------------------------------------------------------------

                violations.add(violation);
            }
        }
        return violations;
    }

    // Update violation
    public boolean updateViolation(Violation violation) throws SQLException {
        String sql = "UPDATE VIOLATION_RECORD SET PARTICIPANT_ID = ?, CATEGORY_ID = ?, VIOLATION_TYPE = ?, " +
                    "VIOLATION_DESCRIPTION = ?, SESSION_SUMMARY = ?, REINFORCEMENT = ?, STATUS = ?, " +
                    "UPDATED_AT = ? WHERE VIOLATION_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, violation.getParticipantId());
            stmt.setInt(2, violation.getCategoryId());
            stmt.setString(3, violation.getViolationType());
            stmt.setString(4, violation.getViolationDescription());
            stmt.setString(5, violation.getSessionSummary());
            stmt.setString(6, violation.getReinforcement());
            stmt.setString(7, violation.getStatus());
            stmt.setTimestamp(8, violation.getUpdatedAt());
            stmt.setInt(9, violation.getViolationId());
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to standardize status values
    private String standardizeStatus(String status) {
        if (status == null) return "ACTIVE";
        switch (status.toUpperCase()) {
            case "RESOLVED":
            case "RESOLVE":
                return "RESOLVED";
            case "ACTIVE":
            default:
                return "ACTIVE";
        }
    }

    // Update violation status with standardized values and resolution notes
    public boolean updateViolationStatus(int violationId, String status, String resolutionNotes) throws SQLException {
        String sql = "UPDATE VIOLATION_RECORD SET STATUS = ?, RESOLUTION_NOTES = ?, UPDATED_AT = CURRENT_TIMESTAMP WHERE VIOLATION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, standardizeStatus(status));
            stmt.setString(2, resolutionNotes);
            stmt.setInt(3, violationId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Delete violation
    public boolean deleteViolation(int violationId) throws SQLException {
        // First check if there are any related sessions
        String checkSessionsSql = "SELECT COUNT(*) FROM SESSIONS WHERE VIOLATION_ID = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSessionsSql)) {
            checkStmt.setInt(1, violationId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete violation: It has associated sessions");
                }
            }
        }

        // If no sessions exist, proceed with deletion
        String sql = "DELETE FROM VIOLATION_RECORD WHERE VIOLATION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, violationId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Get violations by participant name
    public List<Violation> getViolationsByParticipantName(String firstName, String lastName) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE p.PARTICIPANT_FIRSTNAME LIKE ? AND p.PARTICIPANT_LASTNAME LIKE ? " +
                    "ORDER BY v.UPDATED_AT DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + firstName + "%");
            stmt.setString(2, "%" + lastName + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(mapResultSetToViolation(rs));
                }
            }
        }
        return violations;
    }

    // Overloaded method for full name
    public List<Violation> getViolationsByParticipantName(String fullName) throws SQLException {
        String[] names = fullName.split(" ", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : "";
        return getViolationsByParticipantName(firstName, lastName);
    }

    // Get active violations that need follow-up
    public List<Violation> getActiveViolations() throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT * FROM VIOLATION_RECORD WHERE STATUS = 'Active' " +
                    "ORDER BY UPDATED_AT DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                violations.add(mapResultSetToViolation(rs));
            }
        }
        return violations;
    }

    // Helper method to check if participant exists
    private boolean doesParticipantExist(int participantId) throws SQLException {
        String sql = "SELECT 1 FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Helper method to map ResultSet to Violation object
    private Violation mapResultSetToViolation(ResultSet rs) throws SQLException {
        Violation violation = new Violation(
            rs.getInt("VIOLATION_ID"),
            rs.getInt("PARTICIPANT_ID"),
            rs.getInt("CATEGORY_ID"),
            rs.getString("VIOLATION_TYPE"),
            rs.getString("VIOLATION_DESCRIPTION"),
            rs.getString("SESSION_SUMMARY"),
            rs.getString("REINFORCEMENT"),
            rs.getString("STATUS"),
            rs.getTimestamp("UPDATED_AT")
        );
        violation.setResolutionNotes(rs.getString("RESOLUTION_NOTES"));
        
        // Load category
        ViolationCategory category = categoryDAO.getCategoryById(violation.getCategoryId());
        violation.setCategory(category);
        
        return violation;
    }

    public Violation getViolationById(int violationId) throws SQLException {
        String sql = "SELECT * FROM VIOLATION_RECORD WHERE VIOLATION_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, violationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Violation violation = new Violation(
                        rs.getInt("VIOLATION_ID"),
                        rs.getInt("PARTICIPANT_ID"),
                        rs.getInt("CATEGORY_ID"),
                        rs.getString("VIOLATION_TYPE"),
                        rs.getString("VIOLATION_DESCRIPTION"),
                        rs.getString("SESSION_SUMMARY"),
                        rs.getString("REINFORCEMENT"),
                        rs.getString("STATUS"),
                        rs.getTimestamp("UPDATED_AT")
                    );
                    violation.setResolutionNotes(rs.getString("RESOLUTION_NOTES"));
                    
                    // Load category
                    ViolationCategory category = categoryDAO.getCategoryById(violation.getCategoryId());
                    violation.setCategory(category);
                    
                    return violation;
                }
            }
        }

        return null;
    }

    public List<Violation> getViolationsByCategory(int categoryId) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT * FROM VIOLATION_RECORD WHERE CATEGORY_ID = ? ORDER BY UPDATED_AT DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Violation violation = new Violation(
                        rs.getInt("VIOLATION_ID"),
                        rs.getInt("PARTICIPANT_ID"),
                        rs.getInt("CATEGORY_ID"),
                        rs.getString("VIOLATION_TYPE"),
                        rs.getString("VIOLATION_DESCRIPTION"),
                        rs.getString("SESSION_SUMMARY"),
                        rs.getString("REINFORCEMENT"),
                        rs.getString("STATUS"),
                        rs.getTimestamp("UPDATED_AT")
                    );
                    
                    // Load category
                    ViolationCategory category = categoryDAO.getCategoryById(violation.getCategoryId());
                    violation.setCategory(category);
                    
                    violations.add(violation);
                }
            }
        }

        return violations;
    }

    public String getCategoryNameById(int categoryId) throws SQLException {
        String sql = "SELECT CATEGORY_NAME FROM VIOLATION_CATEGORIES WHERE CATEGORY_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("CATEGORY_NAME");
                }
            }
        }
        return null; // Return null if category not found
    }

    // Search violations
    public List<Violation> searchViolations(String searchTerm) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "WHERE v.VIOLATION_TYPE LIKE ? OR v.VIOLATION_DESCRIPTION LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(mapResultSetToViolation(rs));
                }
            }
        }
        return violations;
    }

    public List<Violation> getViolationsBySessionId(int sessionId) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        // Corrected SQL query to join VIOLATION_RECORD with SESSIONS
        String sql = "SELECT vr.* FROM VIOLATION_RECORD vr " +
                     "JOIN SESSIONS s ON vr.VIOLATION_ID = s.VIOLATION_ID " +
                     "WHERE s.SESSION_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    violations.add(mapResultSetToViolation(resultSet));
                }
            }
        }
        return violations;
    }

    public static TimeSeriesCollection buildViolationTimeSeries(List<Violation> violations) {
        Map<String, TimeSeries> seriesMap = new HashMap<>();
        for (Violation v : violations) {
            String type = v.getViolationType();
            TimeSeries series = seriesMap.computeIfAbsent(type, k -> new TimeSeries(k));
            Date date = new Date(v.getUpdatedAt().getTime());
            // Increment count for this date/type
            Number current = series.getValue(new Day(date));
            int newValue = (current == null ? 1 : current.intValue() + 1);
            series.addOrUpdate(new Day(date), newValue);
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for (TimeSeries ts : seriesMap.values()) {
            dataset.addSeries(ts);
        }
        return dataset;
    }

    public DefaultCategoryDataset getViolationTimeSeriesDataset() {
        return getViolationTimeSeriesDataset(null);
    }

    public DefaultCategoryDataset getViolationTimeSeriesDataset(String category) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            String sql = "SELECT DATE(VR.UPDATED_AT) as violation_date, VR.VIOLATION_TYPE, COUNT(*) as count " +
                        "FROM VIOLATION_RECORD VR " +
                        (category != null && !category.equals("All Categories") ? "WHERE VR.VIOLATION_TYPE = ? " : "") +
                        "GROUP BY DATE(VR.UPDATED_AT), VR.VIOLATION_TYPE " +
                        "ORDER BY violation_date";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            if (category != null && !category.equals("All Categories")) {
                pstmt.setString(1, category);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String date = rs.getDate("violation_date").toString();
                String type = rs.getString("VIOLATION_TYPE");
                int count = rs.getInt("count");
                dataset.addValue(count, type, date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataset;
    }

    public DefaultPieDataset<String> getViolationPieDataset() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        try {
            String sql = "SELECT VIOLATION_TYPE, COUNT(*) as count " +
                        "FROM VIOLATION_RECORD " +
                        "GROUP BY VIOLATION_TYPE";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String type = rs.getString("VIOLATION_TYPE");
                int count = rs.getInt("count");
                dataset.setValue(type, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataset;
    }

    public Object[][] getViolationTableData() {
        List<Object[]> data = new ArrayList<>();
        
        try {
            String sql = "SELECT VR.VIOLATION_ID, S.STUDENT_LRN, " +
                        "CONCAT(S.STUDENT_FIRSTNAME, ' ', S.STUDENT_LASTNAME) as student_name, " +
                        "VR.VIOLATION_TYPE, VR.STATUS, VR.UPDATED_AT " +
                        "FROM VIOLATION_RECORD VR " +
                        "JOIN PARTICIPANTS P ON VR.PARTICIPANT_ID = P.PARTICIPANT_ID " +
                        "JOIN STUDENT S ON P.STUDENT_UID = S.STUDENT_UID " +
                        "ORDER BY VR.UPDATED_AT DESC";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("STUDENT_LRN"),
                    rs.getString("student_name"),
                    rs.getString("VIOLATION_TYPE"),
                    rs.getString("STATUS"),
                    rs.getTimestamp("UPDATED_AT")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data.toArray(new Object[0][]);
    }

    public String[] getViolationTableColumnNames() {
        return new String[] {
            "LRN",
            "Student Name",
            "Violation Type",
            "Status",
            "Date"
        };
    }

    public TimeSeriesCollection getViolationTimeSeriesCollection(String category) {
        List<Violation> violations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM VIOLATION_RECORD";
            if (category != null && !category.equals("All Categories")) {
                sql += " WHERE VIOLATION_TYPE = ?";
            }
            PreparedStatement pstmt = connection.prepareStatement(sql);
            if (category != null && !category.equals("All Categories")) {
                pstmt.setString(1, category);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                violations.add(mapResultSetToViolation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buildViolationTimeSeries(violations);
    }

    public Map<String, Integer> getViolationCountsByType(Date startDate, Date endDate, String category) throws SQLException {
        Map<String, Integer> violationCounts = new TreeMap<>();
        
        StringBuilder sql = new StringBuilder("SELECT v.VIOLATION_TYPE, COUNT(*) as count " +
                "FROM VIOLATION_RECORD v " +
                "WHERE 1=1 ");

        if (startDate != null) {
            sql.append("AND v.UPDATED_AT >= ? ");
        }
        if (endDate != null) {
            sql.append("AND v.UPDATED_AT <= ? ");
        }
        if (category != null) {
            sql.append("AND v.CATEGORY_ID IN (SELECT CATEGORY_ID FROM VIOLATION_CATEGORIES WHERE CATEGORY_NAME = ?) ");
        }
        
        sql.append("GROUP BY v.VIOLATION_TYPE ORDER BY count DESC");
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (startDate != null) {
                pstmt.setTimestamp(paramIndex++, new java.sql.Timestamp(startDate.getTime()));
            }
            if (endDate != null) {
                pstmt.setTimestamp(paramIndex++, new java.sql.Timestamp(endDate.getTime()));
            }
            if (category != null) {
                pstmt.setString(paramIndex, category);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String violationType = rs.getString("VIOLATION_TYPE");
                    int count = rs.getInt("count");
                    violationCounts.put(violationType, count);
                }
            }
        }
        
        return violationCounts;
    }

    public List<Object[]> getViolationReportData(Date startDate, Date endDate, String violationType, String groupBy, String searchTerm) throws SQLException {
        List<Object[]> data = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT " +
            "    CONCAT(p.PARTICIPANT_FIRSTNAME, ' ', p.PARTICIPANT_LASTNAME) as participant_name, " +
            "    c.CATEGORY_NAME, " +
            "    v.VIOLATION_TYPE, " +
            "    v.VIOLATION_DESCRIPTION, " +
            "    v.STATUS, " +
            "    v.UPDATED_AT " +
            "FROM VIOLATION_RECORD v " +
            "JOIN VIOLATION_CATEGORIES c ON v.CATEGORY_ID = c.CATEGORY_ID " +
            "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
            "WHERE 1=1 ");

        if (startDate != null) {
            query.append("AND v.UPDATED_AT >= ? ");
        }
        if (endDate != null) {
            query.append("AND v.UPDATED_AT <= ? ");
        }
        if (violationType != null && !violationType.equals("All")) {
            query.append("AND v.VIOLATION_TYPE = ? ");
        }
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            query.append("AND (p.PARTICIPANT_FIRSTNAME LIKE ? OR p.PARTICIPANT_LASTNAME LIKE ? OR v.VIOLATION_TYPE LIKE ?) ");
        }

        query.append("ORDER BY v.UPDATED_AT DESC");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int paramIndex = 1;

            if (startDate != null) {
                stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(startDate.getTime()));
            }
            if (endDate != null) {
                stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(endDate.getTime()));
            }
            if (violationType != null && !violationType.equals("All")) {
                stmt.setString(paramIndex++, violationType);
            }
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String likeTerm = "%" + searchTerm + "%";
                stmt.setString(paramIndex++, likeTerm);
                stmt.setString(paramIndex++, likeTerm);
                stmt.setString(paramIndex++, likeTerm);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    data.add(new Object[]{
                        rs.getString("participant_name"),
                        rs.getString("CATEGORY_NAME"),
                        rs.getString("VIOLATION_TYPE"),
                        rs.getString("VIOLATION_DESCRIPTION"),
                        rs.getString("STATUS"),
                        rs.getTimestamp("UPDATED_AT")
                    });
                }
            }
        }
        return data;
    }

    public Map<String, Map<String, Integer>> getViolationCountsByTypeAndCategory(Date startDate, Date endDate, String violationTypeFilter) throws SQLException {
        Map<String, Map<String, Integer>> result = new TreeMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT v.VIOLATION_TYPE, c.CATEGORY_NAME, COUNT(*) as count " +
                "FROM VIOLATION_RECORD v " +
                "JOIN VIOLATION_CATEGORIES c ON v.CATEGORY_ID = c.CATEGORY_ID " +
                "WHERE 1=1 ");

        if (startDate != null) {
            sql.append("AND v.UPDATED_AT >= ? ");
        }
        if (endDate != null) {
            sql.append("AND v.UPDATED_AT <= ? ");
        }
        if (violationTypeFilter != null) {
            sql.append("AND v.VIOLATION_TYPE = ? ");
        }

        sql.append("GROUP BY v.VIOLATION_TYPE, c.CATEGORY_NAME ORDER BY v.VIOLATION_TYPE, c.CATEGORY_NAME");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (startDate != null) {
                pstmt.setTimestamp(paramIndex++, new java.sql.Timestamp(startDate.getTime()));
            }
            if (endDate != null) {
                pstmt.setTimestamp(paramIndex++, new java.sql.Timestamp(endDate.getTime()));
            }
            if (violationTypeFilter != null) {
                pstmt.setString(paramIndex, violationTypeFilter);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String violationType = rs.getString("VIOLATION_TYPE");
                    String category = rs.getString("CATEGORY_NAME");
                    int count = rs.getInt("count");
                    result.computeIfAbsent(violationType, k -> new TreeMap<>()).put(category, count);
                }
            }
        }
        return result;
    }

    public Map<String, Map<String, Integer>> getViolationCountsByMonthAndCategory(Date startDate, Date endDate,
            String violationType) throws SQLException {
        Map<String, Map<String, Integer>> result = new TreeMap<>();
        String sql = "SELECT DATE_FORMAT(v.UPDATED_AT, '%Y-%m') as month, c.CATEGORY_NAME, COUNT(*) as count "
                + "FROM VIOLATION_RECORD v " + "JOIN VIOLATION_CATEGORIES c ON v.CATEGORY_ID = c.CATEGORY_ID "
                + "WHERE 1=1 ";

        if (startDate != null) {
            sql += "AND v.UPDATED_AT >= ? ";
        }
        if (endDate != null) {
            sql += "AND v.UPDATED_AT <= ? ";
        }
        if (violationType != null) {
            sql += "AND v.VIOLATION_TYPE = ? ";
        }

        sql += "GROUP BY month, c.CATEGORY_NAME ORDER BY month, c.CATEGORY_NAME";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            if (startDate != null) {
                pstmt.setTimestamp(paramIndex++, new java.sql.Timestamp(startDate.getTime()));
            }
            if (endDate != null) {
                pstmt.setTimestamp(paramIndex++, new java.sql.Timestamp(endDate.getTime()));
            }
            if (violationType != null) {
                pstmt.setString(paramIndex++, violationType);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String month = rs.getString("month");
                    String category = rs.getString("CATEGORY_NAME");
                    int count = rs.getInt("count");
                    result.computeIfAbsent(month, k -> new TreeMap<>()).put(category, count);
                }
            }
        }
        return result;
    }
}