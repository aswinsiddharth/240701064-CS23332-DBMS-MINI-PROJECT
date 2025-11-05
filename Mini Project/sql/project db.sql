CREATE SEQUENCE student_roll_seq
START WITH 1001
INCREMENT BY 1
NOCACHE
NOCYCLE;

CREATE TABLE students (
    student_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    roll_number NUMBER UNIQUE NOT NULL,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL,
    full_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(100) UNIQUE
);

SELECT SEQUENCE_NAME 
FROM USER_SEQUENCES 
WHERE SEQUENCE_NAME = 'STUDENT_ROLL_SEQ';

SELECT student_roll_seq.NEXTVAL FROM dual;

select * from students;

ALTER TABLE students ADD created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

desc students;

CREATE TABLE admins (
    admin_id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(64) NOT NULL,  -- SHA-256 hash
    full_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

desc admins;

CREATE SEQUENCE admin_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE teachers (
    teacher_id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(64) NOT NULL,  -- SHA-256 hash
    full_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(100),
    subject VARCHAR2(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);
CREATE SEQUENCE teacher_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE exams (
    exam_id NUMBER PRIMARY KEY,
    exam_name VARCHAR2(200) NOT NULL,
    teacher_id NUMBER NOT NULL,
    duration_minutes NUMBER NOT NULL,
    total_marks NUMBER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active NUMBER(1) DEFAULT 1,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE
);
CREATE SEQUENCE exam_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE questions (
    question_id NUMBER PRIMARY KEY,
    exam_id NUMBER NOT NULL,
    question_text VARCHAR2(1000) NOT NULL,
    option_a VARCHAR2(500) NOT NULL,
    option_b VARCHAR2(500) NOT NULL,
    option_c VARCHAR2(500) NOT NULL,
    option_d VARCHAR2(500) NOT NULL,
    correct_answer CHAR(1) CHECK (correct_answer IN ('A', 'B', 'C', 'D')),
    marks NUMBER NOT NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE
);
CREATE SEQUENCE question_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE exam_results (
    result_id NUMBER PRIMARY KEY,
    exam_id NUMBER NOT NULL,
    roll_number NUMBER NOT NULL,
    score NUMBER NOT NULL,
    total_marks NUMBER NOT NULL,
    percentage NUMBER(5,2),
    exam_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE,
    UNIQUE (exam_id, roll_number)
);
CREATE SEQUENCE result_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE student_answers (
    answer_id NUMBER PRIMARY KEY,
    result_id NUMBER NOT NULL,
    question_id NUMBER NOT NULL,
    selected_answer CHAR(1),
    is_correct NUMBER(1),
    FOREIGN KEY (result_id) REFERENCES exam_results(result_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);
CREATE SEQUENCE answer_id_seq START WITH 1 INCREMENT BY 1;