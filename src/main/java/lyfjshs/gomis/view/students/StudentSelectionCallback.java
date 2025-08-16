package lyfjshs.gomis.view.students;

import lyfjshs.gomis.Database.entity.Student;

public interface StudentSelectionCallback {
    void onStudentSelected(Student student);
}