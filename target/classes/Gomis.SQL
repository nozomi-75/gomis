CREATE DATABASE IF NOT EXISTS gomisDB;
USE gomisDB;
-- Drop existing tables to avoid conflicts
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
  guidance_counselor_id INT PRIMARY KEY AUTO_INCREMENT,
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
    guidance_counselor_id INT,
    FOREIGN KEY (guidance_counselor_id) REFERENCES GUIDANCE_COUNSELORS(guidance_counselor_id)
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
  student_uid INT NOT NULL,
  counselors_id INT, 
  appointment_type VARCHAR(255),
  appointment_date_time DATETIME NOT NULL,
  appointment_status VARCHAR(50),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (student_uid) REFERENCES STUDENTS_DATA(student_uid) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (counselors_id) REFERENCES GUIDANCE_COUNSELORS(guidance_counselor_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Reorganize data insertion order
-- 1. First insert GUIDANCE_COUNSELORS
INSERT INTO GUIDANCE_COUNSELORS (LAST_NAME, FIRST_NAME, middle_initial, gender, specialization, contact_num, email, position)
VALUES
('Reyes', 'Ana', 'D', 'Female', 'Child Psychology', '09178901234', 'ana.reyes@example.com', 'Guidance Counselor'),
('Cruz', 'Jose', 'E', 'Male', 'Adolescent Counseling', '09187654321', 'jose.cruz@example.com', 'Guidance Counselor'),
('Lopez', 'Maria', 'F', 'Female', 'Career Counseling', '09151234567', 'maria.lopez@example.com', 'Senior Guidance Counselor');

-- Insert students
INSERT INTO STUDENTS_DATA (LRN, LAST_NAME, FIRST_NAME, middle_initial, gender, DOB, email, contact_number, guardian_name, guardian_email, guardian_contact_number, ADDRESS)
VALUES
('123456789012', 'Dela Cruz', 'Juan', 'B', 'Male', '2005-03-15', 'juan.delacruz@example.com', '09123456789', 'Maria Dela Cruz', 'maria.delacruz@example.com', '09987654321', '123 Main St, Anytown'),
('987654321098', 'Santos', 'Maria', 'C', 'Female', '2006-07-22', 'maria.santos@example.com', '09112233445', 'Jose Santos', 'jose.santos@example.com', '09223344556', '456 Elm St, Anytown'),
('112233445566', 'Gonzales', 'Pedro', 'A', 'Male', '2004-11-10', 'pedro.gonzales@example.com', '09234567890', 'Juan Gonzales', 'juan.gonzales@example.com', '09334455667', '789 Oak St, Anytown'),
('223344556677', 'Ramos', 'Julia', 'D', 'Female', '2007-05-30', 'julia.ramos@example.com', '09456789123', 'Carmen Ramos', 'carmen.ramos@example.com', '09112233445', '101 Pine St, Anytown'),
('334455667788', 'Torres', 'Miguel', 'E', 'Male', '2005-08-25', 'miguel.torres@example.com', '09199887766', 'Fernando Torres', 'fernando.torres@example.com', '09229988776', '202 Maple St, Anytown');

-- Insert additional guidance counselors
INSERT INTO GUIDANCE_COUNSELORS (LAST_NAME, FIRST_NAME, middle_initial, gender, specialization, contact_num, email, position)
VALUES
('Perez', 'Liza', 'M', 'Female', 'Family Counseling', '09125678901', 'liza.perez@example.com', 'Guidance Counselor'),
('Mendoza', 'Carlos', 'G', 'Male', 'Behavioral Therapy', '09134567890', 'carlos.mendoza@example.com', 'Guidance Counselor');


-- Insert additional students
INSERT INTO STUDENTS_DATA (LRN, LAST_NAME, FIRST_NAME, middle_initial, gender, DOB, email, contact_number, guardian_name, guardian_email, guardian_contact_number, ADDRESS)
VALUES
('556677889900', 'Diaz', 'Nina', 'G', 'Female', '2006-01-15', 'nina.diaz@example.com', '09123456788', 'Cecilia Diaz', 'cecilia.diaz@example.com', '09112233444', '304 Birch St, Anytown'),
('998877665544', 'Castro', 'Albert', 'J', 'Male', '2005-09-11', 'albert.castro@example.com', '09198765432', 'Luisa Castro', 'luisa.castro@example.com', '09123334455', '567 Cedar St, Anytown');

-- Insert participants
INSERT INTO PARTICIPANTS (student_uid, participant_type, LAST_NAME, FIRST_NAME, email, contact_number) 
VALUES
(1, 'Student', 'Dela Cruz', 'Juan', 'juan.delacruz@example.com', '09123456789'),
(2, 'Guardian', 'Dela Cruz', 'Maria', 'maria.delacruz@example.com', '09987654321'),
(3, 'Student', 'Santos', 'Maria', 'maria.santos@example.com', '09112233445'),
(4, 'Guardian', 'Santos', 'Jose', 'jose.santos@example.com', '09223344556'),
(5, 'Student', 'Gonzales', 'Pedro', 'pedro.gonzales@example.com', '09234567890');

-- Insert violation records
INSERT INTO VIOLATION_RECORD (participant_id, violation_type, violation_description, anecdotal_record, reinforcement, V_status) 
VALUES
(1, 'Dress Code', 'Wearing an inappropriate shirt for school', 'Repeated warning about dress code violations', 'Verbal warning', 'Resolved'),
(3, 'Bullying', 'Repeatedly teasing a fellow student', 'Parent meeting scheduled to discuss behavior', 'Behavioral counseling', 'Ongoing'),
(5, 'Late Submission', 'Submitted assignment past the due date', 'One-week extension granted', 'Extra assignments', 'Resolved');

-- Insert student records
INSERT INTO STUDENT_RECORD (student_uid, violation_id, type_of_student, academic_year, semester, strand, year_level, adviser, section, stud_status) 
VALUES
(1, 1, 'Regular', '2024-2025', '1st Semester', 'STEM', 11, 'Ms. Lopez', '11-A', 'Active'),
(2, NULL, 'Regular', '2024-2025', '1st Semester', 'ABM', 12, 'Mr. Cruz', '12-B', 'Active'),
(3, 2, 'Special', '2024-2025', '2nd Semester', 'HUMSS', 12, 'Ms. Reyes', '12-C', 'Active');
-- Insert incidents
INSERT INTO INCIDENTS (student_uid, participant_id, violation_id, incident_description, action_taken, recommendation, status) 
VALUES
(1, 1, 1, 'Juan was caught wearing an improper shirt in class.', 'Verbal warning given to student', 'Ensure proper dress code is followed.', 'Resolved'),
(2, 3, 2, 'Maria Santos was seen bullying a classmate during break.', 'Meeting with parents scheduled', 'Continuous monitoring of behavior is advised.', 'Ongoing'),
(3, 5, 3, 'Pedro Gonzales missed the deadline for his project submission.', 'Extension granted for submission', 'Encourage better time management practices.', 'Resolved');

-- Insert appointments
INSERT INTO APPOINTMENTS (student_uid, counselors_id, appointment_type, appointment_date_time, appointment_status)
VALUES
(1, 1, 'Career Counseling', '2025-02-12 10:00:00', 'Scheduled'),
(3, 2, 'Adolescent Counseling', '2025-02-13 11:00:00', 'Scheduled'),
(5, 3, 'Behavioral Counseling', '2025-02-14 09:30:00', 'Scheduled');


-- 2. Then insert USERS
INSERT INTO USERS (u_name, u_pass, guidance_counselor_id) VALUES
('admin', 'admin', 1),
('jose.cruz', 'password123', 2),
('maria.lopez', 'password123', 3);
-- Insert additional users
INSERT INTO USERS (u_name, u_pass, guidance_counselor_id) VALUES
('liza.perez', 'password456', 4),
('carlos.mendoza', 'password789', 5);
