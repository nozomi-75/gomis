package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import lyfjshs.gomis.Database.DAO.RemarksDAO;
import lyfjshs.gomis.Database.entity.Remarks;

public class RemarksDB_Test {
    private static final String URL = "jdbc:mariadb://localhost:3306/gomisdb";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            RemarksDAO remarksDAO = new RemarksDAO(connection);

            // CREATE: Insert a new remark
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            boolean insertSuccess = remarksDAO.insertRemark("STU12345", "This is a test remark", timestamp);
            System.out.println("Insert Successful: " + insertSuccess);

            // READ: Retrieve remark by ID (assuming last inserted ID)
            Remarks remark = remarksDAO.getRemarkById(1);
            if (remark != null) {
                System.out.println("Retrieved Remark: " + remark);
            } else {
                System.out.println("Remark not found.");
            }

            // READ ALL: Retrieve all remarks
            List<Remarks> remarksList = remarksDAO.getAllRemarks();
            System.out.println("All Remarks:");
            for (Remarks r : remarksList) {
                System.out.println(r);
            }

            // UPDATE: Modify an existing remark (assuming ID = 1)
            boolean updateSuccess = remarksDAO.updateRemark(1, "Updated test remark", new Timestamp(System.currentTimeMillis()));
            System.out.println("Update Successful: " + updateSuccess);

            // DELETE: Remove a remark (assuming ID = 1)
            boolean deleteSuccess = remarksDAO.deleteRemark(1);
            System.out.println("Delete Successful: " + deleteSuccess);

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
}
