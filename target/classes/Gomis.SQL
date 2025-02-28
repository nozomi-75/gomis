CREATE DATABASE IF NOT EXISTS gomisDB;
USE gomisDB;

-- Drop existing tables to avoid conflicts (reverse dependency order)
DROP TABLE IF EXISTS sessions_participants;
DROP TABLE IF EXISTS SESSIONS;
DROP TABLE IF EXISTS INCIDENTS;
DROP TABLE IF EXISTS VIOLATION_RECORD;
DROP TABLE IF EXISTS PARTICIPANTS;
DROP TABLE IF EXISTS APPOINTMENTS;
DROP TABLE IF EXISTS REMARK;
DROP TABLE IF EXISTS GUARDIAN;
DROP TABLE IF EXISTS STUDENT;
DROP TABLE IF EXISTS PARENT;
DROP TABLE IF EXISTS ADDRESS;
DROP TABLE IF EXISTS USERS;
DROP TABLE IF EXISTS GUIDANCE_COUNSELORS;

-- Create tables in correct order (no dependencies first)

-- ADDRESS table (no dependencies)
CREATE TABLE ADDRESS (
    ADDRESS_ID INT PRIMARY KEY AUTO_INCREMENT,
    ADDRESS_HOUSENUMBER VARCHAR(50),
    STREET VARCHAR(50),
    ADDRESS_BARANGAY VARCHAR(50),
    ADDRESS_MUNICIPALITY VARCHAR(50),
    ADDRESS_PROVINCE VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PARENT table (no dependencies)
CREATE TABLE PARENT (
    PARENT_ID INT PRIMARY KEY AUTO_INCREMENT,
    FATHER_LASTNAME VARCHAR(50) NOT NULL,
    FATHER_FIRSTNAME VARCHAR(50) NOT NULL,
    FATHER_MIDDLENAME VARCHAR(50),
    MOTHER_LASTNAME VARCHAR(50) NOT NULL,
    MOTHER_FIRSTNAME VARCHAR(50) NOT NULL,
    MOTHER_MIDDLENAME VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- GUIDANCE_COUNSELORS table (no dependencies)
CREATE TABLE GUIDANCE_COUNSELORS (
    guidance_COUNSELORS_ID INT PRIMARY KEY AUTO_INCREMENT,
    LAST_NAME VARCHAR(60) NOT NULL,
    FIRST_NAME VARCHAR(60) NOT NULL,
    middle_initial VARCHAR(1) NOT NULL, 
    suffix VARCHAR(5),
    gender VARCHAR(10),
    specialization VARCHAR(100),
    contact_num VARCHAR(20),
    email VARCHAR(100),
    position VARCHAR(50),
    profile_picture BLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- USERS table (depends on GUIDANCE_COUNSELORS)
CREATE TABLE USERS (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    u_name VARCHAR(100) NOT NULL UNIQUE,
    u_pass VARCHAR(255) NOT NULL,
    guidance_COUNSELORS_ID INT,
    FOREIGN KEY (guidance_COUNSELORS_ID) REFERENCES GUIDANCE_COUNSELORS(guidance_COUNSELORS_ID) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PARTICIPANTS table (will depend on STUDENT later, but created early for APPOINTMENTS without its foreign key initially)
CREATE TABLE PARTICIPANTS (
    participant_id INT PRIMARY KEY AUTO_INCREMENT,
    student_uid INT,
    participant_type VARCHAR(100) NOT NULL,
    LAST_NAME VARCHAR(100) NOT NULL,
    FIRST_NAME VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    contact_number VARCHAR(15)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- APPOINTMENTS table (depends on PARTICIPANTS, GUIDANCE_COUNSELORS)
CREATE TABLE APPOINTMENTS (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    participant_id INT NOT NULL,
    counselors_id INT, 
    appointment_title VARCHAR(50),
    appointment_type VARCHAR(255),
    appointment_date_time DATETIME NOT NULL,
    appointment_notes VARCHAR(100),
    appointment_status VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (participant_id) REFERENCES PARTICIPANTS(participant_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (counselors_id) REFERENCES GUIDANCE_COUNSELORS(guidance_COUNSELORS_ID) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- STUDENT table (depends on PARENT, ADDRESS, APPOINTMENTS; GUARDIAN will come after)
CREATE TABLE STUDENT (
    STUDENT_UID INT PRIMARY KEY,
    Parent_ID INT,
    Guardian_ID INT,
    APPOINTMENTS_ID INT,
    CONTACT_ID INT,
    STUDENT_LRN VARCHAR(50),
    STUDENT_LASTNAME VARCHAR(50),
    STUDENT_FIRSTNAME VARCHAR(50),
    STUDENT_MIDDLENAME VARCHAR(50),
    STUDENT_SEX VARCHAR(6),
    STUDENT_BIRTHDATE DATE,
    STUDENT_MOTHERTONGUE VARCHAR(50),
    STUDENT_AGE INTEGER,
    STUDENT_IP_TYPE VARCHAR(50),
    STUDENT_RELIGION VARCHAR(50),
    FOREIGN KEY (Parent_ID) REFERENCES PARENT(PARENT_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (APPOINTMENTS_ID) REFERENCES APPOINTMENTS(appointment_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (CONTACT_ID) REFERENCES ADDRESS(ADDRESS_ID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- GUARDIAN table (depends on STUDENT)
CREATE TABLE GUARDIAN (
    GUARDIAN_ID INT PRIMARY KEY AUTO_INCREMENT,
    STUDENT_ID INT NOT NULL,
    GUARDIAN_LASTNAME VARCHAR(50) NOT NULL,
    GUARDIAN_FIRSTNAME VARCHAR(50) NOT NULL,
    GUARDIAN_MIDDLENAME VARCHAR(50),
    GUARDIAN_RELATIONSHIP VARCHAR(50) NOT NULL,
    FOREIGN KEY (STUDENT_ID) REFERENCES STUDENT(STUDENT_UID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add foreign key to STUDENT for Guardian_ID now that GUARDIAN exists
ALTER TABLE STUDENT
ADD FOREIGN KEY (Guardian_ID) REFERENCES GUARDIAN(GUARDIAN_ID) ON DELETE CASCADE ON UPDATE CASCADE;

-- Add foreign key to PARTICIPANTS now that STUDENT exists
ALTER TABLE PARTICIPANTS
ADD FOREIGN KEY (student_uid) REFERENCES STUDENT(STUDENT_UID) ON DELETE CASCADE ON UPDATE CASCADE;

-- VIOLATION_RECORD table (depends on STUDENT)
CREATE TABLE VIOLATION_RECORD (
    violation_id INT PRIMARY KEY AUTO_INCREMENT,
    student_uid INT NOT NULL,
    violation_type VARCHAR(255),
    violation_description TEXT NOT NULL,
    anecdotal_record TEXT,
    reinforcement VARCHAR(255),
    V_status VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_uid) REFERENCES STUDENT(STUDENT_UID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- INCIDENTS table (depends on STUDENT, PARTICIPANTS, VIOLATION_RECORD)
CREATE TABLE INCIDENTS (
    incident_id INT PRIMARY KEY AUTO_INCREMENT,
    student_uid INT, 
    participant_id INT,
    violation_id INT,
    incident_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    incident_description TEXT NOT NULL,
    action_taken TEXT,
    recommendation TEXT,
    status VARCHAR(30), 
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_uid) REFERENCES STUDENT(STUDENT_UID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (participant_id) REFERENCES PARTICIPANTS(participant_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (violation_id) REFERENCES VIOLATION_RECORD(violation_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SESSIONS table (depends on APPOINTMENTS, GUIDANCE_COUNSELORS, PARTICIPANTS, VIOLATION_RECORD)
CREATE TABLE SESSIONS (
    SESSION_ID INT PRIMARY KEY AUTO_INCREMENT,
    APPOINTMENT_ID INT,
    COUNSELORS_ID INT,
    PARTICIPANT_ID INT,
    VIOLATION_ID INT,
    SESSION_TYPE VARCHAR(50),
    SESSION_DATE_TIME DATETIME,
    SESSION_NOTES TEXT,
    SESSION_STATUS VARCHAR(20),
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (APPOINTMENT_ID) REFERENCES APPOINTMENTS(appointment_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (COUNSELORS_ID) REFERENCES GUIDANCE_COUNSELORS(guidance_COUNSELORS_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS(participant_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (VIOLATION_ID) REFERENCES VIOLATION_RECORD(violation_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sessions_participants table (depends on SESSIONS, PARTICIPANTS)
CREATE TABLE sessions_participants (
    SESSION_ID INT,
    PARTICIPANT_ID INT,
    PRIMARY KEY (SESSION_ID, PARTICIPANT_ID),
    FOREIGN KEY (SESSION_ID) REFERENCES SESSIONS(SESSION_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS(participant_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- REMARK table (depends on STUDENT)
CREATE TABLE REMARK (
    REMARK_ID INT PRIMARY KEY AUTO_INCREMENT,
    STUDENT_ID INT,
    REMARK_TEXT VARCHAR(255),
    REMARK_DATE DATE,
    FOREIGN KEY (STUDENT_ID) REFERENCES STUDENT(STUDENT_UID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE CONTACT (
    CONTACT_ID INT PRIMARY KEY AUTO_INCREMENT,
    CONTACT_NUMBER VARCHAR(20) NOT NULL
);

-- Insert sample values
INSERT INTO CONTACT (CONTACT_NUMBER) VALUES ('09123456789');
INSERT INTO CONTACT (CONTACT_NUMBER) VALUES ('09876543210');
INSERT INTO CONTACT (CONTACT_NUMBER) VALUES ('09551234567');


-- Insert data in correct dependency order

-- Insert into PARENT
INSERT INTO PARENT (FATHER_LASTNAME, FATHER_FIRSTNAME, FATHER_MIDDLENAME, MOTHER_LASTNAME, MOTHER_FIRSTNAME, MOTHER_MIDDLENAME)
VALUES
('Cruz', 'Jose', 'A', 'Cruz', 'Maria', 'Santos'),
('Santos', 'Juan', 'B', 'Santos', 'Clara', 'Dela'),
('Reyes', 'Pedro', 'C', 'Reyes', 'Lucia', 'Aquino');

-- Insert into ADDRESS
INSERT INTO ADDRESS (ADDRESS_HOUSENUMBER, STREET, ADDRESS_BARANGAY, ADDRESS_MUNICIPALITY, ADDRESS_PROVINCE)
VALUES
('123', 'Mabini St.', 'Barangay Uno', 'Quezon City', 'Metro Manila'),
('456', 'Rizal Ave.', 'Barangay Dos', 'Makati City', 'Metro Manila'),
('789', 'Bonifacio St.', 'Barangay Tres', 'Cebu City', 'Cebu');

-- Insert into GUIDANCE_COUNSELORS
INSERT INTO GUIDANCE_COUNSELORS (LAST_NAME, FIRST_NAME, middle_initial, suffix, gender, specialization, contact_num, email, position)
VALUES 
('Admin', 'Admin', 'A', '', 'Male', 'Test Counselor', '09123456789', 'admin@admin.com', 'Head Counselor');

-- Insert into USERS
INSERT INTO USERS (u_name, u_pass, guidance_COUNSELORS_ID)
VALUES 
('admin', 'admin', 1);

-- Insert into PARTICIPANTS (initially without student_uid)
INSERT INTO PARTICIPANTS (participant_type, LAST_NAME, FIRST_NAME, email, contact_number)
VALUES
('Student', 'Cruz', 'Juan', 'juan.cruz@student.edu', '09123456789'),
('Student', 'Santos', 'Maria', 'maria.santos@student.edu', '09234567890'),
('Student', 'Reyes', 'Pedro', 'pedro.reyes@student.edu', '09345678901');

-- Insert into APPOINTMENTS
INSERT INTO APPOINTMENTS (participant_id, counselors_id, appointment_title, appointment_type, appointment_date_time, appointment_notes, appointment_status)
VALUES
(1, 1, 'Counseling Session', 'Individual', '2025-03-01 10:00:00', 'First counseling session', 'Scheduled'),
(2, 1, 'Disciplinary Meeting', 'Group', '2025-03-05 14:00:00', 'Meeting with students involved in violations', 'Scheduled');

-- Insert into STUDENT (all required rows with valid dependencies)
INSERT INTO STUDENT (STUDENT_UID, Parent_ID, APPOINTMENTS_ID, CONTACT_ID, STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, STUDENT_SEX, STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION)
VALUES
(1, 1, 1, 1, 'LRN123456789', 'Smith', 'John', 'A', 'Male', '2005-06-15', 'English', 19, 'Type1', 'Christianity'),
(2, 2, 2, 2, 'LRN987654321', 'Doe', 'Jane', 'B', 'Female', '2006-04-10', 'Spanish', 18, 'Type2', 'Islam'),
(3, 3, NULL, 3, 'LRN456789123', 'Reyes', 'Pedro', 'C', 'Male', '2005-08-20', 'Tagalog', 19, 'Type1', 'Christianity');

-- Insert into GUARDIAN
INSERT INTO GUARDIAN (STUDENT_ID, GUARDIAN_LASTNAME, GUARDIAN_FIRSTNAME, GUARDIAN_MIDDLENAME, GUARDIAN_RELATIONSHIP)
VALUES
(1, 'Dela Cruz', 'Mario', 'S', 'Uncle'),
(2, 'Lopez', 'Carmen', 'G', 'Aunt'),
(3, 'Santos', 'Fernando', 'M', 'Grandfather');

-- Update STUDENT with Guardian_ID
UPDATE STUDENT SET Guardian_ID = 1 WHERE STUDENT_UID = 1;
UPDATE STUDENT SET Guardian_ID = 2 WHERE STUDENT_UID = 2;
UPDATE STUDENT SET Guardian_ID = 3 WHERE STUDENT_UID = 3;

-- Update PARTICIPANTS with student_uid (replacing earlier duplicate insert)
UPDATE PARTICIPANTS SET student_uid = 1 WHERE participant_id = 1;
UPDATE PARTICIPANTS SET student_uid = 2 WHERE participant_id = 2;
UPDATE PARTICIPANTS SET student_uid = 3 WHERE participant_id = 3;

-- Insert into VIOLATION_RECORD
INSERT INTO VIOLATION_RECORD (student_uid, violation_type, violation_description, anecdotal_record, reinforcement, V_status)
VALUES
(1, 'Late', 'Student arrived 30 minutes late to class', 'Student explained transportation issues', 'Warning', 'Resolved'),
(2, 'Cheating', 'Student caught using notes during exam', 'Teacher confiscated notes and reported to admin', 'Detention', 'Pending'),
(3, 'Disruptive Behavior', 'Student was talking loudly and interrupting class', 'Teacher gave verbal warning', 'Parent Meeting', 'Ongoing');

-- Insert into INCIDENTS
INSERT INTO INCIDENTS (student_uid, participant_id, violation_id, incident_description, action_taken, recommendation, status)
VALUES
(1, 1, 1, 'Student was caught using a cheat sheet during Mathematics exam', 'Confiscation of materials and immediate parent notification', 'Regular counseling sessions and academic monitoring', 'Under Investigation'),
(2, 2, 2, 'Multiple instances of arriving late to morning classes', 'Parent conference conducted', 'Time management workshop attendance', 'Active'),
(3, 3, 3, 'Wearing incorrect school uniform components', 'Verbal warning issued', 'Review of school dress code policies', 'Resolved');

-- Insert into SESSIONS
INSERT INTO SESSIONS (APPOINTMENT_ID, COUNSELORS_ID, PARTICIPANT_ID, VIOLATION_ID, SESSION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_STATUS)
VALUES
(1, 1, 1, 1, 'Counseling', '2025-03-01 10:30:00', 'Discussed student concerns and well-being', 'Completed'),
(2, 1, 2, 2, 'Disciplinary', '2025-03-05 14:30:00', 'Addressed cheating violation and consequences', 'Ongoing');

-- Insert into sessions_participants
INSERT INTO sessions_participants (SESSION_ID, PARTICIPANT_ID)
VALUES
(1, 1),
(2, 2);

-- Insert into REMARK
INSERT INTO REMARK (STUDENT_ID, REMARK_TEXT, REMARK_DATE)
VALUES
(1, 'Late submission of project', '2025-02-25'),
(2, 'Disruptive behavior in class', '2025-02-24'),
(3, 'Violation of dress code policy', '2025-02-23');