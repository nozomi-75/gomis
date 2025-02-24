CREATE DATABASE IF NOT EXISTS gomisDB;
USE gomisDB;
-- Drop existing tables to avoid conflicts
DROP TABLE IF EXISTS sessions_participants;
DROP TABLE IF EXISTS SESSIONS;
DROP TABLE IF EXISTS INCIDENTS;
DROP TABLE IF EXISTS STUDENT_RECORD;
DROP TABLE IF EXISTS VIOLATION_RECORD;
DROP TABLE IF EXISTS APPOINTMENTS;
DROP TABLE IF EXISTS PARTICIPANTS;
DROP TABLE IF EXISTS STUDENTS_DATA;
DROP TABLE IF EXISTS USERS;
DROP TABLE IF EXISTS GUIDANCE_COUNSELORS;


-- Create tables in correct order (tables with no dependencies first)
CREATE TABLE IF NOT EXISTS GUIDANCE_COUNSELORS (
  guidance_COUNSELORS_ID INT PRIMARY KEY AUTO_INCREMENT,
  LAST_NAME VARCHAR(60) NOT NULL,
  FIRST_NAME VARCHAR(60) NOT NULL,
  middle_initial VARCHAR(1) NOT NULL, 
  suffix VARCHAR(2),
  gender VARCHAR(6),
  specialization VARCHAR(100),
  contact_num VARCHAR(20),
  email VARCHAR(100),
  position VARCHAR(50),
  profile_picture BLOB
) ENGINE=InnoDB;

-- Create USERS table immediately after GUIDANCE_COUNSELORS
CREATE TABLE IF NOT EXISTS USERS (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    u_name VARCHAR(100) NOT NULL UNIQUE,
    u_pass VARCHAR(255) NOT NULL,
    guidance_COUNSELORS_ID INT,
    FOREIGN KEY (guidance_COUNSELORS_ID) REFERENCES GUIDANCE_COUNSELORS(guidance_COUNSELORS_ID)
) ENGINE=InnoDB;

-- STUDENTS_DATA Table
CREATE TABLE IF NOT EXISTS STUDENTS_DATA (
  student_uid INT PRIMARY KEY AUTO_INCREMENT,
  LRN VARCHAR(12) NOT NULL UNIQUE,
  LAST_NAME VARCHAR(60) NOT NULL,
  FIRST_NAME VARCHAR(60) NOT NULL,
  middle_initial VARCHAR(1) NOT NULL,
  suffix VARCHAR(2),
  gender VARCHAR(6) NOT NULL,
  DOB DATE NOT NULL,
  email VARCHAR(100) NOT NULL,
  contact_number VARCHAR(15) NOT NULL,
  guardian_name VARCHAR(150) NOT NULL,
  guardian_email VARCHAR(100) NOT NULL,
  guardian_contact_number VARCHAR(15) NOT NULL,
  ADDRESS VARCHAR(300) NOT NULL
) ENGINE=InnoDB;

-- PARTICIPANTS Table
CREATE TABLE IF NOT EXISTS PARTICIPANTS (
  participant_id INT PRIMARY KEY AUTO_INCREMENT,
  student_uid INT,
  participant_type VARCHAR(100) NOT NULL,
  LAST_NAME VARCHAR(100) NOT NULL,
  FIRST_NAME VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  contact_number VARCHAR(15),
  FOREIGN KEY (student_uid) REFERENCES STUDENTS_DATA(student_uid) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- VIOLATION_RECORD Table
CREATE TABLE IF NOT EXISTS VIOLATION_RECORD (
  violation_id INT PRIMARY KEY AUTO_INCREMENT,
  participant_id INT NOT NULL,
  violation_type VARCHAR(255),
  violation_description TEXT NOT NULL,
  anecdotal_record TEXT,
  reinforcement VARCHAR(255),
  V_status VARCHAR(50),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (participant_id) REFERENCES PARTICIPANTS(participant_id)
) ENGINE=InnoDB;

-- STUDENT_RECORD Table
CREATE TABLE IF NOT EXISTS STUDENT_RECORD (
  student_record_id INT PRIMARY KEY AUTO_INCREMENT,
  student_uid INT NOT NULL,
  violation_id INT,
  type_of_student VARCHAR(100) NOT NULL,
  academic_year VARCHAR(100) NOT NULL,
  semester VARCHAR(100) NOT NULL,
  strand VARCHAR(100) NOT NULL,
  track VARCHAR(100),
  year_level INT,
  adviser VARCHAR(100) NOT NULL,
  section VARCHAR(100) NOT NULL,
  stud_status VARCHAR(100) DEFAULT 'Active',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (student_uid) REFERENCES STUDENTS_DATA(student_uid),
  FOREIGN KEY (violation_id) REFERENCES VIOLATION_RECORD(violation_id)
) ENGINE=InnoDB;

-- INCIDENTS Table
CREATE TABLE IF NOT EXISTS INCIDENTS (
  incident_id INT PRIMARY KEY AUTO_INCREMENT,
  student_uid INT, 
  participant_id INT,
  violation_id INT ,
  incident_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  incident_description TEXT NOT NULL,
  action_taken TEXT,
  recommendation TEXT,
  status VARCHAR(30), 
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (student_uid) REFERENCES STUDENTS_DATA(student_uid) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (participant_id) REFERENCES PARTICIPANTS(participant_id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (violation_id) REFERENCES VIOLATION_RECORD(violation_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- APPOINTMENTS Table
CREATE TABLE IF NOT EXISTS APPOINTMENTS (
  appointment_id INT PRIMARY KEY AUTO_INCREMENT,
  participant_id INT NOT NULL,
  counselors_id INT, 
  appointment_title VARCHAR(50),
  appointment_type VARCHAR(255),
  appointment_date_time DATETIME NOT NULL,
  appointment_notes VARCHAR(100),
  appointment_status VARCHAR(50),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (participant_id) REFERENCES PARTICIPANTS(participant_id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (counselors_id) REFERENCES GUIDANCE_COUNSELORS(guidance_COUNSELORS_ID) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;


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
    UPDATED_AT DATETIME,
    FOREIGN KEY (APPOINTMENT_ID) REFERENCES APPOINTMENTS(APPOINTMENT_ID),
    FOREIGN KEY (COUNSELORS_ID) REFERENCES GUIDANCE_COUNSELORS(GUIDANCE_COUNSELORS_ID),
    FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS(PARTICIPANT_ID),
    FOREIGN KEY (VIOLATION_ID) REFERENCES VIOLATION_RECORD(VIOLATION_ID)
);

CREATE TABLE sessions_participants (
    SESSION_ID INT,
    PARTICIPANT_ID INT,
    PRIMARY KEY (SESSION_ID, PARTICIPANT_ID),
    FOREIGN KEY (SESSION_ID) REFERENCES SESSIONS(SESSION_ID) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS(participant_id)
        ON DELETE CASCADE 
        ON UPDATE CASCADE
) ENGINE=InnoDB;


-- Insert Guidance Counselors
INSERT INTO GUIDANCE_COUNSELORS (LAST_NAME, FIRST_NAME, middle_initial, suffix, gender, specialization, contact_num, email, position) VALUES
('Santos', 'Maria', 'A', '', 'Female', 'Behavioral Counseling', '09123456789', 'maria.santos@school.edu', 'Head Counselor'),
('Cruz', 'Juan', 'B', 'Jr', 'Male', 'Academic Counseling', '09234567890', 'juan.cruz@school.edu', 'Senior Counselor'),
('Reyes', 'Ana', 'C', '', 'Female', 'Career Counseling', '09345678901', 'ana.reyes@school.edu', 'Counselor');

-- Insert Users
INSERT INTO USERS (u_name, u_pass, guidance_COUNSELORS_ID) VALUES
('admin', 'admin', 1),
('jcruz', 'passy234', 2),
('areyes', 'pass123', 3);

-- Insert Students Data
INSERT INTO STUDENTS_DATA (LRN, LAST_NAME, FIRST_NAME, middle_initial, suffix, gender, DOB, email, contact_number, guardian_name, guardian_email, guardian_contact_number, ADDRESS) VALUES
('123456789012', 'Garcia', 'Juan', 'P', '', 'Male', '2006-05-15', 'juan.garcia@student.edu', '09123456789', 'Maria Garcia', 'maria.garcia@email.com', '09987654321', '123 Main St, Manila'),
('123456789013', 'Santos', 'Maria', 'L', '', 'Female', '2006-07-20', 'maria.santos@student.edu', '09234567890', 'Pedro Santos', 'pedro.santos@email.com', '09876543210', '456 Oak St, Quezon City'),
('123456789014', 'Reyes', 'Carlos', 'M', 'Jr', 'Male', '2006-03-10', 'carlos.reyes@student.edu', '09345678901', 'Carlos Reyes Sr', 'carlos.reyes@email.com', '09765432109', '789 Pine St, Makati'),
('123456789015', 'Luna', 'Sofia', 'R', '', 'Female', '2006-09-25', 'sofia.luna@student.edu', '09456789012', 'Ana Luna', 'ana.luna@email.com', '09654321098', '321 Elm St, Pasig'),
('123456789016', 'Cruz', 'Miguel', 'T', '', 'Male', '2006-11-30', 'miguel.cruz@student.edu', '09567890123', 'Jose Cruz', 'jose.cruz@email.com', '09543210987', '654 Cedar St, Mandaluyong');

-- Insert Participants (Students as participants)
INSERT INTO PARTICIPANTS (student_uid, participant_type, LAST_NAME, FIRST_NAME, email, contact_number) VALUES
(1, 'Student', 'Garcia', 'Juan', 'juan.garcia@student.edu', '09123456789'),
(2, 'Student', 'Santos', 'Maria', 'maria.santos@student.edu', '09234567890'),
(3, 'Student', 'Reyes', 'Carlos', 'carlos.reyes@student.edu', '09345678901'),
(4, 'Student', 'Luna', 'Sofia', 'sofia.luna@student.edu', '09456789012'),
(5, 'Student', 'Cruz', 'Miguel', 'miguel.cruz@student.edu', '09567890123');

-- Insert Violation Records
INSERT INTO VIOLATION_RECORD (participant_id, violation_type, violation_description, anecdotal_record, reinforcement, V_status) VALUES
(1, 'Academic', 'Cheating during midterm exam', 'Student was caught using unauthorized materials', 'Academic counseling and written warning', 'Active'),
(2, 'Behavioral', 'Excessive tardiness', 'Student has been late to class 5 times this month', 'Parent conference and attendance monitoring', 'Active'),
(3, 'Disciplinary', 'Dress code violation', 'Not wearing proper school uniform', 'Verbal warning', 'Resolved');

-- Insert Student Records
INSERT INTO STUDENT_RECORD (student_uid, violation_id, type_of_student, academic_year, semester, strand, track, year_level, adviser, section) VALUES
(1, 1, 'Regular', '2023-2024', 'First', 'STEM', 'Academic', 11, 'Ms. Rodriguez', 'STEM-11A'),
(2, 2, 'Regular', '2023-2024', 'First', 'ABM', 'Academic', 11, 'Mr. Torres', 'ABM-11B'),
(3, 3, 'Regular', '2023-2024', 'First', 'HUMSS', 'Academic', 11, 'Mrs. Fernandez', 'HUMSS-11A'),
(4, NULL, 'Regular', '2023-2024', 'First', 'GAS', 'Academic', 11, 'Ms. Lim', 'GAS-11A'),
(5, NULL, 'Regular', '2023-2024', 'First', 'STEM', 'Academic', 11, 'Ms. Rodriguez', 'STEM-11A');

-- Insert Incidents
INSERT INTO INCIDENTS (student_uid, participant_id, violation_id, incident_description, action_taken, recommendation, status) VALUES
(1, 1, 1, 'Student was caught using a cheat sheet during Mathematics exam', 'Confiscation of materials and immediate parent notification', 'Regular counseling sessions and academic monitoring', 'Under Investigation'),
(2, 2, 2, 'Multiple instances of arriving late to morning classes', 'Parent conference conducted', 'Time management workshop attendance', 'Active'),
(3, 3, 3, 'Wearing incorrect school uniform components', 'Verbal warning issued', 'Review of school dress code policies', 'Resolved');

