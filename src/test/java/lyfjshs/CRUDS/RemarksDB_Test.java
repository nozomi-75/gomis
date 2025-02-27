package lyfjshs.CRUDS;

import java.sql.Timestamp;
import java.util.List;

import lyfjshs.gomis.Database.DAO.RemarksDAO;
import lyfjshs.gomis.Database.model.Remarks;

public class RemarksDB_Test {
    public static void main(String[] args) {
        RemarksDAO remarksDAO = new RemarksDAO();

        // Insert a new remark
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        boolean insertSuccess = remarksDAO.insertRemark("STU12345", "This is a test remark.", currentTime);
        System.out.println("Insert Success: " + insertSuccess);

        // Retrieve all remarks
        List<Remarks> remarksList = remarksDAO.getAllRemarks();
        System.out.println("All Remarks:");
        for (Remarks remark : remarksList) {
            System.out.println(remark);
        }

        // Get a specific remark by ID (assuming at least one exists)
        if (!remarksList.isEmpty()) {
            int remarkId = remarksList.get(0).getRemarkId();
            Remarks retrievedRemark = remarksDAO.getRemarkById(remarkId);
            System.out.println("Retrieved Remark: " + retrievedRemark);

            // Update the remark
            boolean updateSuccess = remarksDAO.updateRemark(remarkId, "Updated test remark.", new Timestamp(System.currentTimeMillis()));
            System.out.println("Update Success: " + updateSuccess);

            // Retrieve the updated remark
            Remarks updatedRemark = remarksDAO.getRemarkById(remarkId);
            System.out.println("Updated Remark: " + updatedRemark);

            // Delete the remark
            boolean deleteSuccess = remarksDAO.deleteRemark(remarkId);
            System.out.println("Delete Success: " + deleteSuccess);
        }
    }
}
