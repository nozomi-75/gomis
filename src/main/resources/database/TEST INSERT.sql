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
(10, '0123456789'),
(11, '1234567890'),
(12, '2345678901'),
(13, '3456789012'),
(14, '4567890123'),
(15, '5678901234'),
(16, '6789012345'),
(17, '7890123456'),
(18, '8901234567'),
(19, '9012345678'),
(20, '0123456789'),
(21, '1234567890')
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
(10, '012', 'Ash St', 'Region 10', 'Province 10', 'City 10', 'Barangay 10', '0123'),
(11, '123', 'Hickory St', 'Region 11', 'Province 11', 'City 11', 'Barangay 11', '1234'),
(12, '234', 'Spruce St', 'Region 12', 'Province 12', 'City 12', 'Barangay 12', '2345'),
(13, '345', 'Sycamore St', 'Region 13', 'Province 13', 'City 13', 'Barangay 13', '3456'),
(14, '456', 'Poplar St', 'Region 14', 'Province 14', 'City 14', 'Barangay 14', '4567'),
(15, '567', 'Hemlock St', 'Region 15', 'Province 15', 'City 15', 'Barangay 15', '5678'),
(16, '678', 'Magnolia St', 'Region 16', 'Province 16', 'City 16', 'Barangay 16', '6789'),
(17, '789', 'Acacia St', 'Region 17', 'Province 17', 'City 17', 'Barangay 17', '7890'),
(18, '890', 'Dogwood St', 'Region 18', 'Province 18', 'City 18', 'Barangay 18', '8901'),
(19, '901', 'Redwood St', 'Region 19', 'Province 19', 'City 19', 'Barangay 19', '9012'),
(20, '012', 'Aspen St', 'Region 20', 'Province 20', 'City 20', 'Barangay 20', '0123'),
(21, '123', 'Beech St', 'Region 21', 'Province 21', 'City 21', 'Barangay 21', '1234')
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
(10, 'Rodriguez', 'Ivy', 'J', 'Aunt', '0123456789'),
(11, 'Martinez', 'Jack', 'K', 'Uncle', '1234567890'),
(12, 'Hernandez', 'Kate', 'L', 'Aunt', '2345678901'),
(13, 'Lopez', 'Leo', 'M', 'Uncle', '3456789012'),
(14, 'Gonzalez', 'Mia', 'N', 'Aunt', '4567890123'),
(15, 'Wilson', 'Nina', 'O', 'Uncle', '5678901234'),
(16, 'Anderson', 'Oscar', 'P', 'Aunt', '6789012345'),
(17, 'Thomas', 'Peter', 'Q', 'Uncle', '7890123456'),
(18, 'Moore', 'Quinn', 'R', 'Aunt', '8901234567'),
(19, 'Taylor', 'Rachel', 'S', 'Uncle', '9012345678'),
(20, 'Martin', 'Sam', 'T', 'Aunt', '0123456789'),
(21, 'Lee', 'Tina', 'U', 'Uncle', '1234567890')
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
(10, 'Quinn', 'Rodriguez', 'R', '8901234567', 'Rachel', 'Rodriguez', 'S', '9012345678'),
(11, 'Sam', 'Martinez', 'T', '0123456789', 'Tina', 'Martinez', 'U', '1234567890'),
(12, 'Uma', 'Hernandez', 'V', '2345678901', 'Vera', 'Hernandez', 'W', '3456789012'),
(13, 'Wade', 'Lopez', 'X', '4567890123', 'Xena', 'Lopez', 'Y', '5678901234'),
(14, 'Yara', 'Gonzalez', 'Z', '6789012345', 'Zane', 'Gonzalez', 'AA', '7890123456'),
(15, 'Aaron', 'Wilson', 'BB', '8901234567', 'Ava', 'Wilson', 'CC', '9012345678'),
(16, 'Bella', 'Anderson', 'DD', '0123456789', 'Ben', 'Anderson', 'EE', '1234567890'),
(17, 'Cara', 'Thomas', 'FF', '2345678901', 'Chris', 'Thomas', 'GG', '3456789012'),
(18, 'Dana', 'Moore', 'HH', '4567890123', 'David', 'Moore', 'II', '5678901234'),
(19, 'Ella', 'Taylor', 'JJ', '6789012345', 'Ethan', 'Taylor', 'KK', '7890123456'),
(20, 'Finn', 'Martin', 'LL', '8901234567', 'Fiona', 'Martin', 'MM', '9012345678'),
(21, 'Gina', 'Lee', 'NN', '0123456789', 'George', 'Lee', 'OO', '1234567890')
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
(10, 'School J', 'SCH010', 'District 10', 'Division 10', 'Region 10', 'First', '2023-2024', 'Grade 10', 'Section J', 'Track 10', 'Course 10'),
(11, 'School K', 'SCH011', 'District 11', 'Division 11', 'Region 11', 'First', '2023-2024', 'Grade 10', 'Section K', 'Track 11', 'Course 11'),
(12, 'School L', 'SCH012', 'District 12', 'Division 12', 'Region 12', 'First', '2023-2024', 'Grade 10', 'Section L', 'Track 12', 'Course 12'),
(13, 'School M', 'SCH013', 'District 13', 'Division 13', 'Region 13', 'First', '2023-2024', 'Grade 10', 'Section M', 'Track 13', 'Course 13'),
(14, 'School N', 'SCH014', 'District 14', 'Division 14', 'Region 14', 'First', '2023-2024', 'Grade 10', 'Section N', 'Track 14', 'Course 14'),
(15, 'School O', 'SCH015', 'District 15', 'Division 15', 'Region 15', 'First', '2023-2024', 'Grade 10', 'Section O', 'Track 15', 'Course 15'),
(16, 'School P', 'SCH016', 'District 16', 'Division 16', 'Region 16', 'First', '2023-2024', 'Grade 10', 'Section P', 'Track 16', 'Course 16'),
(17, 'School Q', 'SCH017', 'District 17', 'Division 17', 'Region 17', 'First', '2023-2024', 'Grade 10', 'Section Q', 'Track 17', 'Course 17'),
(18, 'School R', 'SCH018', 'District 18', 'Division 18', 'Region 18', 'First', '2023-2024', 'Grade 10', 'Section R', 'Track 18', 'Course 18'),
(19, 'School S', 'SCH019', 'District 19', 'Division 19', 'Region 19', 'First', '2023-2024', 'Grade 10', 'Section S', 'Track 19', 'Course 19'),
(20, 'School T', 'SCH020', 'District 20', 'Division 20', 'Region 20', 'First', '2023-2024', 'Grade 10', 'Section T', 'Track 20', 'Course 20'),
(21, 'School U', 'SCH021', 'District 21', 'Division 21', 'Region 21', 'First', '2023-2024', 'Grade 10', 'Section U', 'Track 21', 'Course 21')
ON DUPLICATE KEY UPDATE SF_SCHOOL_NAME = VALUES(SF_SCHOOL_NAME);


-- Insert sample data into STUDENT table
INSERT INTO STUDENT (STUDENT_UID, PARENT_ID, GUARDIAN_ID, ADDRESS_ID, CONTACT_ID, SF_SECTION, STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, STUDENT_SEX, STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION)
VALUES
(1, 1, 1, 1, 1, 'Section A', 'LRN123', 'Doe', 'Jane', 'C', 'Female', '2005-01-01', 'English', 18, 'Type A', 'Christian'),
(2, 2, 2, 2, 2, 'Section B', 'LRN234', 'Smith', 'Alice', 'B', 'Female', '2006-02-02', 'English', 17, 'Type B', 'Christian'),
(3, 3, 3, 3, 3, 'Section C', 'LRN345', 'Johnson', 'Bob', 'C', 'Male', '2007-03-03', 'English', 16, 'Type C', 'Christian'),
(4, 4, 4, 4, 4, 'Section D', 'LRN456', 'Williams', 'Charlie', 'D', 'Female', '2008-04-04', 'English', 15, 'Type D', 'Christian'),
(5, 5, 5, 5, 5, 'Section E', 'LRN567', 'Brown', 'Daisy', 'E', 'Female', '2009-05-05', 'English', 14, 'Type E', 'Christian'),
(6, 6, 6, 6, 6, 'Section F', 'LRN678', 'Jones', 'Eve', 'F', 'Male', '2010-06-06', 'English', 13, 'Type F', 'Christian'),
(7, 7, 7, 7, 7, 'Section G', 'LRN789', 'Garcia', 'Frank', 'G', 'Male', '2011-07-07', 'English', 12, 'Type G', 'Christian'),
(8, 8, 8, 8, 8, 'Section H', 'LRN890', 'Miller', 'Grace', 'H', 'Female', '2012-08-08', 'English', 11, 'Type H', 'Christian'),
(9, 9, 9, 9, 9, 'Section I', 'LRN901', 'Davis', 'Heidi', 'I', 'Female', '2013-09-09', 'English', 10, 'Type I', 'Christian'),
(10, 10, 10, 10, 10, 'Section J', 'LRN012', 'Rodriguez', 'Ivy', 'J', 'Male', '2014-10-10', 'English', 9, 'Type J', 'Christian'),
(11, 11, 11, 11, 11, 'Section K', 'LRN123', 'Martinez', 'Jack', 'K', 'Male', '2015-11-11', 'English', 8, 'Type K', 'Christian'),
(12, 12, 12, 12, 12, 'Section L', 'LRN234', 'Hernandez', 'Kate', 'L', 'Female', '2016-12-12', 'English', 7, 'Type L', 'Christian'),
(13, 13, 13, 13, 13, 'Section M', 'LRN345', 'Lopez', 'Leo', 'M', 'Male', '2017-01-01', 'English', 6, 'Type M', 'Christian'),
(14, 14, 14, 14, 14, 'Section N', 'LRN456', 'Gonzalez', 'Mia', 'N', 'Female', '2018-02-02', 'English', 5, 'Type N', 'Christian'),
(15, 15, 15, 15, 15, 'Section O', 'LRN567', 'Wilson', 'Nina', 'O', 'Female', '2019-03-03', 'English', 4, 'Type O', 'Christian'),
(16, 16, 16, 16, 16, 'Section P', 'LRN678', 'Anderson', 'Oscar', 'P', 'Male', '2020-04-04', 'English', 3, 'Type P', 'Christian'),
(17, 17, 17, 17, 17, 'Section Q', 'LRN789', 'Thomas', 'Peter', 'Q', 'Male', '2021-05-05', 'English', 2, 'Type Q', 'Christian'),
(18, 18, 18, 18, 18, 'Section R', 'LRN890', 'Moore', 'Quinn', 'R', 'Female', '2022-06-06', 'English', 1, 'Type R', 'Christian'),
(19, 19, 19, 19, 19, 'Section S', 'LRN901', 'Taylor', 'Rachel', 'S', 'Female', '2023-07-07', 'English', 0, 'Type S', 'Christian'),
(20, 20, 20, 20, 20, 'Section T', 'LRN012', 'Martin', 'Sam', 'T', 'Male', '2024-08-08', 'English', 18, 'Type T', 'Christian'),
(21, 21, 21, 21, 21, 'Section U', 'LRN123', 'Lee', 'Tina', 'U', 'Female', '2025-09-09', 'English', 18, 'Type U', 'Christian')
ON DUPLICATE KEY UPDATE STUDENT_LASTNAME = VALUES(STUDENT_LASTNAME);

-- Insert sample data into PARTICIPANTS table
INSERT INTO PARTICIPANTS (PARTICIPANT_ID, STUDENT_UID, PARTICIPANT_TYPE, PARTICIPANT_LASTNAME, PARTICIPANT_FIRSTNAME, EMAIL, CONTACT_NUMBER)
VALUES
(1, 1, 'student', 'Garcia', 'Juan', 'juan.garcia@email.com', '09123456789'),
(2, NULL, 'non-student', 'Cruz', 'Maria', 'maria.cruz@email.com', '09234567890')
ON DUPLICATE KEY UPDATE PARTICIPANT_LASTNAME = VALUES(PARTICIPANT_LASTNAME);

-- Insert sample data into APPOINTMENTS table
INSERT INTO APPOINTMENTS (GUIDANCE_COUNSELOR_ID, APPOINTMENT_TITLE, CONSULTATION_TYPE, APPOINTMENT_DATE_TIME, APPOINTMENT_STATUS, APPOINTMENT_NOTES, UPDATED_AT)
VALUES
(2, 'First Appointment', 'Initial', '2023-10-02 10:00:00', 'Scheduled', NULL, '2023-10-01 12:00:00')
ON DUPLICATE KEY UPDATE APPOINTMENT_TITLE = VALUES(APPOINTMENT_TITLE);

-- Insert sample data into VIOLATION_RECORD table
INSERT INTO VIOLATION_RECORD (VIOLATION_ID, PARTICIPANT_ID, VIOLATION_TYPE, VIOLATION_DESCRIPTION, ANECDOTAL_RECORD, REINFORCEMENT, STATUS, UPDATED_AT)
VALUES
(1, 1, 'Minor', 'Violation description', 'Anecdotal record', 'Reinforcement action', 'Resolved', '2023-10-01 11:00:00')
ON DUPLICATE KEY UPDATE VIOLATION_TYPE = VALUES(VIOLATION_TYPE);

-- Insert sample data into SESSIONS table
INSERT INTO SESSIONS (APPOINTMENT_ID, GUIDANCE_COUNSELOR_ID, PARTICIPANT_ID, VIOLATION_ID, CONSULTATION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_STATUS, UPDATED_AT)
VALUES
(1, 1, 1, 1, 'Disciplinary', '2025-03-04 10:00:00', 'Discussed the importance of school rules and behavior expectations.','Completed', NOW())
ON DUPLICATE KEY UPDATE CONSULTATION_TYPE = VALUES(CONSULTATION_TYPE);

-- Insert sample data into SESSIONS_PARTICIPANTS table
INSERT INTO SESSIONS_PARTICIPANTS (PARTICIPANT_ID, SESSION_ID)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE PARTICIPANT_ID = VALUES(PARTICIPANT_ID);

-- Insert sample data into REMARK table
INSERT INTO REMARK (REMARK_ID, STUDENT_ID, REMARK_TEXT, REMARK_DATE)
VALUES
(1, 1, 'Good performance', '2023-10-01')
ON DUPLICATE KEY UPDATE REMARK_TEXT = VALUES(REMARK_TEXT);

-- Insert sample data into INCIDENTS table
INSERT INTO INCIDENTS (INCIDENT_ID, PARTICIPANT_ID, INCIDENT_DATE, INCIDENT_DESCRIPTION, ACTION_TAKEN, RECOMMENDATION, STATUS, UPDATED_AT)
VALUES
(1, 1, '2023-10-01 10:00:00', 'Minor incident', 'Warning given', 'Monitor behavior', 'Resolved', '2023-10-01 11:00:00')
ON DUPLICATE KEY UPDATE INCIDENT_DESCRIPTION = VALUES(INCIDENT_DESCRIPTION);

