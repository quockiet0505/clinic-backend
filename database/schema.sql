/* =====================================================
   DATABASE: CLINIC MANAGEMENT SYSTEM (FINAL VERSION)
   ===================================================== */

DROP DATABASE IF EXISTS clinic_system;
CREATE DATABASE clinic_system
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
USE clinic_system;

/* =====================================================
   1. ROLE
   ===================================================== */

CREATE TABLE role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

/* =====================================================
   2. ACCOUNT
   ===================================================== */

CREATE TABLE account (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    is_active TINYINT DEFAULT 1,
    failed_attempt INT DEFAULT 0,
    locked_until DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

/* =====================================================
   3. ACCOUNT_ROLE
   ===================================================== */

CREATE TABLE account_role (
    account_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(role_id)
);

/* =====================================================
   4. EXPERTISE
   ===================================================== */

CREATE TABLE expertise (
    expertise_id INT AUTO_INCREMENT PRIMARY KEY,
    expertise_name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

/* =====================================================
   5. STAFF
   ===================================================== */

CREATE TABLE staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT UNIQUE,
    expertise_id INT,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    date_of_birth DATE,
    phone VARCHAR(20),
    address VARCHAR(255),
    staff_type ENUM('DOCTOR','STAFF', 'LAB_TECH','ADMIN') NOT NULL,
    experience VARCHAR(100),
    image_url VARCHAR(255),
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (expertise_id) REFERENCES expertise(expertise_id)
);

/* =====================================================
   6. PATIENT
   ===================================================== */

CREATE TABLE patient (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    date_of_birth DATE,
    phone VARCHAR(20),
    address VARCHAR(255),
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE SET NULL
);

CREATE INDEX idx_patient_phone ON patient(phone);

/* =====================================================
   7. PATIENT VITAL PROFILE
   ===================================================== */

CREATE TABLE patient_vital_profile (
    patient_id INT PRIMARY KEY,
    height INT,
    blood_type VARCHAR(5),
    allergies TEXT,
    medical_history TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE
);

/* =====================================================
   8. STAFF SCHEDULE
   ===================================================== */

CREATE TABLE staff_schedule (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    staff_id INT NOT NULL,
    working_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('WORKING','OFF') DEFAULT 'WORKING',
    note VARCHAR(255),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

CREATE INDEX idx_schedule_staff_date 
ON staff_schedule(staff_id, working_date);

/* =====================================================
   9. SERVICE
   ===================================================== */

CREATE TABLE service (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    service_type ENUM('EXAM','LAB_TEST','IMAGING') NOT NULL,
    price DECIMAL(10,2) NOT NULL, -- giá mặc định
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

/* =====================================================
   10. DOCTOR SERVICE PRICE (GIÁ THEO BÁC SĨ)
   ===================================================== */

CREATE TABLE doctor_service_price (
    id INT AUTO_INCREMENT PRIMARY KEY,
    staff_id INT NOT NULL,
    service_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id),
    FOREIGN KEY (service_id) REFERENCES service(service_id),
    UNIQUE(staff_id, service_id)
);

/* =====================================================
   11. APPOINTMENT
   ===================================================== */

CREATE TABLE appointment (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    main_doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    time_start TIME NULL,
    time_end TIME NULL,
    appointment_type ENUM('ONLINE','WALK_IN') NOT NULL,
    status ENUM('PENDING','CONFIRMED','CHECKED_IN','IN_PROGRESS',
                'COMPLETED','CANCELLED','NO_SHOW') 
           DEFAULT 'PENDING',
    created_by ENUM('PATIENT','STAFF') NOT NULL,
    checkin_time DATETIME NULL,
    checkout_time DATETIME NULL,
    queue_number INT NULL,
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (main_doctor_id) REFERENCES staff(staff_id)
);

CREATE INDEX idx_appointment_date 
ON appointment(appointment_date);

CREATE INDEX idx_appointment_doctor 
ON appointment(main_doctor_id);

CREATE UNIQUE INDEX uq_doctor_time 
ON appointment(main_doctor_id, appointment_date, time_start);

/* =====================================================
   12. MEDICAL RECORD
   ===================================================== */

CREATE TABLE medical_record (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    appointment_id INT,
    main_doctor_id INT NOT NULL,
    diagnosis TEXT,
    treatment TEXT,
    note TEXT,
    status ENUM('IN_PROGRESS','WAITING_RESULT',
                'DONE','CANCELLED') 
           DEFAULT 'IN_PROGRESS',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id),
    FOREIGN KEY (main_doctor_id) REFERENCES staff(staff_id)
);

CREATE INDEX idx_medical_record_patient 
ON medical_record(patient_id);

/* =====================================================
   13. MEDICAL RECORD VITAL
   ===================================================== */

CREATE TABLE medical_record_vital (
    record_id INT PRIMARY KEY,
    weight DECIMAL(5,2),
    blood_pressure VARCHAR(20),
    pulse INT,
    recorded_by INT,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES staff(staff_id)
);

/* =====================================================
   14. SERVICE ORDER
   ===================================================== */

CREATE TABLE service_order (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    service_id INT NOT NULL,
    ordered_by INT NOT NULL,
    status ENUM('ORDERED','DONE','CANCELLED') DEFAULT 'ORDERED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id),
    FOREIGN KEY (service_id) REFERENCES service(service_id),
    FOREIGN KEY (ordered_by) REFERENCES staff(staff_id)
);

CREATE INDEX idx_service_order_record 
ON service_order(record_id);

/* =====================================================
   15. SERVICE RESULT
   ===================================================== */

CREATE TABLE service_result (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    result_data TEXT,
    conclusion TEXT,
    entered_by INT NOT NULL,
    entered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES service_order(order_id),
    FOREIGN KEY (entered_by) REFERENCES staff(staff_id)
);

/* =====================================================
   16. MEDICINE
   ===================================================== */

CREATE TABLE medicine (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(20),
    price DECIMAL(10,2),
    quantity INT,
    usage_note VARCHAR(255),
    active_element VARCHAR(255),
    production_unit VARCHAR(255),
    mfg DATE,
    exp DATE,
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

/* =====================================================
   17. PRESCRIPTION
   ===================================================== */

CREATE TABLE prescription (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id)
);

CREATE TABLE prescription_item (
    prescription_id INT NOT NULL,
    medicine_id INT NOT NULL,
    dosage VARCHAR(100),
    quantity INT,
    price DECIMAL(10,2),
    PRIMARY KEY (prescription_id, medicine_id),
    FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id),
    FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id)
);

/* =====================================================
   18. BILL
   ===================================================== */

CREATE TABLE bill (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    total_price DECIMAL(12,2),
    payment_method ENUM('CASH','TRANSFER'),
    status ENUM('UNPAID','PAID','CANCELLED') DEFAULT 'UNPAID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id)
);

CREATE INDEX idx_bill_record 
ON bill(record_id);

CREATE TABLE bill_item (
    bill_item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    service_id INT NULL,
    medicine_id INT NULL,
    quantity INT,
    price DECIMAL(10,2),
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id),
    FOREIGN KEY (service_id) REFERENCES service(service_id),
    FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id)
);

/* =====================================================
   19. FOLLOW UP (TÁI KHÁM – CÓ NGÀY GIỜ)
   ===================================================== */

CREATE TABLE follow_up (
    follow_up_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    scheduled_datetime DATETIME NOT NULL,
    note VARCHAR(255),
    status ENUM('PENDING','CONFIRMED',
                'COMPLETED','CANCELLED','MISSED') 
           DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id),
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES staff(staff_id)
);

CREATE INDEX idx_followup_datetime 
ON follow_up(scheduled_datetime);

/* =====================================================
   20. NOTIFICATION & FEEDBACK
   ===================================================== */

CREATE TABLE notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT,
    type ENUM('EMAIL','SYSTEM'),
    content TEXT,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id)
);