-- SQLBook: Code

-- Insert sample data into GUIDANCE_COUNSELORS table
INSERT INTO GUIDANCE_COUNSELORS (GUIDANCE_COUNSELOR_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME, SUFFIX, GENDER, SPECIALIZATION, CONTACT_NUM, EMAIL, POSITION, PROFILE_PICTURE)
VALUES
(2, 'Smith', 'Alice', 'B', 'Jr', 'Female', 'Counseling', 987654321, 'alice.smith@example.com', 'Counselor', NULL)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);


-- Insert sample data into USERS table
INSERT INTO USERS (USER_ID, U_NAME, U_PASS, GUIDANCE_COUNSELOR_ID)
VALUES
(2, 'admin2', 'admin2', 2)
ON DUPLICATE KEY UPDATE U_NAME = VALUES(U_NAME);


-- Insert sample data into CONTACT table
INSERT INTO CONTACT (CONTACT_ID, CONTACT_NUMBER)
VALUES
(1, '1234567890'),
(2, '2345678901'),
(3, '3456789012'),
(4, '4567890123'),
(5, '5678901234'),
(6, '6789012345'),
(7, '7890123456'),
(8, '8901234567'),
(9, '9012345678'),
(10, '0123456789')
ON DUPLICATE KEY UPDATE CONTACT_NUMBER = VALUES(CONTACT_NUMBER);

-- Insert sample data into ADDRESS table
INSERT INTO ADDRESS (ADDRESS_ID, ADDRESS_HOUSE_NUMBER, ADDRESS_STREET_SUBDIVISION, ADDRESS_REGION, ADDRESS_PROVINCE, ADDRESS_MUNICIPALITY, ADDRESS_BARANGAY, ADDRESS_ZIP_CODE)
VALUES
(1, '123', 'Main St', 'Region 1', 'Province 1', 'City 1', 'Barangay 1', '1234'),
(2, '234', 'Elm St', 'Region 2', 'Province 2', 'City 2', 'Barangay 2', '2345'),
(3, '345', 'Oak St', 'Region 3', 'Province 3', 'City 3', 'Barangay 3', '3456'),
(4, '456', 'Pine St', 'Region 4', 'Province 4', 'City 4', 'Barangay 4', '4567'),
(5, '567', 'Cedar St', 'Region 5', 'Province 5', 'City 5', 'Barangay 5', '5678'),
(6, '678', 'Maple St', 'Region 6', 'Province 6', 'City 6', 'Barangay 6', '6789'),
(7, '789', 'Birch St', 'Region 7', 'Province 7', 'City 7', 'Barangay 7', '7890'),
(8, '890', 'Walnut St', 'Region 8', 'Province 8', 'City 8', 'Barangay 8', '8901'),
(9, '901', 'Cherry St', 'Region 9', 'Province 9', 'City 9', 'Barangay 9', '9012'),
(10, '012', 'Ash St', 'Region 10', 'Province 10', 'City 10', 'Barangay 10', '0123')
ON DUPLICATE KEY UPDATE ADDRESS_HOUSE_NUMBER = VALUES(ADDRESS_HOUSE_NUMBER);


-- Insert sample data into GUARDIAN table
INSERT INTO GUARDIAN (GUARDIAN_ID, GUARDIAN_LASTNAME, GUARDIAN_FIRST_NAME, GUARDIAN_MIDDLE_NAME, GUARDIAN_RELATIONSHIP, GUARDIAN_CONTACT_NUMBER)
VALUES
(1, 'Doe', 'John', 'A', 'Uncle', '1234567890'),
(2, 'Smith', 'Alice', 'B', 'Aunt', '2345678901'),
(3, 'Johnson', 'Bob', 'C', 'Uncle', '3456789012'),
(4, 'Williams', 'Charlie', 'D', 'Aunt', '4567890123'),
(5, 'Brown', 'Daisy', 'E', 'Uncle', '5678901234'),
(6, 'Jones', 'Eve', 'F', 'Aunt', '6789012345'),
(7, 'Garcia', 'Frank', 'G', 'Uncle', '7890123456'),
(8, 'Miller', 'Grace', 'H', 'Aunt', '8901234567'),
(9, 'Davis', 'Heidi', 'I', 'Uncle', '9012345678'),
(10, 'Rodriguez', 'Ivy', 'J', 'Aunt', '0123456789')
ON DUPLICATE KEY UPDATE GUARDIAN_LASTNAME = VALUES(GUARDIAN_LASTNAME);

-- Insert sample data into PARENTS table
INSERT INTO PARENTS (PARENT_ID, FATHER_FIRSTNAME, FATHER_LASTNAME, FATHER_MIDDLENAME, FATHER_CONTACT_NUMBER, MOTHER_FIRSTNAME, MOTHER_LASTNAME, MOTHER_MIDDLE_NAME, MOTHER_CONTACT_NUMBER)
VALUES
(1, 'John', 'Doe', 'A', '1234567890', 'Jane', 'Doe', 'B', '0987654321'),
(2, 'Alice', 'Smith', 'B', '2345678901', 'Bob', 'Smith', 'C', '3456789012'),
(3, 'Charlie', 'Johnson', 'D', '4567890123', 'Daisy', 'Johnson', 'E', '5678901234'),
(4, 'Eve', 'Williams', 'F', '6789012345', 'Frank', 'Williams', 'G', '7890123456'),
(5, 'Grace', 'Brown', 'H', '8901234567', 'Heidi', 'Brown', 'I', '9012345678'),
(6, 'Ivy', 'Jones', 'J', '0123456789', 'Jack', 'Jones', 'K', '1234567890'),
(7, 'Kate', 'Garcia', 'L', '2345678901', 'Leo', 'Garcia', 'M', '3456789012'),
(8, 'Mia', 'Miller', 'N', '4567890123', 'Nina', 'Miller', 'O', '5678901234'),
(9, 'Oscar', 'Davis', 'P', '6789012345', 'Peter', 'Davis', 'Q', '7890123456'),
(10, 'Quinn', 'Rodriguez', 'R', '8901234567', 'Rachel', 'Rodriguez', 'S', '9012345678')
ON DUPLICATE KEY UPDATE FATHER_FIRSTNAME = VALUES(FATHER_FIRSTNAME);


-- Insert sample data into SCHOOL_FORM table
INSERT INTO SCHOOL_FORM (SF_ID, SF_SCHOOL_NAME, SF_SCHOOL_ID, SF_DISTRICT, SF_DIVISION, SF_REGION, SF_SEMESTER, SF_SCHOOL_YEAR, SF_GRADE_LEVEL, SF_SECTION, SF_TRACK_AND_STRAND, SF_COURSE)
VALUES
(1, 'School A', 'SCH001', 'District 1', 'Division 1', 'Region 1', 'First', '2023-2024', 'Grade 10', 'Section A', 'Track 1', 'Course 1'),
(2, 'School B', 'SCH002', 'District 2', 'Division 2', 'Region 2', 'First', '2023-2024', 'Grade 10', 'Section B', 'Track 2', 'Course 2'),
(3, 'School C', 'SCH003', 'District 3', 'Division 3', 'Region 3', 'First', '2023-2024', 'Grade 10', 'Section C', 'Track 3', 'Course 3'),
(4, 'School D', 'SCH004', 'District 4', 'Division 4', 'Region 4', 'First', '2023-2024', 'Grade 10', 'Section D', 'Track 4', 'Course 4'),
(5, 'School E', 'SCH005', 'District 5', 'Division 5', 'Region 5', 'First', '2023-2024', 'Grade 10', 'Section E', 'Track 5', 'Course 5'),
(6, 'School F', 'SCH006', 'District 6', 'Division 6', 'Region 6', 'First', '2023-2024', 'Grade 10', 'Section F', 'Track 6', 'Course 6'),
(7, 'School G', 'SCH007', 'District 7', 'Division 7', 'Region 7', 'First', '2023-2024', 'Grade 10', 'Section G', 'Track 7', 'Course 7'),
(8, 'School H', 'SCH008', 'District 8', 'Division 8', 'Region 8', 'First', '2023-2024', 'Grade 10', 'Section H', 'Track 8', 'Course 8'),
(9, 'School I', 'SCH009', 'District 9', 'Division 9', 'Region 9', 'First', '2023-2024', 'Grade 10', 'Section I', 'Track 9', 'Course 9'),
(10, 'School J', 'SCH010', 'District 10', 'Division 10', 'Region 10', 'First', '2023-2024', 'Grade 10', 'Section J', 'Track 10', 'Course 10')
ON DUPLICATE KEY UPDATE SF_SCHOOL_NAME = VALUES(SF_SCHOOL_NAME);


-- Insert sample data into STUDENT table (updated to allow multiple students per SF_ID)
INSERT INTO STUDENT (STUDENT_UID, PARENT_ID, GUARDIAN_ID, ADDRESS_ID, CONTACT_ID, SF_ID, STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, STUDENT_SEX, STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION)
VALUES
(1, 1, 1, 1, 1, 1, 'LRN123', 'Doe', 'Jane', 'C', 'Female', '2005-01-01', 'English', 18, 'Type A', 'Christian'), -- SF_ID = 1
(2, 2, 2, 2, 2, 1, 'LRN234', 'Smith', 'Alice', 'B', 'Female', '2006-02-02', 'English', 17, 'Type B', 'Christian'), -- SF_ID = 1
(3, 3, 3, 3, 3, 1, 'LRN345', 'Johnson', 'Bob', 'C', 'Male', '2007-03-03', 'English', 16, 'Type C', 'Christian'), -- SF_ID = 1
(4, 4, 4, 4, 4, 2, 'LRN456', 'Williams', 'Charlie', 'D', 'Female', '2008-04-04', 'English', 15, 'Type D', 'Christian'), -- SF_ID = 2
(5, 5, 5, 5, 5, 2, 'LRN567', 'Brown', 'Daisy', 'E', 'Female', '2009-05-05', 'English', 14, 'Type E', 'Christian'), -- SF_ID = 2
(6, 6, 6, 6, 6, 3, 'LRN678', 'Jones', 'Eve', 'F', 'Male', '2010-06-06', 'English', 13, 'Type F', 'Christian'), -- SF_ID = 3
(7, 7, 7, 7, 7, 3, 'LRN789', 'Garcia', 'Frank', 'G', 'Male', '2011-07-07', 'English', 12, 'Type G', 'Christian'), -- SF_ID = 3
(8, 8, 8, 8, 8, 4, 'LRN890', 'Miller', 'Grace', 'H', 'Female', '2012-08-08', 'English', 11, 'Type H', 'Christian'), -- SF_ID = 4
(9, 9, 9, 9, 9, 4, 'LRN901', 'Davis', 'Heidi', 'I', 'Female', '2013-09-09', 'English', 10, 'Type I', 'Christian'), -- SF_ID = 4
(10, 10, 10, 10, 10, 5, 'LRN012', 'Rodriguez', 'Ivy', 'J', 'Male', '2014-10-10', 'English', 9, 'Type J', 'Christian') -- SF_ID = 5
ON DUPLICATE KEY UPDATE STUDENT_LASTNAME = VALUES(STUDENT_LASTNAME);

SELECT SF_ID, COUNT(STUDENT_UID) AS student_count
FROM STUDENT
GROUP BY SF_ID;

-- Insert Student Participant
INSERT INTO PARTICIPANTS (PARTICIPANT_ID, STUDENT_UID, PARTICIPANT_TYPE, PARTICIPANT_LASTNAME, PARTICIPANT_FIRSTNAME, PARTICIPANT_SEX, CONTACT_NUMBER)
VALUES 
(1, 1, 'Student', 'Doe', 'Jane', 'DoeJane@student.edu', '09123456789');

-- Insert Non-Student Participants
INSERT INTO PARTICIPANTS (PARTICIPANT_ID, STUDENT_UID, PARTICIPANT_TYPE, PARTICIPANT_LASTNAME, PARTICIPANT_FIRSTNAME, PARTICIPANT_SEX, CONTACT_NUMBER)
VALUES 
(2, NULL, 'Non-Student', 'Reyes', 'Maria', 'maria.reyes@example.com', '09234567890'),
(3, NULL, 'Non-Student', 'Santos', 'Carlos', 'carlos.santos@example.com', '09345678901'),
(4, 1, 'Student', 'Rivera', 'Mark', 'mark.rivera@student.edu', '09456789012'),
(5, NULL, 'Non-Student', 'Lopez', 'Ana', 'ana.lopez@example.com', '09567890123'),
(6, NULL, 'Non-Student', 'Fernandez', 'Luis', 'luis.fernandez@example.com', '09678901234');

-- Insert Appointments
INSERT INTO APPOINTMENTS (APPOINTMENT_ID, GUIDANCE_COUNSELOR_ID, APPOINTMENT_TITLE, CONSULTATION_TYPE, APPOINTMENT_DATE_TIME, APPOINTMENT_STATUS, APPOINTMENT_NOTES, UPDATED_AT)
VALUES 
(1, 1, 'Career Counseling', 'Career Guidance', '2025-03-21 10:00:00', 'Scheduled', 'Discussing career options.', NOW()),
(2, 1, 'Parental Consultation', 'Behavioral Consultation', '2025-03-22 14:00:00', 'Scheduled', 'Consultation with parents regarding student behavior.', NOW()),
(3, 1, 'Group Guidance', 'Personal Consultation', '2025-03-23 09:30:00', 'Scheduled', 'Guidance session for a group of students.', NOW());

-- Appointment 1: 1 Student Participant
INSERT INTO APPOINTMENT_PARTICIPANTS (APPOINTMENT_ID, PARTICIPANT_ID)
VALUES 
(1, 1);  -- Juan Garcia (Student)

-- Appointment 2: 2 Non-Student Participants
INSERT INTO APPOINTMENT_PARTICIPANTS (APPOINTMENT_ID, PARTICIPANT_ID)
VALUES 
(2, 2),  -- Maria Reyes (Non-Student)
(2, 3);  -- Carlos Santos (Non-Student)

-- Appointment 3: 3 Participants (1 Student, 2 Non-Students)
INSERT INTO APPOINTMENT_PARTICIPANTS (APPOINTMENT_ID, PARTICIPANT_ID)
VALUES 
(3, 4),  -- Mark Rivera (Student)
(3, 5),  -- Ana Lopez (Non-Student)
(3, 6);  -- Luis Fernandez (Non-Student)

SELECT a.APPOINTMENT_ID, a.APPOINTMENT_TITLE, p.PARTICIPANT_FIRSTNAME, p.PARTICIPANT_LASTNAME, p.PARTICIPANT_TYPE 
FROM APPOINTMENTS a
JOIN APPOINTMENT_PARTICIPANTS ap ON a.APPOINTMENT_ID = ap.APPOINTMENT_ID
JOIN PARTICIPANTS p ON ap.PARTICIPANT_ID = p.PARTICIPANT_ID
ORDER BY a.APPOINTMENT_ID;
