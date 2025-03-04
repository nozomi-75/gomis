-- SQLBook: Code
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
(21, 'Lee', 'Tina', 'U', 'Uncle', '1234567890');

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
(21, 'Gina', 'Lee', 'NN', '0123456789', 'George', 'Lee', 'OO', '1234567890');


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
(21, '123', 'Beech St', 'Region 21', 'Province 21', 'City 21', 'Barangay 21', '1234');


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
(21, '1234567890');

-- Insert sample data into STUDENT table
INSERT INTO STUDENT (STUDENT_UID, PARENT_ID, GUARDIAN_ID, ADDRESS_ID, CONTACT_ID, STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, STUDENT_SEX, STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION)
VALUES
(1, 1, 1, 1, 1, 'LRN123', 'Doe', 'Jane', 'C', 'Female', '2005-01-01', 'English', 18, 'Type A', 'Christian'),
(2, 2, 2, 2, 2, 'LRN234', 'Smith', 'Alice', 'B', 'Female', '2006-02-02', 'English', 17, 'Type B', 'Christian'),
(3, 3, 3, 3, 3, 'LRN345', 'Johnson', 'Bob', 'C', 'Male', '2007-03-03', 'English', 16, 'Type C', 'Christian'),
(4, 4, 4, 4, 4, 'LRN456', 'Williams', 'Charlie', 'D', 'Female', '2008-04-04', 'English', 15, 'Type D', 'Christian'),
(5, 5, 5, 5, 5, 'LRN567', 'Brown', 'Daisy', 'E', 'Female', '2009-05-05', 'English', 14, 'Type E', 'Christian'),
(6, 6, 6, 6, 6, 'LRN678', 'Jones', 'Eve', 'F', 'Male', '2010-06-06', 'English', 13, 'Type F', 'Christian'),
(7, 7, 7, 7, 7, 'LRN789', 'Garcia', 'Frank', 'G', 'Male', '2011-07-07', 'English', 12, 'Type G', 'Christian'),
(8, 8, 8, 8, 8, 'LRN890', 'Miller', 'Grace', 'H', 'Female', '2012-08-08', 'English', 11, 'Type H', 'Christian'),
(9, 9, 9, 9, 9, 'LRN901', 'Davis', 'Heidi', 'I', 'Female', '2013-09-09', 'English', 10, 'Type I', 'Christian'),
(10, 10, 10, 10, 10, 'LRN012', 'Rodriguez', 'Ivy', 'J', 'Male', '2014-10-10', 'English', 9, 'Type J', 'Christian'),
(11, 11, 11, 11, 11, 'LRN123', 'Martinez', 'Jack', 'K', 'Male', '2015-11-11', 'English', 8, 'Type K', 'Christian'),
(12, 12, 12, 12, 12, 'LRN234', 'Hernandez', 'Kate', 'L', 'Female', '2016-12-12', 'English', 7, 'Type L', 'Christian'),
(13, 13, 13, 13, 13, 'LRN345', 'Lopez', 'Leo', 'M', 'Male', '2017-01-01', 'English', 6, 'Type M', 'Christian'),
(14, 14, 14, 14, 14, 'LRN456', 'Gonzalez', 'Mia', 'N', 'Female', '2018-02-02', 'English', 5, 'Type N', 'Christian'),
(15, 15, 15, 15, 15, 'LRN567', 'Wilson', 'Nina', 'O', 'Female', '2019-03-03', 'English', 4, 'Type O', 'Christian'),
(16, 16, 16, 16, 16, 'LRN678', 'Anderson', 'Oscar', 'P', 'Male', '2020-04-04', 'English', 3, 'Type P', 'Christian'),
(17, 17, 17, 17, 17, 'LRN789', 'Thomas', 'Peter', 'Q', 'Male', '2021-05-05', 'English', 2, 'Type Q', 'Christian'),
(18, 18, 18, 18, 18, 'LRN890', 'Moore', 'Quinn', 'R', 'Female', '2022-06-06', 'English', 1, 'Type R', 'Christian'),
(19, 19, 19, 19, 19, 'LRN901', 'Taylor', 'Rachel', 'S', 'Female', '2023-07-07', 'English', 0, 'Type S', 'Christian'),
(20, 20, 20, 20, 20, 'LRN012', 'Martin', 'Sam', 'T', 'Male', '2024-08-08', 'English', 18, 'Type T', 'Christian'),
(21, 21, 21, 21, 21, 'LRN123', 'Lee', 'Tina', 'U', 'Female', '2025-09-09', 'English', 18, 'Type U', 'Christian');


-- Insert sample data into PARTICIPANTS table
INSERT INTO PARTICIPANTS (PARTICIPANT_ID, STUDENT_UID, PARTICIPANT_TYPE, PARTICIPANT_LASTNAME, PARTICIPANT_FIRSTNAME, EMAIL, CONTACT_NUMBER)
VALUES
(1, 1, 'Student', 'Doe', 'Jane', 'jane.doe@example.com', '1234567890'),
(2, 2, 'Student', 'Smith', 'Alice', 'alice.smith@example.com', '2345678901'),
(3, 3, 'Student', 'Johnson', 'Bob', 'bob.johnson@example.com', '3456789012'),
(4, 4, 'Student', 'Williams', 'Charlie', 'charlie.williams@example.com', '4567890123'),
(5, 5, 'Student', 'Brown', 'Daisy', 'daisy.brown@example.com', '5678901234'),
(6, 6, 'Student', 'Jones', 'Eve', 'eve.jones@example.com', '6789012345'),
(7, 7, 'Student', 'Garcia', 'Frank', 'frank.garcia@example.com', '7890123456'),
(8, 8, 'Student', 'Miller', 'Grace', 'grace.miller@example.com', '8901234567'),
(9, 9, 'Student', 'Davis', 'Heidi', 'heidi.davis@example.com', '9012345678'),
(10, 10, 'Student', 'Rodriguez', 'Ivy', 'ivy.rodriguez@example.com', '0123456789'),
(11, 11, 'Student', 'Martinez', 'Jack', 'jack.martinez@example.com', '1234567890'),
(12, 12, 'Student', 'Hernandez', 'Kate', 'kate.hernandez@example.com', '2345678901'),
(13, 13, 'Student', 'Lopez', 'Leo', 'leo.lopez@example.com', '3456789012'),
(14, 14, 'Student', 'Gonzalez', 'Mia', 'mia.gonzalez@example.com', '4567890123'),
(15, 15, 'Student', 'Wilson', 'Nina', 'nina.wilson@example.com', '5678901234'),
(16, 16, 'Student', 'Anderson', 'Oscar', 'oscar.anderson@example.com', '6789012345'),
(17, 17, 'Student', 'Thomas', 'Peter', 'peter.thomas@example.com', '7890123456'),
(18, 18, 'Student', 'Moore', 'Quinn', 'quinn.moore@example.com', '8901234567'),
(19, 19, 'Student', 'Taylor', 'Rachel', 'rachel.taylor@example.com', '9012345678'),
(20, 20, 'Student', 'Martin', 'Sam', 'sam.martin@example.com', '0123456789'),
(21, 21, 'Student', 'Lee', 'Tina', 'tina.lee@example.com', '1234567890');

-- Insert sample data into REMARK table
INSERT INTO REMARK (REMARK_ID, STUDENT_ID, REMARK_TEXT, REMARK_DATE)
VALUES
(1, 1, 'Good performance', '2023-10-01');

-- Insert sample data into INCIDENTS table
INSERT INTO INCIDENTS (INCIDENT_ID, PARTICIPANT_ID, INCIDENT_DATE, INCIDENT_DESCRIPTION, ACTION_TAKEN, RECOMMENDATION, STATUS, UPDATED_AT)
VALUES
(1, 1, '2023-10-01 10:00:00', 'Minor incident', 'Warning given', 'Monitor behavior', 'Resolved', '2023-10-01 11:00:00');

-- Insert sample data into VIOLATION_RECORD table
INSERT INTO VIOLATION_RECORD (VIOLATION_ID, PARTICIPANT_ID, VIOLATION_TYPE, VIOLATION_DESCRIPTION, ANECDOTAL_RECORD, REINFORCEMENT, STATUS, UPDATED_AT)
VALUES
(1, 1, 'Minor', 'Violation description', 'Anecdotal record', 'Reinforcement action', 'Resolved', '2023-10-01 11:00:00');

-- Insert sample data into GUIDANCE_COUNSELORS table
INSERT INTO GUIDANCE_COUNSELORS (GUIDANCE_COUNSELOR_ID, LAST_NAME, FIRST_NAME, MIDDLE_INITIAL, SUFFIX, GENDER, SPECIALIZATION, CONTACT_NUM, EMAIL, POSITION, PROFILE_PICTURE)
VALUES
(1, 'Smith', 'Alice', 'B', 'Jr', 'Female', 'Counseling', 987654321, 'alice.smith@example.com', 'Counselor', NULL);

-- Insert sample data into APPOINTMENTS table
INSERT INTO APPOINTMENTS (APPOINTMENT_ID, PARTICIPANT_ID, GUIDANCE_COUNSELOR_ID, APPOINTMENT_TITLE, APPOINTMENT_TYPE, APPOINTMENT_DATE_TIME, APPOINTMENT_STATUS, APPOINTMENT_NOTES, UPDATED_AT)
VALUES
(1, 1, 1, 'First Appointment', 'Initial', '2023-10-02 10:00:00', 'Scheduled', 'Initial meeting', '2023-10-01 12:00:00');

-- Insert sample data into USERS table
INSERT INTO USERS (USER_ID, U_NAME, U_PASS, GUIDANCE_COUNSELOR_ID)
VALUES
(1, 'admin', 'admin', 1);
