CREATE DATABASE IF NOT EXISTS gomisDB;
USE gomisDB;
-- Drop existing tables to avoid conflicts
DROP TABLE IF EXISTS sessions_participants;
DROP TABLE IF EXISTS SESSIONS;
DROP TABLE IF EXISTS INCIDENTS;
DROP TABLE IF EXISTS VIOLATION_RECORD;
DROP TABLE IF EXISTS PARTICIPANTS;
DROP TABLE IF EXISTS STUDENT;
DROP TABLE IF EXISTS APPOINTMENTS;
DROP TABLE IF EXISTS USERS;
DROP TABLE IF EXISTS GUIDANCE_COUNSELORS;
DROP TABLE IF EXISTS ADDRESS;
DROP TABLE IF EXISTS REMARK;
DROP TABLE IF EXISTS GUARDIAN;
DROP TABLE IF EXISTS PARENT;


-- Create tables in correct order (tables with no dependencies first)
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

-- Create USERS table immediately after GUIDANCE_COUNSELORS
CREATE TABLE USERS (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    u_name VARCHAR(100) NOT NULL UNIQUE,
    u_pass VARCHAR(255) NOT NULL,
    guidance_COUNSELORS_ID INT,
    FOREIGN KEY (guidance_COUNSELORS_ID) REFERENCES GUIDANCE_COUNSELORS(guidance_COUNSELORS_ID) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- STUDENTS_DATA Table
CREATE TABLE STUDENT (
    STUDENT_UID INT PRIMARY KEY AUTO_INCREMENT,
    STUDENT_LRN VARCHAR(20) NOT NULL UNIQUE,
    STUDENT_LASTNAME VARCHAR(50) NOT NULL,
    STUDENT_FIRSTNAME VARCHAR(50) NOT NULL,
    STUDENT_MIDDLENAME VARCHAR(50),
    STUDENT_BIRTHDATE DATE,
    STUDENT_MOTHERTONGUE VARCHAR(50),
    STUDENT_AGE INT,
    STUDENT_IP_TYPE VARCHAR(20),
    STUDENT_RELIGION VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- PARTICIPANTS Table
CREATE TABLE PARTICIPANTS (
  participant_id INT PRIMARY KEY AUTO_INCREMENT,
  student_uid INT,
  participant_type VARCHAR(100) NOT NULL,
  LAST_NAME VARCHAR(100) NOT NULL,
  FIRST_NAME VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  contact_number VARCHAR(15),
  FOREIGN KEY (student_uid) REFERENCES STUDENT(STUDENT_UID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- VIOLATION_RECORD Table
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

-- APPOINTMENTS Table
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
    FOREIGN KEY (APPOINTMENT_ID) REFERENCES APPOINTMENTS(APPOINTMENT_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (COUNSELORS_ID) REFERENCES GUIDANCE_COUNSELORS(GUIDANCE_COUNSELORS_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS(PARTICIPANT_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (VIOLATION_ID) REFERENCES VIOLATION_RECORD(VIOLATION_ID) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sessions_participants (
    SESSION_ID INT,
    PARTICIPANT_ID INT,
    PRIMARY KEY (SESSION_ID, PARTICIPANT_ID),
    FOREIGN KEY (SESSION_ID) REFERENCES SESSIONS(SESSION_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS(participant_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ADDRESS (
    ADDRESS_ID INT PRIMARY KEY AUTO_INCREMENT,
    ADDRESS_HOUSENUMBER VARCHAR(50),
    STREET VARCHAR(50),
    ADDRESS_BARANGAY VARCHAR(50),
    ADDRESS_MUNICIPALITY VARCHAR(50),
    ADDRESS_PROVINCE VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE REMARK (
    REMARK_ID INT PRIMARY KEY AUTO_INCREMENT,
    STUDENT_ID INT,
    REMARK_TEXT VARCHAR(255),
    REMARK_DATE DATE,
    FOREIGN KEY (STUDENT_ID) REFERENCES STUDENT(STUDENT_UID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE GUARDIAN (
    GUARDIAN_ID INT PRIMARY KEY AUTO_INCREMENT,
    STUDENT_ID INT NOT NULL,
    GUARDIAN_LASTNAME VARCHAR(50) NOT NULL,
    GUARDIAN_FIRSTNAME VARCHAR(50) NOT NULL,
    GUARDIAN_MIDDLENAME VARCHAR(50),
    GUARDIAN_RELATIONSHIP VARCHAR(50) NOT NULL,
    FOREIGN KEY (STUDENT_ID) REFERENCES STUDENT(STUDENT_UID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE PARENT (
    PARENT_ID INT PRIMARY KEY AUTO_INCREMENT,
    FATHER_LASTNAME VARCHAR(50) NOT NULL,
    FATHER_FIRSTNAME VARCHAR(50) NOT NULL,
    FATHER_MIDDLENAME VARCHAR(50),
    MOTHER_LASTNAME VARCHAR(50) NOT NULL,
    MOTHER_FIRSTNAME VARCHAR(50) NOT NULL,
    MOTHER_MIDDLENAME VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Insert Guidance Counselors
INSERT INTO GUIDANCE_COUNSELORS (LAST_NAME, FIRST_NAME, middle_initial, suffix, gender, specialization, contact_num, email, position)
VALUES 
('Admin', 'admin', 'A', '', 'Male', 'Test Counselor', '09123456789', 'admin@admin.com', 'Head Counselor');

-- Insert Users
INSERT INTO USERS (u_name, u_pass, guidance_COUNSELORS_ID)
VALUES 
('admin', 'admin', 1);

-- Insert Students Data
INSERT INTO STUDENT (STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION)
VALUES 
('123456789', 'Cruz', 'Juan', 'Dela', '2005-05-15', 'Tagalog', 19, 'Regular', 'Catholic'),
('987654321', 'Santos', 'Maria', 'Lopez', '2006-07-20', 'Ilocano', 18, 'Special', 'Christian'),
('456123789', 'Reyes', 'Pedro', 'Aquino', '2004-12-10', 'Bisaya', 20, 'Regular', 'Muslim');

-- Insert Participants (Students as participants)
INSERT INTO PARTICIPANTS (student_uid, participant_type, LAST_NAME, FIRST_NAME, email, contact_number)
VALUES
(1, 'Student', 'Cruz', 'Juan', 'juan.cruz@student.edu', '09123456789'),
(2, 'Student', 'Santos', 'Maria', 'maria.santos@student.edu', '09234567890'),
(3, 'Student', 'Reyes', 'Pedro', 'pedro.reyes@student.edu', '09345678901');

-- Insert Violation Records
INSERT INTO VIOLATION_RECORD (student_uid, violation_type, violation_description, anecdotal_record, reinforcement, V_status)
VALUES
(1, 'Late', 'Student arrived 30 minutes late to class', 'Student explained transportation issues', 'Warning', 'Resolved'),
(2, 'Cheating', 'Student caught using notes during exam', 'Teacher confiscated notes and reported to admin', 'Detention', 'Pending'),
(3, 'Disruptive Behavior', 'Student was talking loudly and interrupting class', 'Teacher gave verbal warning', 'Parent Meeting', 'Ongoing');

INSERT INTO INCIDENTS (student_uid, participant_id, violation_id, incident_description, action_taken, recommendation, status)
VALUES
(1, 1, 1, 'Student was caught using a cheat sheet during Mathematics exam', 'Confiscation of materials and immediate parent notification', 'Regular counseling sessions and academic monitoring', 'Under Investigation'),
(2, 2, 2, 'Multiple instances of arriving late to morning classes', 'Parent conference conducted', 'Time management workshop attendance', 'Active'),
(3, 3, 3, 'Wearing incorrect school uniform components', 'Verbal warning issued', 'Review of school dress code policies', 'Resolved');

INSERT INTO ADDRESS (ADDRESS_HOUSENUMBER, STREET, ADDRESS_BARANGAY, ADDRESS_MUNICIPALITY, ADDRESS_PROVINCE)
VALUES
('123', 'Mabini St.', 'Barangay Uno', 'Quezon City', 'Metro Manila'),
('456', 'Rizal Ave.', 'Barangay Dos', 'Makati City', 'Metro Manila'),
('789', 'Bonifacio St.', 'Barangay Tres', 'Cebu City', 'Cebu');

INSERT INTO REMARK (STUDENT_ID, REMARK_TEXT, REMARK_DATE)
VALUES
(1, 'Late submission of project.', '2025-02-25'),
(2, 'Disruptive behavior in class.', '2025-02-24'),
(3, 'Violation of dress code policy.', '2025-02-23');

INSERT INTO GUARDIAN (STUDENT_ID, GUARDIAN_LASTNAME, GUARDIAN_FIRSTNAME, GUARDIAN_MIDDLENAME, GUARDIAN_RELATIONSHIP)
VALUES
(1, 'Dela Cruz', 'Mario', 'S.', 'Uncle'),
(2, 'Lopez', 'Carmen', 'G.', 'Aunt'),
(3, 'Santos', 'Fernando', 'M.', 'Grandfather');

INSERT INTO APPOINTMENTS (participant_id, counselors_id, appointment_title, appointment_type, appointment_date_time, appointment_notes, appointment_status)
VALUES
(1, 1, 'Counseling Session', 'Individual', '2025-03-01 10:00:00', 'First counseling session', 'Scheduled'),
(2, 1, 'Disciplinary Meeting', 'Group', '2025-03-05 14:00:00', 'Meeting with students involved in violations', 'Scheduled');

INSERT INTO SESSIONS (APPOINTMENT_ID, COUNSELORS_ID, PARTICIPANT_ID, VIOLATION_ID, SESSION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_STATUS)
VALUES
(1, 1, 1, 1, 'Counseling', '2025-03-01 10:30:00', 'Discussed student concerns and well-being', 'Completed'),
(2, 1, 2, 2, 'Disciplinary', '2025-03-05 14:30:00', 'Addressed cheating violation and consequences', 'Ongoing');

INSERT INTO sessions_participants (SESSION_ID, PARTICIPANT_ID)
VALUES
(1, 1),
(2, 2);

INSERT INTO PARENT (STUDENT_ID, PARENT_LASTNAME, PARENT_FIRSTNAME, PARENT_MIDDLENAME, PARENT_RELATIONSHIP, PARENT_CONTACT, PARENT_EMAIL)
VALUES
(1, 'Cruz', 'Maria', 'Santos', 'Mother', '09123456789', 'maria.cruz@parent.com'),
(2, 'Santos', 'Jose', 'Dela', 'Father', '09234567890', 'jose.santos@parent.com'),
(3, 'Reyes', 'Lucia', 'Aquino', 'Mother', '09345678901', 'lucia.reyes@parent.com');