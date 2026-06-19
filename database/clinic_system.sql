/* =====================================================
   DATABASE: CLINIC MANAGEMENT SYSTEM (FINAL VERSION)
   ===================================================== */

DROP DATABASE IF EXISTS clinic_system;
CREATE DATABASE clinic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinic_system;

-- [NHÓM 1: QUẢN TRỊ & PHÂN QUYỀN]

-- 1. ROLE
CREATE TABLE role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. ACCOUNT
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

-- 3. ACCOUNT_ROLE
CREATE TABLE account_role (
    account_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(role_id)
);

-- [NHÓM 2: NHÂN SỰ & NGÀY PHÉP]

-- 4. EXPERTISE
CREATE TABLE expertise (
    expertise_id INT AUTO_INCREMENT PRIMARY KEY,
    expertise_name VARCHAR(100) NOT NULL,
    icon_url VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 5. STAFF

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
    specialty_treatment VARCHAR(255),
    image_url VARCHAR(255),
    is_featured BOOLEAN DEFAULT FALSE,
    featured_priority INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (expertise_id) REFERENCES expertise(expertise_id)
);

-- 6. STAFF SCHEDULE
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

-- 7. LEAVE REQUEST
CREATE TABLE leave_request (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    staff_id INT NOT NULL,
    leave_type ENUM('ANNUAL', 'SICK', 'UNPAID', 'OTHER') DEFAULT 'ANNUAL',
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by INT, 
    rejection_reason TEXT, 
    reviewed_at DATETIME,  
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id),
    FOREIGN KEY (approved_by) REFERENCES staff(staff_id)
);

-- [NHÓM 3: BỆNH NHÂN & SỨC KHỎE]

-- 8. PATIENT
CREATE TABLE patient (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    date_of_birth DATE,
    phone VARCHAR(20),
    address VARCHAR(255),
    avatar_url VARCHAR(255),
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE SET NULL
);

-- 9. PATIENT VITAL PROFILE
CREATE TABLE patient_vital_profile (
    patient_id INT PRIMARY KEY,
    height INT,
    blood_type VARCHAR(5),
    allergies TEXT,
    chronic_diseases TEXT, 
    medical_history TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE
);

-- [NHÓM 4: LỊCH HẸN]

-- 10. APPOINTMENT
CREATE TABLE appointment (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    main_doctor_id INT NULL,
    service_id INT NULL,
    appointment_date DATE NOT NULL,
    time_start TIME NULL,
    time_end TIME NULL,
    appointment_type ENUM('ONLINE','WALK_IN') NOT NULL,
    status ENUM(
        'PENDING',
        'CONFIRMED',
        'CHECKED_IN',
        'IN_PROGRESS',
        'WAITING_RESULT',
        'COMPLETED',
        'SKIPPED',
        'CANCELLED',
        'NO_SHOW'
    ) DEFAULT 'PENDING',
    created_by ENUM('PATIENT','STAFF') NOT NULL,
    checkin_time DATETIME NULL,
    checkout_time DATETIME NULL,
    queue_number INT NULL,
    cancelled_by ENUM('PATIENT', 'CLINIC') NULL,
    cancel_reason TEXT,
     note TEXT NULL,
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (patient_id)
        REFERENCES patient(patient_id)
        ON DELETE CASCADE,

    FOREIGN KEY (main_doctor_id)
        REFERENCES staff(staff_id),

    FOREIGN KEY (service_id)
        REFERENCES service(service_id)
);

-- [NHÓM 5: DỊCH VỤ và XÉT NGHIỆM & GIÁ]

CREATE TABLE service (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    service_type ENUM('EXAM','LAB_TEST','IMAGING') NOT NULL DEFAULT 'LAB_TEST',
    original_price DECIMAL(10,2) NOT NULL,
    discount_price DECIMAL(10,2),
    image_url VARCHAR(255),
    is_featured BOOLEAN DEFAULT FALSE,
    featured_priority INT DEFAULT 0,
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 12. DOCTOR SERVICE PRICE
CREATE TABLE doctor_service_price (
    id INT AUTO_INCREMENT PRIMARY KEY,
    staff_id INT NOT NULL,
    service_id INT NOT NULL,
    original_price DECIMAL(10,2) NOT NULL,
    discount_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id),
    FOREIGN KEY (service_id) REFERENCES service(service_id),
    UNIQUE(staff_id, service_id)
);

-- [NHÓM 6: KHÁM BỆNH & CẬN LÂM SÀNG]

-- 13. MEDICAL RECORD
CREATE TABLE medical_record (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    appointment_id INT,
    main_doctor_id INT  NULL,
    diagnosis TEXT,
    treatment TEXT,
    note TEXT,
    status ENUM('IN_PROGRESS','WAITING_RESULT','DONE','CANCELLED') DEFAULT 'IN_PROGRESS',
    updated_by_doctor_id INT NULL,  
    edit_reason TEXT,
    consultation_fee DECIMAL(10,2) DEFAULT 0,
    service_fee DECIMAL(10,2) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id),
    FOREIGN KEY (main_doctor_id) REFERENCES staff(staff_id),
    FOREIGN KEY (updated_by_doctor_id) REFERENCES staff(staff_id)
);

-- 14. MEDICAL RECORD VITAL
CREATE TABLE medical_record_vital (
    record_id INT PRIMARY KEY,
    weight DECIMAL(5,2),
    blood_pressure VARCHAR(20),
    pulse INT,
    recorded_by INT,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES staff(staff_id)
);

-- 15. SERVICE ORDER (Để in Phiếu chỉ định)
CREATE TABLE service_order (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    service_id INT NOT NULL,
    ordered_by INT NOT NULL,
    status ENUM('ORDERED','DONE','CANCELLED', 'REJECTED') DEFAULT 'ORDERED',
    rejection_reason TEXT,                 
    sample_collected_at DATETIME NULL,     
    sample_collected_by INT NULL,          
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id),
    FOREIGN KEY (service_id) REFERENCES service(service_id),
    FOREIGN KEY (ordered_by) REFERENCES staff(staff_id),
    FOREIGN KEY (sample_collected_by) REFERENCES staff(staff_id)
);

-- 16. SERVICE RESULT (Để in tờ Kết quả)
CREATE TABLE service_result (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    result_data TEXT,
    conclusion TEXT,
    attachment_url VARCHAR(255) NULL, 
    entered_by INT NOT NULL,
    entered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES service_order(order_id),
    FOREIGN KEY (entered_by) REFERENCES staff(staff_id)
);

-- [NHÓM 7: ĐƠN THUỐC]

-- 17. MEDICINE
CREATE TABLE medicine (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active_element VARCHAR(255), 
    packing_standard VARCHAR(100), -- VD: Hộp 10 vỉ
    base_unit VARCHAR(50),         -- VD: Viên, Lọ
--     sell_price DECIMAL(10,2),    
    usage_note VARCHAR(255),
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 18. PRESCRIPTION
CREATE TABLE prescription (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id)
);

-- 19. PRESCRIPTION ITEM (Để in Đơn thuốc)
CREATE TABLE prescription_item (
    prescription_id INT NOT NULL,
    medicine_id INT NOT NULL,
    unit VARCHAR(50) NOT NULL,        -- Đơn vị kê (VD: Vỉ, Viên)
    quantity DECIMAL(8,2) NOT NULL,   -- Số lượng lẻ (1.5 viên)
    dosage VARCHAR(255),              -- Cách dùng
    PRIMARY KEY (prescription_id, medicine_id),
    FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id),
    FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id)
);

-- [NHÓM 8: CHĂM SÓC & THÔNG BÁO]

-- 20. FOLLOW UP (Tái khám)
CREATE TABLE follow_up (
    follow_up_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    scheduled_datetime DATETIME NOT NULL,
    note VARCHAR(255),
    status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED','MISSED') DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id),
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES staff(staff_id)
);

-- 21. NOTIFICATION
CREATE TABLE notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT,
    type ENUM('EMAIL','SYSTEM'),
    content TEXT,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

-- 22. FEEDBACK
CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      reply TEXT NULL,
	 replied_at DATETIME NULL,
	 replied_by INT NULL,
	 FOREIGN KEY (replied_by) REFERENCES staff(staff_id),
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id)
);


-- feed back doctor

CREATE TABLE  doctor_review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
     reply TEXT NULL,
	 replied_at DATETIME NULL,
	 replied_by INT NULL,
	 FOREIGN KEY (replied_by) REFERENCES staff(staff_id),
    FOREIGN KEY (doctor_id) REFERENCES staff(staff_id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE,
    UNIQUE KEY unique_review (doctor_id, patient_id)
);


-- [NHÓM 9: CHATBOT AI]

-- 23. CHAT SESSION
CREATE TABLE chat_session (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NULL, 
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id)
);

-- 24. CHAT MESSAGE
CREATE TABLE chat_message (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    sender_type ENUM('USER', 'BOT') NOT NULL,
    message_content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_session(session_id) ON DELETE CASCADE
);


-- [ NHÓM 10: LOGO AND QUICK ICON]
	-- ---------------------------
-- 25. TẠO BẢNG QUICK_ACTION (ICON TRUY CẬP NHANH)
-- ---------------------------
CREATE TABLE IF NOT EXISTS quick_action (
    action_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    icon_url VARCHAR(255),
    display_order INT DEFAULT 0,
    is_active TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ---------------------------
-- 26. TẠO BẢNG LOGO_SETTING
-- ---------------------------
CREATE TABLE IF NOT EXISTS logo_setting (
    logo_id INT AUTO_INCREMENT PRIMARY KEY,
    logo_key VARCHAR(50) NOT NULL UNIQUE,   -- 'main', 'favicon', 'login'
    image_url VARCHAR(255) NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS banner_setting (
    banner_id INT AUTO_INCREMENT PRIMARY KEY,
    banner_key VARCHAR(50) NOT NULL UNIQUE,   -- ví dụ: 'main', 'home', 'about'
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255) NULL,               -- nếu muốn click vào banner
    display_order INT DEFAULT 0,
    is_active TINYINT DEFAULT 1,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

--
-- ========================================================
-- 27. TẠO BẢNG system_setting (Lưu cấu hình chung)
-- ========================================================
CREATE TABLE IF NOT EXISTS system_setting (
    setting_key VARCHAR(50) NOT NULL PRIMARY KEY,
    setting_value TEXT,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- 28. TẠO BẢNG contact_message (Lưu tin nhắn liên hệ)
-- ========================================================
CREATE TABLE IF NOT EXISTS contact_message (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    subject VARCHAR(100) DEFAULT 'Khác',
    content TEXT NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'RESOLVED', 'REJECTED') DEFAULT 'PENDING',
    replied_at DATETIME NULL,
    reply_content TEXT NULL,
    replied_by INT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (replied_by) REFERENCES staff(staff_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;