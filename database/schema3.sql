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
    chronic_diseases TEXT, -- Bổ sung: Các bệnh mãn tính
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
   9. LEAVE REQUEST
   ===================================================== */
CREATE TABLE leave_request (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    staff_id INT NOT NULL,
    leave_type ENUM('ANNUAL', 'SICK', 'UNPAID', 'OTHER') DEFAULT 'ANNUAL',
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by INT, 
    rejection_reason TEXT, -- Bổ sung: Lý do từ chối nghỉ phép
    reviewed_at DATETIME,  -- Bổ sung: Thời gian duyệt/từ chối
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id),
    FOREIGN KEY (approved_by) REFERENCES staff(staff_id)
);

CREATE INDEX idx_leave_staff ON leave_request(staff_id);
CREATE INDEX idx_leave_date ON leave_request(from_date, to_date);

/* =====================================================
   10. SERVICE
   ===================================================== */
CREATE TABLE service (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    service_type ENUM('EXAM','LAB_TEST','IMAGING') NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

/* =====================================================
   11. DOCTOR SERVICE PRICE
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
   12. APPOINTMENT
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
    cancelled_by ENUM('PATIENT', 'CLINIC') NULL, -- Bổ sung: Ai hủy lịch
    cancel_reason TEXT,                          -- Bổ sung: Lý do hủy
    cancelled_at DATETIME NULL,                  -- Bổ sung: Thời gian hủy
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (main_doctor_id) REFERENCES staff(staff_id)
);

CREATE INDEX idx_appointment_date ON appointment(appointment_date);
CREATE INDEX idx_appointment_doctor ON appointment(main_doctor_id);
CREATE UNIQUE INDEX uq_doctor_time ON appointment(main_doctor_id, appointment_date, time_start);

/* =====================================================
   13. MEDICAL RECORD
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
    updated_by_doctor_id INT NULL,  -- Bổ sung: Bác sĩ nào sửa bệnh án
    edit_reason TEXT,               -- Bổ sung: Lý do chỉnh sửa
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id),
    FOREIGN KEY (main_doctor_id) REFERENCES staff(staff_id),
    FOREIGN KEY (updated_by_doctor_id) REFERENCES staff(staff_id)
);

CREATE INDEX idx_medical_record_patient ON medical_record(patient_id);

/* =====================================================
   14. MEDICAL RECORD VITAL
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
   15. SERVICE ORDER (Chỉ định dịch vụ cận lâm sàng)
   ===================================================== */
CREATE TABLE service_order (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    service_id INT NOT NULL,
    ordered_by INT NOT NULL,
    status ENUM('ORDERED','DONE','CANCELLED', 'REJECTED') DEFAULT 'ORDERED',
    rejection_reason TEXT,                 -- Bổ sung: Lý do phòng lab từ chối mẫu
    sample_collected_at DATETIME NULL,     -- Bổ sung: Thời gian lấy mẫu
    sample_collected_by INT NULL,          -- Bổ sung: Người lấy mẫu
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id),
    FOREIGN KEY (service_id) REFERENCES service(service_id),
    FOREIGN KEY (ordered_by) REFERENCES staff(staff_id),
    FOREIGN KEY (sample_collected_by) REFERENCES staff(staff_id)
);

CREATE INDEX idx_service_order_record ON service_order(record_id);

/* =====================================================
   16. SERVICE RESULT (Kết quả cận lâm sàng)
   ===================================================== */
CREATE TABLE service_result (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    result_data TEXT,
    conclusion TEXT,
    attachment_url VARCHAR(255) NULL, -- Bổ sung: Link file đính kèm (PDF, X-Quang, Siêu âm)
    entered_by INT NOT NULL,
    entered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES service_order(order_id),
    FOREIGN KEY (entered_by) REFERENCES staff(staff_id)
);

/* =====================================================
   17. MEDICINE
   ===================================================== */
CREATE TABLE medicine (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active_element VARCHAR(255), 
    unit VARCHAR(20),            
    sell_price DECIMAL(10,2),    
    quantity INT DEFAULT 0,      
    min_stock_level INT DEFAULT 10, 
    usage_note VARCHAR(255),
    production_unit VARCHAR(255),
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

/* =====================================================
   18. SUPPLIER
   ===================================================== */
CREATE TABLE supplier (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    is_active TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

/* =====================================================
   19. PURCHASE ORDER (Phiếu nhập hàng)
   ===================================================== */
CREATE TABLE purchase_order (
    po_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15,2) DEFAULT 0,
    status ENUM('PENDING', 'RECEIVED', 'CANCELLED') DEFAULT 'PENDING',
    created_by INT NOT NULL, 
    note TEXT,
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
    FOREIGN KEY (created_by) REFERENCES staff(staff_id)
);

/* =====================================================
   20. PURCHASE ORDER ITEM (Chi tiết nhập hàng)
   ===================================================== */
CREATE TABLE purchase_order_item (
    po_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,               -- Số lượng đặt
    received_quantity INT DEFAULT 0,     -- Bổ sung: Số lượng thực nhận
    rejected_quantity INT DEFAULT 0,     -- Bổ sung: Số lượng bị lỗi/trả lại
    unit_price DECIMAL(10,2) NOT NULL, 
    mfg DATE, 
    exp DATE, 
    batch_number VARCHAR(50), 
    note TEXT,                           -- Bổ sung: Ghi chú nếu hàng lỗi
    PRIMARY KEY (po_id, medicine_id),
    FOREIGN KEY (po_id) REFERENCES purchase_order(po_id),
    FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id)
);

/* =====================================================
   21. INVENTORY TRANSACTION (Lịch sử xuất/nhập tồn kho)
   ===================================================== */
CREATE TABLE inventory_transaction (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    transaction_type ENUM('IMPORT', 'DISPENSE', 'EXPIRED', 'LOST', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL, -- Có thể số âm hoặc dương
    reference_id VARCHAR(50), -- Chứa ID của hóa đơn, đơn nhập, hoặc mã phiếu hủy
    handled_by INT NOT NULL,
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id),
    FOREIGN KEY (handled_by) REFERENCES staff(staff_id)
);

/* =====================================================
   22. PRESCRIPTION (Đơn thuốc)
   ===================================================== */
CREATE TABLE prescription (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id)
);

/* =====================================================
   23. PRESCRIPTION ITEM (Chi tiết đơn thuốc)
   ===================================================== */
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
   24. BILL (Hóa đơn)
   ===================================================== */
CREATE TABLE bill (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    total_price DECIMAL(12,2),
    payment_method ENUM('CASH','TRANSFER'),
    status ENUM('UNPAID','PAID','CANCELLED', 'REFUNDED') DEFAULT 'UNPAID',
    discount_reason TEXT,  -- Bổ sung: Lý do giảm giá (nếu có)
    cancel_reason TEXT,    -- Bổ sung: Lý do hủy hóa đơn
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id)
);

CREATE INDEX idx_bill_record ON bill(record_id);

/* =====================================================
   25. BILL ITEM (Chi tiết hóa đơn)
   ===================================================== */
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
   26. REFUND LOG (Lịch sử hoàn tiền)
   ===================================================== */
CREATE TABLE refund_log (
    refund_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    reason TEXT NOT NULL,
    processed_by INT NOT NULL,
    refunded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id),
    FOREIGN KEY (processed_by) REFERENCES staff(staff_id)
);

/* =====================================================
   27. EXPENSE CATEGORY (Danh mục chi tiêu)
   ===================================================== */
CREATE TABLE expense_category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL, 
    description VARCHAR(255)
);

/* =====================================================
   28. CLINIC EXPENSE (Phiếu chi)
   ===================================================== */
CREATE TABLE clinic_expense (
    expense_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    expense_date DATE NOT NULL,
    payment_method ENUM('CASH','TRANSFER') DEFAULT 'CASH',
    description TEXT,
    receipt_image_url VARCHAR(255), 
    created_by INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES expense_category(category_id),
    FOREIGN KEY (created_by) REFERENCES staff(staff_id)
);
   
/* =====================================================
   29. FOLLOW UP (Tái khám)
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

CREATE INDEX idx_followup_datetime ON follow_up(scheduled_datetime);

/* =====================================================
   30. NOTIFICATION
   ===================================================== */
CREATE TABLE notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT,
    type ENUM('EMAIL','SYSTEM'),
    content TEXT,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

/* =====================================================
   31. FEEDBACK
   ===================================================== */
CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES medical_record(record_id)
);

/* =====================================================
   32. CHAT SESSION (Phiên chat AI)
   ===================================================== */
CREATE TABLE chat_session (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NULL, -- Có thể NULL nếu khách chưa đăng nhập
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    FOREIGN KEY (patient_id) REFERENCES patient(patient_id)
);

/* =====================================================
   33. CHAT MESSAGE (Nội dung chat AI)
   ===================================================== */
CREATE TABLE chat_message (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    sender_type ENUM('USER', 'BOT') NOT NULL,
    message_content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_session(session_id) ON DELETE CASCADE
);